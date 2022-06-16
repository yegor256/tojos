package com.yegor256.tojos;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class YamlTest {

    @Test
    public void writesAndReads(@TempDir final Path temp) {
        final File file = temp.resolve("test.yml").toFile();
        final Map<String, String> keys = new HashMap<>();
        final List<Map<String, String>> tojo = new LinkedList<>();
        tojo.add(keys);
        keys.put("key", "value");
        final Mono yaml = new Yaml(file);
        yaml.write(tojo);
        MatcherAssert.assertThat(
            keys.get("key"),
            Matchers.equalTo(
                yaml.read().stream().findFirst().get().get("key")
            )
        );
    }
}
