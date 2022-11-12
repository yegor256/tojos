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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
     * The number of changes in under-test mono.
     */
    private final AtomicInteger actual = new AtomicInteger(0);

    /**
     * The mono under test.
     */
    private Mono mono;

    /**
     * The executor.
     */
    private ThreadPoolExecutor executor;

    /**
     * The blocking queue.
     */
    private BlockingQueue<Runnable> queue;

    @BeforeEach
    final void setUp() {
        this.mono = new MnSynchronized(new MnMemory());
        this.queue = new LinkedBlockingQueue<>(MnSynchronizedTest.THREADS);
        this.executor = new ThreadPoolExecutor(
            MnSynchronizedTest.THREADS,
            MnSynchronizedTest.THREADS,
            5L,
            TimeUnit.SECONDS,
            this.queue,
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
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
        this.executor.prestartAllCoreThreads();
        for (int trds = 0; trds < MnSynchronizedTest.THREADS; ++trds) {
            this.queue.offer(
                new MnSynchronizedTest.TestTask(
                    trds,
                    this.mono,
                    this.actual
                )
            );
        }
        this.executor.shutdown();
        assert this.executor.awaitTermination(5L, TimeUnit.SECONDS);
        MatcherAssert.assertThat(
            this.actual.get(),
            Matchers.equalTo(MnSynchronizedTest.expectedSize())
        );
    }

    /**
     * The expected.
     *
     * @return Sum of arithmetic progression from 1 to number of threads
     */
    private static Integer expectedSize() {
        final int len = MnSynchronizedTest.THREADS;
        return len + (len - 1) * len / 2;
    }

    /**
     * The test task to concurrent read and write operations.
     *
     * @since 0.3.0
     */
    private static final class TestTask implements Runnable {

        /**
         * The logger.
         */
        private static final Logger LOGGER =
            Logger.getLogger(MnSynchronizedTest.class.getName());

        /**
         * The id of thread.
         */
        private final int idx;

        /**
         * Local mono.
         */
        private final Mono mono;

        /**
         * Row to write into mono.
         */
        private final Map<String, String> row;

        /**
         * The counter.
         */
        private final AtomicInteger counter;

        /**
         * Ctor.
         *
         * @param idx The id
         * @param mno The mono
         * @param cntr The counter
         */
        TestTask(
            final int idx,
            final Mono mno,
            final AtomicInteger cntr
        ) {
            this.idx = idx;
            this.mono = mno;
            this.row = new HashMap<>(0);
            this.counter = cntr;
        }

        @Override
        public void run() {
            this.row.put(Tojos.KEY, String.valueOf(this.idx));
            final Collection<Map<String, String>> rows = this.mono.read();
            rows.add(this.row);
            this.mono.write(rows);
            this.counter.addAndGet(rows.size());
            MnSynchronizedTest.TestTask.LOGGER.log(
                Level.INFO,
                String.format(
                    "Thread %d, written %d rows",
                    this.idx,
                    rows.size()
                )
            );
        }
    }
}
