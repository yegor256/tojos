/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import com.yegor256.Mktmp;
import com.yegor256.MktmpResolver;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
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
        final TjDefaultTest.Counted mono = new TjDefaultTest.Counted(new MnMemory());
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

    /**
     * A mono that remembers how many times it was read.
     * @since 1.0
     */
    private static final class Counted implements Mono {

        /**
         * The mono that does the work.
         */
        private final Mono origin;

        /**
         * How many times it was read.
         */
        private final AtomicInteger count;

        /**
         * Ctor.
         * @param mono The mono that does the work
         */
        Counted(final Mono mono) {
            this.origin = mono;
            this.count = new AtomicInteger();
        }

        @Override
        public Collection<Map<String, String>> read() {
            this.count.incrementAndGet();
            return this.origin.read();
        }

        @Override
        public void write(final Collection<Map<String, String>> rows) {
            this.origin.write(rows);
        }

        @Override
        public void close() throws IOException {
            this.origin.close();
        }

        /**
         * How many times this mono was read.
         * @return The number of reads
         */
        int reads() {
            return this.count.get();
        }
    }
}
