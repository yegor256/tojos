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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private final AtomicInteger counter = new AtomicInteger(0);

    /**
     * The mono under test.
     */
    private Mono shared;

    /**
     * The executor.
     */
    private ExecutorService executor;

    @BeforeEach
    final void setUp() {
        this.shared = new MnSynchronized(new MnMemory());
        this.executor = Executors.newFixedThreadPool(MnSynchronizedTest.THREADS);
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
        for (int trds = 1; trds <= MnSynchronizedTest.THREADS; ++trds) {
            this.executor.submit(
                new MnSynchronizedTest.TestTask(
                    MnSynchronizedTest.rowsBySize(trds),
                    this.shared,
                    this.counter
                )
            );
        }
        this.executor.shutdown();
        assert this.executor.awaitTermination(5L, TimeUnit.SECONDS);
        MatcherAssert.assertThat(
            this.counter.get(),
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
     * The rows to write.
     *
     * @param size The size of rows
     * @return Collection of rows
     */
    private static Collection<Map<String, String>> rowsBySize(final int size) {
        final Map<String, String> row = new HashMap<>(0);
        row.put(Tojos.KEY, String.valueOf(size));
        final Collection<Map<String, String>> res = new ArrayList<>(size);
        for (int idx = 0; idx < size; ++idx) {
            res.add(row);
        }
        return res;
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
         * Local mono.
         */
        private final Mono mono;

        /**
         * Rows to write.
         */
        private final Collection<Map<String, String>> rows;

        /**
         * The counter.
         */
        private final AtomicInteger counter;

        /**
         * Ctor.
         *
         * @param rws The rows to write
         * @param mno The mono
         * @param cntr The counter
         */
        TestTask(
            final Collection<Map<String, String>> rws,
            final Mono mno,
            final AtomicInteger cntr
        ) {
            this.rows = rws;
            this.mono = mno;
            this.counter = cntr;
        }

        @Override
        public void run() {
            this.mono.write(this.rows);
            this.counter.addAndGet(this.rows.size());
            MnSynchronizedTest.TestTask.LOGGER.log(
                Level.INFO,
                String.format(
                    "Thread %s, written %d rows.\nReading:\n%s",
                    this,
                    this.rows.size(),
                    this.mono.read()
                )
            );
        }
    }
}
