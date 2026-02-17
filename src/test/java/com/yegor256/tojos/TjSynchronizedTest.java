/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import com.yegor256.Mktmp;
import com.yegor256.MktmpResolver;
import com.yegor256.Together;
import java.nio.file.Path;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test case for {@link TjSynchronized}.
 *
 * @since 0.3.0
 */
@ExtendWith(MktmpResolver.class)
final class TjSynchronizedTest {

    @ParameterizedTest
    @ValueSource(strings = {"x.csv", "x.json"})
    void addsTojoParallel(final String file, @Mktmp final Path temp) {
        final Tojos tojos = new TjSynchronized(new TjDefault(new MnJson(temp.resolve(file))));
        final int threads = 50;
        MatcherAssert.assertThat(
            "must add all tojos in parallel",
            new Together<>(threads, thread -> tojos.add(Integer.toString(thread))),
            Matchers.iterableWithSize(threads)
        );
    }

    @RepeatedTest(10)
    void readsAndWritesConcurrentlyWithHighFrequency(@Mktmp final Path temp) {
        final Tojos tojos = new TjSynchronized(
            new TjDefault(
                new MnSynchronized(
                    new MnJson(temp.resolve("mono.json"))
                )
            )
        );
        MatcherAssert.assertThat(
            "must work fine",
            new Together<>(
                thread -> {
                    final Tojo tojo = tojos.add(UUID.randomUUID().toString());
                    final String key = "foo";
                    final String uuid = UUID.randomUUID().toString();
                    tojo.set(key, uuid);
                    tojo.get(key);
                    tojo.set(key, uuid);
                    tojo.get(key);
                    tojo.set(key, uuid);
                    tojo.get(key);
                    return 1;
                }
            ),
            Matchers.not(Matchers.hasItem(0))
        );
    }
}
