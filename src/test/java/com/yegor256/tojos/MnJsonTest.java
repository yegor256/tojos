/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
    void readsEmptyFile(@Mktmp final Path temp) {
        MatcherAssert.assertThat(
            "must read empty collection from new file",
            new MnJson(temp.resolve("foo/bar/empty.json")).read().size(),
            Matchers.equalTo(0)
        );
    }

    @Test
    void writesAndReadsRow(@Mktmp final Path temp) {
        final Mono json = new MnJson(temp.resolve("foo/bar/a.json"));
        final Collection<Map<String, String>> rows = json.read();
        final Map<String, String> row = new HashMap<>(0);
        final String key = Tojos.ID_KEY;
        final String value = "привет,\t\r\n друг!";
        row.put(key, value);
        rows.add(row);
        json.write(rows);
        MatcherAssert.assertThat(
            "must read back written value",
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
        row.put(Tojos.ID_KEY, "hello, world!");
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
