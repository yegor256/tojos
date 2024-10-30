/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2024 Yegor Bugayenko
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link MnMemory}.
 *
 * @since 0.12.0
 */
final class MnMemoryTest {

    @Test
    void readsEmpty() {
        final Mono mono = new MnMemory();
        MatcherAssert.assertThat(
            "must work fine",
            mono.read().size(),
            Matchers.equalTo(0)
        );
    }

    @Test
    void checksSimpleScenario() {
        final Mono mono = new MnMemory();
        final Map<String, String> row = new HashMap<>(0);
        final String key = Tojos.ID_KEY;
        final String value = "привет,\t\n \"друг\"!";
        row.put(key, value);
        final Collection<Map<String, String>> rows = new ArrayList<>(0);
        rows.add(row);
        mono.write(rows);
        MatcherAssert.assertThat(
            "must work fine",
            mono.read().iterator().next().get(key),
            Matchers.equalTo(value)
        );
    }
}
