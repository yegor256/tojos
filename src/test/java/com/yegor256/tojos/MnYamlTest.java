/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import com.yegor256.Mktmp;
import com.yegor256.MktmpResolver;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
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
}
