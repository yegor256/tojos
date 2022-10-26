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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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

    static final int THREADS = 5;

    private Mono sync;

    private ExecutorService executors;

    private CountDownLatch latch;

    private Collection<Collection<Map<String, String>>> acum;

    @BeforeEach
    final void setUp(@TempDir final Path temp) {
        this.sync = new MnSynchronized(
            new MnJson(
                temp.resolve("foo/bar/baz.json")
            )
        );
        this.executors = Executors.newFixedThreadPool(MnSynchronizedTest.THREADS);
        this.latch = new CountDownLatch(1);
        this.acum = Collections.synchronizedList(new ArrayList<>(0));
    }

    @Test
    final void concurrentScenario() throws InterruptedException {
        for (int trds = 0; trds < MnSynchronizedTest.THREADS; ++trds) {
            final Map<String, String> row = new HashMap<>(0);
            row.put(Tojos.KEY, String.format("%d", trds));
            this.executors.submit(
                () -> {
                    this.latch.await();
                    final Collection<Map<String, String>> rows = this.sync.read();
                    rows.add(row);
                    this.sync.write(rows);
                    this.acum.add(rows);
                    return rows;
                }
            );
        }
        this.waitTillEnd();
        final AtomicInteger actual = MnSynchronizedTest.totalSize(this.acum);
        MatcherAssert.assertThat(
            actual.get(),
            Matchers.equalTo(MnSynchronizedTest.expected())
        );
    }

    private void waitTillEnd() throws InterruptedException {
        this.latch.countDown();
        this.executors.shutdown();
        assert this.executors.awaitTermination(10L, TimeUnit.SECONDS);
    }

    private static Integer expected() {
        final Collection<Collection<Map<String, String>>> acum = new ArrayList<>(0);
        for (int idx = 0; idx < MnSynchronizedTest.THREADS; ++idx) {
            final Collection<Map<String, String>> rows = new ArrayList<>(0);
            for (int jdx = 0; jdx <= idx; ++jdx) {
                final Map<String, String> row = new HashMap<>(0);
                row.put(Tojos.KEY, String.format("%d", jdx));
                rows.add(row);
            }
            acum.add(rows);
        }
        final AtomicInteger res = MnSynchronizedTest.totalSize(acum);
        return res.get();
    }

    private static AtomicInteger totalSize(final Iterable<Collection<Map<String, String>>> acm) {
        final AtomicInteger res = new AtomicInteger();
        acm.forEach(rows -> res.addAndGet(rows.size()));
        return res;
    }
}
