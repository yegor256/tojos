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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test case for MonoTojo usage in concurrent environment.
 *
 * @since 0.16
 */
@ExtendWith(MktmpResolver.class)
final class ToMonoTest {

    @Test
    void readsAndWrites(@Mktmp final Path temp) {
        final Tojos tojos = new TjDefault(new MnJson(temp.resolve("mono.json")));
        tojos.add("foo").set("k", "v");
        MatcherAssert.assertThat(
            "reads back",
            tojos.select(t -> true).iterator().next().get("k"),
            Matchers.equalTo("v")
        );
    }

    @Test
    void setsConcurrentlyToTheSameMono(@Mktmp final Path temp) {
        final Tojo tojo = new TjDefault(
            new MnJson(temp.resolve("mono.json"))
        ).add("foo");
        MatcherAssert.assertThat(
            "must work fine",
            new Together<>(
                thread -> {
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
