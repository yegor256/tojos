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
