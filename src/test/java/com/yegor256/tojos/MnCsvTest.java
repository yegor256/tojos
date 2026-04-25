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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test case for {@link MnCsv}.
 *
 * @since 0.3.0
 */
@ExtendWith(MktmpResolver.class)
final class MnCsvTest {

    @Test
    void readsEmptyFile(@Mktmp final Path temp) {
        MatcherAssert.assertThat(
            "must read empty collection from new file",
            new MnCsv(temp.resolve("foo/bar/empty.csv")).read().size(),
            Matchers.equalTo(0)
        );
    }

    @Test
    void writesAndReadsRow(@Mktmp final Path temp) {
        final Mono csv = new MnCsv(temp.resolve("foo/bar/a.csv"));
        final Collection<Map<String, String>> rows = csv.read();
        final Map<String, String> row = new HashMap<>(0);
        final String key = Tojos.ID_KEY;
        final String value = "привет,\t\012 \"друг\"!";
        row.put(key, value);
        rows.add(row);
        csv.write(rows);
        MatcherAssert.assertThat(
            "must read back written value",
            csv.read().iterator().next().get(key),
            Matchers.equalTo(value)
        );
    }

    @Test
    void ignoresEmptyElements(@Mktmp final Path temp) {
        final Mono csv = new MnCsv(temp.resolve("foo/bar/xx.csv"));
        final Collection<Map<String, String>> rows = csv.read();
        final Map<String, String> row = new HashMap<>(0);
        row.put("x", "1");
        row.put("y", "");
        rows.add(row);
        csv.write(rows);
        MatcherAssert.assertThat(
            "must work fine",
            csv.read().iterator().next().containsKey("y"),
            Matchers.equalTo(false)
        );
    }

    @Test
    void keepsBackslash(@Mktmp final Path temp) {
        final Mono csv = new MnCsv(temp.resolve("foo/bar/slash.csv"));
        final Collection<Map<String, String>> rows = csv.read();
        final Map<String, String> row = new HashMap<>(0);
        final String path = "\\my\\windows\\path\\to\\here";
        row.put("a", path);
        rows.add(row);
        csv.write(rows);
        MatcherAssert.assertThat(
            "must have the right element inside",
            csv.read().iterator().next().get("a"),
            Matchers.equalTo(path)
        );
    }

    @RepeatedTest(5)
    void writesWhileRowIsModifiedConcurrently(@Mktmp final Path temp) throws Exception {
        final Mono csv = new MnCsv(temp.resolve("concurrent.csv"));
        final Map<String, String> row = new HashMap<>(0);
        row.put(Tojos.ID_KEY, "row-1");
        for (int idx = 0; idx < 32; ++idx) {
            row.put(String.format("k%d", idx), String.format("v%d", idx));
        }
        final Collection<Map<String, String>> rows = new LinkedList<>();
        rows.add(row);
        final AtomicBoolean stop = new AtomicBoolean(false);
        final Thread modifier = new Thread(
            () -> {
                int idx = 0;
                while (!stop.get()) {
                    final String key = String.format("dyn-%d", idx);
                    row.put(key, Integer.toString(idx));
                    row.remove(key);
                    ++idx;
                }
            }
        );
        modifier.start();
        try {
            for (int idx = 0; idx < 256; ++idx) {
                csv.write(rows);
            }
        } finally {
            stop.set(true);
            modifier.join();
        }
        MatcherAssert.assertThat(
            "must persist the row without throwing ConcurrentModificationException",
            csv.read(),
            Matchers.iterableWithSize(1)
        );
    }

    @Test
    void putsKeyFirst(@Mktmp final Path temp) throws IOException {
        final Path path = temp.resolve("key-test.json");
        final Mono csv = new MnCsv(path);
        final Collection<Map<String, String>> rows = csv.read();
        final Map<String, String> row = new HashMap<>(0);
        row.put(Tojos.ID_KEY, "xyz");
        row.put("_x", "");
        row.put("zzzz", "");
        rows.add(row);
        csv.write(rows);
        MatcherAssert.assertThat(
            "must work fine",
            new String(Files.readAllBytes(path), StandardCharsets.UTF_8),
            Matchers.matchesPattern(
                Pattern.compile(
                    String.format("^\"%s\",.*", Tojos.ID_KEY),
                    Pattern.MULTILINE | Pattern.DOTALL
                )
            )
        );
    }
}
