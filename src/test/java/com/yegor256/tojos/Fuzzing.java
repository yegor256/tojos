/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.runner.RunWith;

/**
 * Fuzz testing for some classes.
 *
 * @since 0.7.0
 */
@RunWith(JQF.class)
@SuppressWarnings({"JTCOP.RuleAllTestsHaveProductionClass", "JTCOP.RuleCorrectTestName"})
public final class Fuzzing {

    @Fuzz
    public void fuzzMnTabs(final Collection<Map<String, String>> before) throws IOException {
        Fuzzing.assumeValid(before);
        final File temp = File.createTempFile(this.getClass().getCanonicalName(), "");
        final Mono tabs = new MnTabs(temp);
        tabs.write(before);
        final Collection<Map<String, String>> after = tabs.read();
        for (final Map<String, String> row : before) {
            MatcherAssert.assertThat(
                "must contain the same rows",
                after,
                Matchers.hasItem(row)
            );
        }
    }

    @Fuzz
    public void fuzzMnJson(final Collection<Map<String, String>> before) throws IOException {
        Fuzzing.assumeValid(before);
        final File temp = File.createTempFile(this.getClass().getCanonicalName(), "");
        final Mono tabs = new MnJson(temp);
        tabs.write(before);
        final Collection<Map<String, String>> after = tabs.read();
        for (final Map<String, String> row : before) {
            MatcherAssert.assertThat(
                "must contain the same rows",
                after,
                Matchers.hasItem(row)
            );
        }
    }

    private static void assumeValid(final Iterable<Map<String, String>> rows) {
        for (final Map<String, String> row : rows) {
            for (final Map.Entry<String, String> entry : row.entrySet()) {
                Assume.assumeFalse(entry.getKey() == null);
                Assume.assumeFalse(entry.getKey().isEmpty());
                Assume.assumeFalse(entry.getValue() == null);
            }
        }
    }
}
