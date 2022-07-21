package com.yegor256.tojos;

import java.nio.file.Path;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test for cached tojos. Not thread-safe.
 */
class CachedTojosTest {

    @Test
    void testAddClearsCache(@TempDir final Path temp) {
        final Tojos tojos = new MonoTojos(new Csv(temp.resolve("my-tojos.csv")));
        tojos.add("A").set("k1", "v1").set("k2", "vv1");
        tojos.add("B").set("k1", "v2").set("k2", "vv2");
        tojos.add("C").set("k1", "v3").set("k2", "vv3");
        final Tojos cached = new CachedTojos(tojos);
        cached.select(x -> true);
        cached.add("D").set("k1", "v4").set("k2", "vv4");
        MatcherAssert.assertThat(
            cached.select(x -> true).size(),
            Matchers.equalTo(4)
        );
    }

    @Test
    void testSelectFromCached(@TempDir final Path temp) {
        final Tojos tojos = new MonoTojos(new Csv(temp.resolve("my-tojos.csv")));
        tojos.add("A").set("k1", "v1").set("k2", "vv1");
        tojos.add("B").set("k1", "v2").set("k2", "vv2");
        tojos.add("C").set("k1", "v3").set("k2", "vv3");
        final Tojos cached = new CachedTojos(tojos);
        cached.select(x -> true);
        tojos.add("D").set("k1", "v4").set("k2", "vv4");
        MatcherAssert.assertThat(
            cached.select(x -> true).size(),
            Matchers.equalTo(3)
        );
    }
}