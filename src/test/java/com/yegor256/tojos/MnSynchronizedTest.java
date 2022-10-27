/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2022 Yegor Bugayenko
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.yegor256.tojos;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test case for {@link MnSynchronized}.
 *
 * @since 0.3.0
 */
class MnSynchronizedTest {

    /**
     * Number of threads.
     */
    static final int THREADS = 5;

    /**
     * The logger.
     */
    private static final Logger LOGGER =
        Logger.getLogger(MnSynchronizedTest.class.getName());

    /**
     * The mono under test.
     */
    private Mono mono;

    /**
     * The executor service.
     */
    private ExecutorService executors;

    /**
     * The latch.
     */
    private CountDownLatch latch;

    /**
     * The accumulator that contains a changes in the under test mono.
     */
    private Collection<Collection<Map<String, String>>> changes;

    /**
     * The row.
     */
    private Map<String, String> row;

    @BeforeEach
    final void setUp(@TempDir final Path temp) {
        this.latch = new CountDownLatch(1);
        this.mono = new MnSynchronized(new MnJson(temp.resolve("foo/bar/baz.json")));
        this.executors = Executors.newFixedThreadPool(MnSynchronizedTest.THREADS);
        this.changes = Collections.synchronizedList(new ArrayList<>(0));
        this.row = Collections.synchronizedMap(new HashMap<>(0));
    }

    /**
     * Thread-safety test.
     * In this test, we check the number of changes in MnSynchronized mono.
     * It should be equal to the sum of the arithmetic progression over the number of threads.
     *
     * @throws InterruptedException When fails
     */
    @Test
    final void concurrentScenario() throws InterruptedException {
        for (int trds = 0; trds < MnSynchronizedTest.THREADS; ++trds) {
            this.row.put(Tojos.KEY, String.valueOf(trds));
            this.executors.submit(this.testCase(trds));
        }
        this.waitTillEnd();
        MatcherAssert.assertThat(
            MnSynchronizedTest.totalSize(this.changes),
            Matchers.equalTo(MnSynchronizedTest.expectedSize())
        );
    }

    private Callable<?> testCase(final int current) {
        final AtomicReference<Collection<Map<String, String>>> rows = new AtomicReference<>();
        return () -> {
            try {
                this.latch.await();
                rows.set(this.mono.read());
                rows.get().add(this.row);
                this.mono.write(rows.get());
                this.changes.add(rows.get());
                return this.latch;
            } finally {
                MnSynchronizedTest.LOGGER.log(
                    Level.INFO,
                    String.format(
                        "Finished at thread %d, written %d rows",
                        current,
                        rows.get().size()
                    )
                );
            }
        };
    }

    private void waitTillEnd() throws InterruptedException {
        this.latch.countDown();
        this.executors.shutdown();
        assert this.executors.awaitTermination(10L, TimeUnit.SECONDS);
    }

    /**
     * The expected.
     *
     * @return Summary of arithmetic progression from 1 to number of threads
     */
    private static Integer expectedSize() {
        final int len = MnSynchronizedTest.THREADS;
        return len + (len - 1) * len / 2;
    }

    private static Integer totalSize(final Iterable<Collection<Map<String, String>>> accm) {
        final AtomicInteger res = new AtomicInteger();
        accm.forEach(rows -> res.addAndGet(rows.size()));
        return res.get();
    }
}
