/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import com.yegor256.Mktmp;
import com.yegor256.MktmpResolver;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test case for {@link MnYaml}.
 * @since 0.9.2
 */
@ExtendWith(MktmpResolver.class)
final class MnYamlTest {

    @Test
    void writesAndReads(@Mktmp final Path temp) {
        final Map<String, String> keys = new HashMap<>();
        final List<Map<String, String>> tojo = new ArrayList<>(0);
        final String key = "key";
        tojo.add(keys);
        keys.put(key, "value");
        final Mono yaml = new MnYaml(temp.resolve("test.yml"));
        yaml.write(tojo);
        MatcherAssert.assertThat(
            "must work fine",
            keys.get(key),
            Matchers.equalTo(
                yaml.read().stream().findFirst().get().get(key)
            )
        );
    }

    @Test
    void throwsClearlyWhenYamlRootIsMap(@Mktmp final Path temp) throws Exception {
        final Path file = temp.resolve("map.yml");
        Files.write(
            file,
            String.format("parent:%n  child: value%n").getBytes(StandardCharsets.UTF_8)
        );
        MatcherAssert.assertThat(
            "the message must mention the file path",
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new MnYaml(file).read(),
                "must throw a clear IllegalArgumentException when the YAML root is a map"
            ).getMessage(),
            Matchers.containsString(file.toString())
        );
    }

    @Test
    void readsEmptyFileAsEmptyCollection(@Mktmp final Path temp) throws Exception {
        final Path file = temp.resolve("empty.yml");
        Files.write(file, new byte[0]);
        MatcherAssert.assertThat(
            "an empty YAML file must produce an empty collection, not throw NPE",
            new MnYaml(file).read(),
            Matchers.empty()
        );
    }
}
