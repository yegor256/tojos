/*
 * The MIT License (MIT)
 *
 * SPDX-FileCopyrightText: Copyright (c) 2021-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import com.yegor256.Mktmp;
import com.yegor256.MktmpResolver;
import java.nio.file.Path;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test case for {@link TjDefault}.
 *
 * @since 0.3.0
 */
@ExtendWith(MktmpResolver.class)
final class MnPostponedTest {

    @Test
    void writesMassively(@Mktmp final Path temp) throws Exception {
        final Mono mono = new MnJson(temp.resolve("big-data.json"));
        final long delay = 500L;
        final Tojos tojos = new TjDefault(
            new MnPostponed(mono, delay)
        );
        final int total = 200;
        for (int idx = 0; idx < total; ++idx) {
            final String key = String.format("k%d", idx);
            tojos.add(String.format("key-%d", idx))
                .set(key, String.format("v%d", idx));
            MatcherAssert.assertThat(
                "must work fine",
                tojos.select(r -> r.exists(key)),
                Matchers.iterableWithSize(1)
            );
        }
        MatcherAssert.assertThat(
            "must work fine",
            tojos.select(r -> true),
            Matchers.iterableWithSize(total)
        );
        tojos.close();
        MatcherAssert.assertThat(
            "must work fine",
            new TjDefault(mono).select(r -> true),
            Matchers.iterableWithSize(total)
        );
    }

    @Test
    @SuppressWarnings("JTCOP.RuleAssertionMessage")
    void avoidsSavingAfterDestruction(@Mktmp final Path temp) {
        final Mono mono = new MnJson(temp.resolve("x/y/z/data.json"));
        final Tojos tojos = new TjDefault(
            new MnPostponed(mono, 500L)
        );
        tojos.add("hello");
    }

}
