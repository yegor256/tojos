/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2025 Yegor Bugayenko
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
