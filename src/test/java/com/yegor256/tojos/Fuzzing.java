/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
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
        final Mono tabs = new MnTabs(this.tempPath());
        tabs.write(before);
        for (final Map<String, String> row : before) {
            MatcherAssert.assertThat(
                "must contain the same rows",
                tabs.read(),
                Matchers.hasItem(row)
            );
        }
    }

    @Fuzz
    public void fuzzMnJson(final Collection<Map<String, String>> before) throws IOException {
        Fuzzing.assumeValid(before);
        final Mono json = new MnJson(this.tempPath());
        json.write(before);
        for (final Map<String, String> row : before) {
            MatcherAssert.assertThat(
                "must contain the same rows",
                json.read(),
                Matchers.hasItem(row)
            );
        }
    }

    /**
     * Make a temporary file path for fuzz tests.
     *
     * @return Path of a freshly-created temp file
     * @throws IOException If creating the file fails
     */
    private Path tempPath() throws IOException {
        return File.createTempFile(this.getClass().getCanonicalName(), "").toPath();
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
