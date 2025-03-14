/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import com.yegor256.Mktmp;
import com.yegor256.MktmpResolver;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.cactoos.Scalar;
import org.cactoos.experimental.Threads;
import org.cactoos.number.SumOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test case for {@link MnSticky}.
 *
 * @since 0.12.0
 */
@ExtendWith(MktmpResolver.class)
final class MnStickyTest {

    @Test
    void readsFromEmpty() {
        final Mono mono = new MnSticky(new MnMemory());
        MatcherAssert.assertThat(
            "must work fine",
            mono.read().size(),
            Matchers.equalTo(0)
        );
    }

    @Test
    void checksSimpleScenario(@Mktmp final Path temp) {
        final Mono sticky = new MnSticky(new MnCsv(temp.resolve("x.csv")));
        final Map<String, String> row = new HashMap<>(0);
        final String key = Tojos.ID_KEY;
        final String value = "привет,\t\n \"друг\"!";
        row.put(key, value);
        final Collection<Map<String, String>> rows = new ArrayList<>(0);
        rows.add(row);
        sticky.write(rows);
        MatcherAssert.assertThat(
            "must work fine",
            sticky.read().iterator().next().get(key),
            Matchers.equalTo(value)
        );
    }

    @Test
    void readsAndWritesConcurrentlyWithHighFrequency(@Mktmp final Path temp) {
        final TjDefault tojos = new TjDefault(new MnSticky(new MnJson(temp.resolve("x.json"))));
        final int processors = Runtime.getRuntime().availableProcessors();
        MatcherAssert.assertThat(
            "must work fine",
            new SumOf(
                new Threads<>(
                    processors,
                    IntStream.range(0, processors)
                        .mapToObj(String::valueOf)
                        .map(tojos::add)
                        .map(
                            tojo -> (Scalar<Integer>) () -> {
                                final String key = "uuid";
                                final String uuid = UUID.randomUUID().toString();
                                tojo.set(key, uuid);
                                tojo.get(key);
                                tojo.set(key, uuid);
                                tojo.get(key);
                                tojo.set(key, uuid);
                                tojo.get(key);
                                return 1;
                            }
                        ).collect(Collectors.toList())
                )
            ),
            Matchers.equalTo(processors)
        );
    }
}
