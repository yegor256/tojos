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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test case for {@link MnSynchronized}.
 *
 * @since 0.3.0
 */
class MnSynchronizedTest {

    @Test
    void concurrentScenario(@TempDir final Path temp) throws InterruptedException {
        final Mono sync = new MnSynchronized(new MnJson(temp.resolve("foo/bar/baz.json")));
        final int threads = 300;
        final ExecutorService service = Executors.newFixedThreadPool(threads);
        final CountDownLatch latch = new CountDownLatch(1);
        final Collection<Collection<Map<String, String>>> actual =
            Collections.synchronizedList(new ArrayList<>(0));
        for (int trds = 0; trds < threads; ++trds) {
            final int curr = trds;
            service.submit(
                () -> {
                    latch.await();
                    final Map<String, String> row = new HashMap<>(0);
                    row.put(Tojos.KEY, String.format("%d", curr));
                    final Collection<Map<String, String>> rows = sync.read();
                    rows.add(row);
                    sync.write(rows);
                    actual.add(rows);
                    return rows;
                }
            );
        }
        latch.countDown();
        service.shutdown();
        assert service.awaitTermination(10L, TimeUnit.SECONDS);
        final AtomicInteger real = new AtomicInteger();
        actual.forEach(rows -> real.addAndGet(rows.size()));
        MatcherAssert.assertThat(
            real.get(),
            Matchers.equalTo(MnSynchronizedTest.expected(threads))
        );
    }

    private static Integer expected(final int range) {
        final Collection<Collection<Map<String, String>>> acm = new ArrayList<>(0);
        for (int idx = 0; idx < range; ++idx) {
            final Collection<Map<String, String>> rows = new ArrayList<>(0);
            for (int jdx = 0; jdx <= idx; ++jdx) {
                final Map<String, String> row = new HashMap<>(0);
                row.put(Tojos.KEY, String.format("%d", jdx));
                rows.add(row);
            }
            acm.add(rows);
        }
        final AtomicInteger res = new AtomicInteger();
        acm.forEach(rs -> res.addAndGet(rs.size()));
        return res.get();
    }
}
