/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2026 Yegor Bugayenko
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
        final Tojos tojos = new TjDefault(new MnPostponed(mono, 500L));
        final int total = 200;
        for (int idx = 0; idx < total; ++idx) {
            tojos.add(String.format("key-%d", idx))
                .set(String.format("k%d", idx), String.format("v%d", idx));
        }
        tojos.close();
        MatcherAssert.assertThat(
            "must persist all entries to disk",
            new TjDefault(mono).select(r -> true),
            Matchers.iterableWithSize(total)
        );
    }

    @Test
    void avoidsSavingAfterDestruction(@Mktmp final Path temp) {
        MatcherAssert.assertThat(
            "must add entry without saving to disk immediately",
            new TjDefault(
                new MnPostponed(
                    new MnJson(temp.resolve("x/y/z/data.json")),
                    500L
                )
            ).add("hello"),
            Matchers.notNullValue()
        );
    }

}
