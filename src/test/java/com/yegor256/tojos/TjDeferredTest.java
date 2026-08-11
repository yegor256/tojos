/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import com.yegor256.Mktmp;
import com.yegor256.MktmpResolver;
import java.io.IOException;
import java.nio.file.Path;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test case for {@link TjDeferred}.
 * @since 1.0
 */
@ExtendWith(MktmpResolver.class)
final class TjDeferredTest {

    @Test
    void readsMonoOnceWhileBuilding() throws IOException {
        final MnCounted mono = new MnCounted(new MnMemory());
        try (Tojos tojos = new TjDeferred(mono)) {
            for (int row = 0; row < 20; row = row + 1) {
                tojos.add(String.format("row-%d", row)).set("k", "v").set("j", "w");
            }
        }
        MatcherAssert.assertThat(
            "must read the mono once, however many rows are built",
            mono.reads(),
            Matchers.equalTo(1)
        );
    }

    @Test
    void writesMonoOnceWhenClosed() throws IOException {
        final MnCounted mono = new MnCounted(new MnMemory());
        try (Tojos tojos = new TjDeferred(mono)) {
            for (int row = 0; row < 20; row = row + 1) {
                tojos.add(String.format("row-%d", row)).set("k", "v");
            }
            MatcherAssert.assertThat(
                "must keep the rows in memory until closed",
                mono.writes(),
                Matchers.equalTo(0)
            );
        }
    }

    @Test
    void keepsCellsUntilTheyAreRead() throws IOException {
        try (Tojos tojos = new TjDeferred(new MnMemory())) {
            tojos.add("book").set("title", "Object Thinking");
            MatcherAssert.assertThat(
                "must remember what was put into a row",
                tojos.select(row -> row.exists("title")).iterator().next().get("title"),
                Matchers.equalTo("Object Thinking")
            );
        }
    }

    @Test
    void leavesRowsInMonoWhenClosed(@Mktmp final Path temp) throws IOException {
        final Mono mono = new MnCsv(temp.resolve("books.csv"));
        try (Tojos tojos = new TjDeferred(mono)) {
            tojos.add("one").set("k", "v1");
            tojos.add("two").set("k", "v2");
        }
        MatcherAssert.assertThat(
            "must write every row to the mono, but it didnt",
            new TjSmart(new TjDefault(mono)).size(),
            Matchers.equalTo(2)
        );
    }

    @Test
    void seesRowsThatWereAlreadyThere(@Mktmp final Path temp) throws IOException {
        final Mono mono = new MnCsv(temp.resolve("old.csv"));
        try (Tojos before = new TjDefault(mono)) {
            before.add("kettle").set("size", "big");
        }
        try (Tojos tojos = new TjDeferred(mono)) {
            MatcherAssert.assertThat(
                "must read what the mono already had",
                tojos.select(row -> true).iterator().next().get("size"),
                Matchers.equalTo("big")
            );
        }
    }

    @Test
    void savesUniqueIds() throws IOException {
        try (Tojos tojos = new TjDeferred(new MnMemory())) {
            tojos.add("only").set("k", "first");
            tojos.add("only").set("k", "second");
            MatcherAssert.assertThat(
                "must keep one row per id, but it made two",
                tojos.select(row -> true).size(),
                Matchers.equalTo(1)
            );
        }
    }

    @Test
    void closesMonoItWasGiven() throws IOException {
        final MnCounted mono = new MnCounted(new MnMemory());
        new TjDeferred(mono).close();
        MatcherAssert.assertThat(
            "must close the mono it was given, but it left it open",
            mono.closes(),
            Matchers.equalTo(1)
        );
    }

    @Test
    void forgetsCellItNeverHad() throws IOException {
        try (Tojos tojos = new TjDeferred(new MnMemory())) {
            MatcherAssert.assertThat(
                "must not claim a cell nobody wrote",
                tojos.add("empty").exists("k"),
                Matchers.is(false)
            );
        }
    }

    @Test
    void showsEveryCellOfRow() throws IOException {
        try (Tojos tojos = new TjDeferred(new MnMemory())) {
            MatcherAssert.assertThat(
                "must show the cells of a row, since that is what gets written",
                tojos.add("cup").set("size", "big").toMap(),
                Matchers.allOf(
                    Matchers.hasEntry("id", "cup"),
                    Matchers.hasEntry("size", "big")
                )
            );
        }
    }

    @Test
    void keepsMonoEmptyWhenNothingWasAsked(@Mktmp final Path temp) throws IOException {
        final Path path = temp.resolve("untouched.csv");
        new TjDeferred(new MnCsv(path)).close();
        MatcherAssert.assertThat(
            "must not write a table it was never asked about",
            path.toFile().exists(),
            Matchers.is(false)
        );
    }
}
