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

/**
 * Test for {@link TjCached}.
 *
 * @since 1.0
 */
final class TjCachedTest {

    @Test
    void testAddClearsCache(@TempDir final Path temp) {
        final Tojos tojos = new TjDefault(new MnCsv(temp.resolve("my-tojos-1.csv")));
        final String[] keys = {"k10", "k20"};
        tojos.add("A0").set(keys[0], "v10").set(keys[1], "vv10");
        tojos.add("B0").set(keys[0], "v20").set(keys[1], "vv20");
        tojos.add("C0").set(keys[0], "v30").set(keys[1], "vv30");
        final Tojos cached = new TjCached(tojos);
        cached.select(x -> true);
        cached.add("D0").set(keys[0], "v40").set(keys[1], "vv40");
        MatcherAssert.assertThat(
            cached.select(x -> true).size(),
            Matchers.equalTo(4)
        );
    }

    @Test
    void testSelectFromCached(@TempDir final Path temp) {
        final Tojos tojos = new TjDefault(new MnCsv(temp.resolve("my-tojos-2.csv")));
        final String[] keys = {"k11", "k21"};
        tojos.add("A1").set(keys[0], "v11").set(keys[1], "vv11");
        tojos.add("B1").set(keys[0], "v21").set(keys[1], "vv21");
        tojos.add("C1").set(keys[0], "v31").set(keys[1], "vv31");
        final Tojos cached = new TjCached(tojos);
        cached.select(x -> true);
        tojos.add("D1").set(keys[0], "v41").set(keys[1], "vv41");
        MatcherAssert.assertThat(
            cached.select(x -> true).size(),
            Matchers.equalTo(3)
        );
    }
}
