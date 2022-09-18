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
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test case for {@link TjDefault}.
 *
 * @since 0.3.0
 */
final class TjDefaultTest {

    @ParameterizedTest
    @ValueSource(strings = {"a.csv", "a.json"})
    void simpleScenario(final String file, @TempDir final Path temp) {
        final Tojos tojos = new TjDefault(new MnCsv(temp.resolve(file)));
        tojos.add("foo").set("k", "v").set("a", "b");
        tojos.select(t -> t.exists("k")).iterator().next();
        MatcherAssert.assertThat(
            tojos.select(t -> t.exists("k")).iterator().next().get("a"),
            Matchers.equalTo("b")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"x.csv", "x.json"})
    void addTojo(final String file, @TempDir final Path temp) {
        final Tojos tojos = new TjDefault(new MnJson(temp.resolve(file)));
        tojos.add("foo-1");
        MatcherAssert.assertThat(
            new TjSmart(tojos).size(),
            Matchers.equalTo(1)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"y.csv", "y.json"})
    void uniqueIds(final String file, @TempDir final Path temp) {
        final Tojos tojos = new TjDefault(new MnTabs(temp.resolve(file)));
        final String name = "foo11";
        tojos.add(name);
        tojos.add(name);
        MatcherAssert.assertThat(
            new TjSmart(tojos).size(),
            Matchers.equalTo(1)
        );
    }

    @Test
    void massiveWrite(@TempDir final Path temp) {
        final Tojos tojos = new TjDefault(
            new MnSticky(
                new MnJson(temp.resolve("big-data.json"))
            )
        );
        final int total = 100;
        for (int idx = 0; idx < total; ++idx) {
            final String key = String.format("k%d", idx);
            tojos.add(String.format("key-%d", idx))
                .set(key, String.format("v%d", idx));
            MatcherAssert.assertThat(
                tojos.select(r -> r.exists(key)),
                Matchers.iterableWithSize(1)
            );
        }
        MatcherAssert.assertThat(
            tojos.select(r -> true),
            Matchers.iterableWithSize(total)
        );
    }

}
