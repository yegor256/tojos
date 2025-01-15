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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test case for {@link MnJson}.
 *
 * @since 0.3.0
 */
@ExtendWith(MktmpResolver.class)
final class MnJsonTest {

    @Test
    void checksSimpleScenario(@Mktmp final Path temp) {
        final Mono json = new MnJson(temp.resolve("foo/bar/a.json"));
        final Collection<Map<String, String>> rows = json.read();
        MatcherAssert.assertThat(
            "must work fine",
            json.read().size(),
            Matchers.equalTo(0)
        );
        final Map<String, String> row = new HashMap<>(0);
        final String key = Tojos.ID_KEY;
        final String value = "привет,\t\r\n друг!";
        row.put(key, value);
        rows.add(row);
        json.write(rows);
        MatcherAssert.assertThat(
            "must work fine",
            json.read().iterator().next().get(key),
            Matchers.equalTo(value)
        );
    }

    @Test
    void writesEmptyCollection(@Mktmp final Path temp) {
        final Mono json = new MnJson(temp.resolve("foo/bar/b.json"));
        json.write(Collections.emptyList());
        MatcherAssert.assertThat(
            "must be empty",
            json.read(),
            Matchers.empty()
        );
    }

    @Test
    void prints(@Mktmp final Path temp) throws IOException {
        final Path path = temp.resolve("z.json");
        final Mono json = new MnJson(path);
        final Collection<Map<String, String>> rows = json.read();
        final Map<String, String> row = new HashMap<>(0);
        final String key = Tojos.ID_KEY;
        final String value = "hello, world!";
        row.put(key, value);
        rows.add(row);
        rows.add(row);
        json.write(rows);
        MatcherAssert.assertThat(
            "must work fine",
            new String(Files.readAllBytes(path), StandardCharsets.UTF_8),
            Matchers.containsString("},\n")
        );
    }

    @Test
    void retrievesKeyAtFirstPosition(@Mktmp final Path temp) throws IOException {
        final Path path = temp.resolve("key-test.json");
        final Mono json = new MnJson(path);
        final Collection<Map<String, String>> rows = json.read();
        final Map<String, String> row = new HashMap<>(0);
        row.put(Tojos.ID_KEY, "xyz");
        row.put("_x", "");
        row.put("zzzz", "");
        rows.add(row);
        json.write(rows);
        MatcherAssert.assertThat(
            "must work fine",
            new String(Files.readAllBytes(path), StandardCharsets.UTF_8),
            Matchers.matchesPattern(
                Pattern.compile(
                    String.format(".*\\{\\s+\"%s\":.*", Tojos.ID_KEY),
                    Pattern.MULTILINE | Pattern.DOTALL
                )
            )
        );
    }

}
