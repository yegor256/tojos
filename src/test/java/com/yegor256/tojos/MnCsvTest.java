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

import com.yegor256.Mktmp;
import com.yegor256.MktmpResolver;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
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
    void checksSimpleScenario(@Mktmp final Path temp) {
        final Mono csv = new MnCsv(temp.resolve("foo/bar/a.csv"));
        final Collection<Map<String, String>> rows = csv.read();
        MatcherAssert.assertThat(
                "must work fine",
                csv.read().size(),
                Matchers.equalTo(0)
        );
        final Map<String, String> row = new HashMap<>(0);
        final String key = Tojos.ID_KEY;
        final String value = "привет,\t\n \"друг\"!";
        row.put(key, value);
        rows.add(row);
        csv.write(rows);
        MatcherAssert.assertThat(
                "must work fine",
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

    @Test
    public void testConcurrentModificationBugInDupMethod() throws InterruptedException, NoSuchMethodException {
        Collection<Map<String, String>> rows = new ArrayList<>();
        AtomicReference<Throwable> exceptionRef = new AtomicReference<>();

        Method dupMethod = MnCsv.class.getDeclaredMethod("dup", Collection.class);
        dupMethod.setAccessible(true);

        Thread addThead = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                rows.add(new HashMap<>(Map.of("key" + i, "value" + i)));
            }
        });

        Thread dupThead = new Thread(() -> {
            try {
                Thread.sleep(10);
                dupMethod.invoke(null, rows);
            } catch (Exception e) {
                exceptionRef.set(e);
            }
        });

        Thread removeThread = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                int finalI = i;
                rows.removeIf(map -> map.containsKey("key" + finalI));
            }
        });

        addThead.start();
        dupThead.start();
        removeThread.start();

        addThead.join();
        dupThead.join();
        removeThread.join();

        if (exceptionRef.get() != null) {
            Throwable exception = exceptionRef.get();
            Assertions.fail("Test failed due to exception: " + exception.getClass().getSimpleName(), exception);
        }
    }

}
