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
import java.util.*;
import java.util.concurrent.*;
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
     * The mono under test.
     */
    private Mono shared;

    /**
     * The executor.
     */
    private ExecutorService executor;

    /**
     * An additional rows.
     */
    private Collection<Map<String, String>> additional;

    /**
     * The latch.
     */
    private CountDownLatch latch;

    @BeforeEach
    final void setUp(@TempDir final Path temp) {
        this.shared = new MnSynchronized(new MnJson(temp.resolve("bar/baz/a.json")));
        this.executor = Executors.newFixedThreadPool(MnSynchronizedTest.THREADS);
        this.latch = new CountDownLatch(1);
        this.additional = rowsByThreads();
    }

    @Test
    final void writesConcurrently() throws InterruptedException {
        for (int trds = 1; trds <= MnSynchronizedTest.THREADS; ++trds) {
            this.executor.submit(
                () -> {
                    this.latch.await();
                    final Collection<Map<String, String>> increased = this.shared.read();
                    increased.addAll(this.additional);
                    this.shared.write(increased);
                    return this.shared.read().size();
                }
            );
        }
        this.latch.countDown();
        this.executor.shutdown();
        assert this.executor.awaitTermination(1, TimeUnit.MINUTES);
        MatcherAssert.assertThat(
            this.shared.read().size(),
            Matchers.equalTo(
                MnSynchronizedTest.THREADS * MnSynchronizedTest.THREADS
            )
        );
    }

    /**
     * The rows which size equal to number of threads.
     *
     * @return Collection of rows
     */
    private static Collection<Map<String, String>> rowsByThreads() {
        return Collections.nCopies(
            MnSynchronizedTest.THREADS,
            Collections.singletonMap(
                Tojos.KEY,
                String.valueOf(MnSynchronizedTest.THREADS)
            )
        );
    }
}
