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
