/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
        MatcherAssert.assertThat(
            "must work fine",
            new MnMemory().read().size(),
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
