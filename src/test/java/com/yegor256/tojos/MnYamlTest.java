/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2022 Yegor Bugayenko
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

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test case for {@link MnYaml}.
 * @since 0.9.2
 */
final class MnYamlTest {

    @Test
    void writesAndReads(@TempDir final Path temp) {
        final File file = temp.resolve("test.yml").toFile();
        final Map<String, String> keys = new HashMap<>();
        final List<Map<String, String>> tojo = new LinkedList<>();
        final String key = "key";
        tojo.add(keys);
        keys.put(key, "value");
        final Mono yaml = new MnYaml(file);
        yaml.write(tojo);
        MatcherAssert.assertThat(
            keys.get(key),
            Matchers.equalTo(
                yaml.read().stream().findFirst().get().get(key)
            )
        );
    }

    @Test
    void throwsWhenReadingFromResources() {
        Assertions.assertThrows(
                ClassCastException.class,
                () -> new MnYaml("src/test/resources/test.yml").read()
        );
    }
}
