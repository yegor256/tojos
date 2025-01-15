/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021-2025 Yegor Bugayenko
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test case for {@link MnTabs}.
 *
 * @since 0.7.0
 */
@ExtendWith(MktmpResolver.class)
final class MnTabsTest {

    @Test
    void checksSimpleScenario(@Mktmp final Path temp) {
        final Mono tabs = new MnTabs(temp.resolve("foo/bar/a.tabs"));
        final Collection<Map<String, String>> rows = tabs.read();
        MatcherAssert.assertThat(
            "must work fine",
            tabs.read().size(),
            Matchers.equalTo(0)
        );
        final Map<String, String> row = new HashMap<>(0);
        final String key = Tojos.ID_KEY;
        final String value = "привет,\t\r\n друг!";
        row.put(key, value);
        rows.add(row);
        tabs.write(rows);
        MatcherAssert.assertThat(
            "must work fine",
            tabs.read().iterator().next().get(key),
            Matchers.equalTo(value)
        );
    }

    @Test
    void readsWriteEmptyValues(@Mktmp final Path temp) {
        final Mono tabs = new MnTabs(temp.resolve("x.tabs"));
        final Collection<Map<String, String>> rows = tabs.read();
        final Map<String, String> row = new HashMap<>(0);
        row.put(Tojos.ID_KEY, "");
        rows.add(row);
        tabs.write(rows);
        MatcherAssert.assertThat(
            "reads back empty string",
            tabs.read().iterator().next().get(Tojos.ID_KEY),
            Matchers.equalTo("")
        );
    }

    @Test
    void savesEmptyRow(@Mktmp final Path temp) {
        final Mono tabs = new MnTabs(temp.resolve("e.tabs"));
        final Collection<Map<String, String>> rows = tabs.read();
        rows.add(new HashMap<>(0));
        rows.add(new HashMap<>(0));
        tabs.write(rows);
        MatcherAssert.assertThat(
            "reads back empty row",
            tabs.read().size(),
            Matchers.equalTo(2)
        );
    }

}
