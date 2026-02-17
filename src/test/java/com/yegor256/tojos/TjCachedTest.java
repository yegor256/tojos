/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import com.yegor256.Mktmp;
import com.yegor256.MktmpResolver;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test for {@link TjCached}.
 *
 * @since 1.0
 */
@ExtendWith(MktmpResolver.class)
final class TjCachedTest {

    @Test
    void addsClearsCache(@Mktmp final Path temp) {
        final Tojos tojos = new TjDefault(new MnCsv(temp.resolve("my-tojos-1.csv")));
        final String[] keys = {"k10", "k20"};
        tojos.add("A0").set(keys[0], "v10").set(keys[1], "vv10");
        tojos.add("B0").set(keys[0], "v20").set(keys[1], "vv20");
        tojos.add("C0").set(keys[0], "v30").set(keys[1], "vv30");
        final Tojos cached = new TjCached(tojos);
        cached.select(x -> true);
        cached.add("D0").set(keys[0], "v40").set(keys[1], "vv40");
        MatcherAssert.assertThat(
            "must work fine",
            cached.select(x -> true).size(),
            Matchers.equalTo(4)
        );
    }

    @Test
    void selectsFromCached(@Mktmp final Path temp) {
        final Tojos tojos = new TjDefault(new MnCsv(temp.resolve("my-tojos-2.csv")));
        final String[] keys = {"k11", "k21"};
        tojos.add("A1").set(keys[0], "v11").set(keys[1], "vv11");
        tojos.add("B1").set(keys[0], "v21").set(keys[1], "vv21");
        tojos.add("C1").set(keys[0], "v31").set(keys[1], "vv31");
        final Tojos cached = new TjCached(tojos);
        cached.select(x -> true);
        tojos.add("D1").set(keys[0], "v41").set(keys[1], "vv41");
        MatcherAssert.assertThat(
            "must work fine",
            cached.select(x -> true).size(),
            Matchers.equalTo(3)
        );
    }

    @Test
    void selectsRowsFast(@Mktmp final Path temp) {
        final Tojos cached =
            new TjCached(
                new TjDefault(
                    new MnPostponed(new MnCsv(temp.resolve("my-tojos-4.csv")))
                )
            );
        final String[] keys = {"UUID", "Age"};
        final Collection<String> selected = new HashSet<>();
        final int rows = 10_000;
        for (int row = 0; rows > row; row = row + 1) {
            final String uuid = UUID.randomUUID().toString();
            cached.add(uuid).set(keys[0], uuid).set(keys[1], String.valueOf(row));
            if (0 == row % 100) {
                selected.add(uuid);
            }
        }
        MatcherAssert.assertThat(
            "must select rows within 100ms",
            TjCachedTest.timed(
                () -> cached.select(
                    x -> selected.contains(x.get(keys[0]))
                        && rows / 2 < Integer.parseInt(x.get(keys[1]))
                )
            ),
            Matchers.lessThan(100L)
        );
    }

    /**
     * Measure execution time of a runnable in milliseconds.
     * @param task Task to time
     * @return Elapsed time in milliseconds
     */
    private static long timed(final Runnable task) {
        final long[] times = {System.nanoTime(), 0L};
        task.run();
        times[1] = System.nanoTime();
        return TimeUnit.NANOSECONDS.toMillis(times[1] - times[0]);
    }
}
