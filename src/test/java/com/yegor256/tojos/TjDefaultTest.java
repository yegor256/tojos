/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import com.yegor256.Mktmp;
import com.yegor256.MktmpResolver;
import java.nio.file.Path;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test case for {@link TjDefault}.
 * @since 0.3.0
 */
@ExtendWith(MktmpResolver.class)
final class TjDefaultTest {

    @ParameterizedTest
    @ValueSource(strings = {"a.csv", "a.json"})
    void checksSimpleScenario(final String file, @Mktmp final Path temp) {
        final Tojos tojos = new TjDefault(new MnCsv(temp.resolve(file)));
        tojos.add("foo").set("k", "v").set("a", "b");
        tojos.select(t -> t.exists("k")).iterator().next();
        MatcherAssert.assertThat(
            "must work fine",
            tojos.select(t -> t.exists("k")).iterator().next().get("a"),
            Matchers.equalTo("b")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"x.csv", "x.json"})
    void addsTojo(final String file, @Mktmp final Path temp) {
        final Tojos tojos = new TjDefault(new MnJson(temp.resolve(file)));
        tojos.add("foo-1");
        MatcherAssert.assertThat(
            "must work fine",
            new TjSmart(tojos).size(),
            Matchers.equalTo(1)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"y.csv", "y.json"})
    void savesUniqueIds(final String file, @Mktmp final Path temp) {
        final Tojos tojos = new TjDefault(new MnTabs(temp.resolve(file)));
        final String name = "foo11";
        tojos.add(name);
        tojos.add(name);
        MatcherAssert.assertThat(
            "must work fine",
            new TjSmart(tojos).size(),
            Matchers.equalTo(1)
        );
    }

    @Test
    void calculatesToString(@Mktmp final Path temp) {
        final Tojos tojos = new TjDefault(new MnTabs(temp.resolve("hello.csv")));
        tojos.add("foo-bar");
        MatcherAssert.assertThat(
            "must work fine",
            tojos.select(t -> true).iterator().next().toString(),
            Matchers.equalTo("foo-bar")
        );
    }

    @Test
    void readsMonoOnceWhileSelecting() {
        final MnCounted mono = new MnCounted(new MnMemory());
        final Tojos tojos = new TjDefault(mono);
        for (int row = 0; row < 10; row = row + 1) {
            tojos.add(String.format("row-%d", row)).set("k", "v");
        }
        final int before = mono.reads();
        tojos.select(t -> t.exists("k"));
        MatcherAssert.assertThat(
            "must read the mono once while selecting, however many rows it tests",
            mono.reads() - before,
            Matchers.equalTo(1)
        );
    }
}
