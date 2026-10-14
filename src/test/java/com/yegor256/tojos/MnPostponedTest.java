/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import com.yegor256.Mktmp;
import com.yegor256.MktmpResolver;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test case for {@link TjDefault}.
 *
 * @since 0.3.0
 */
@ExtendWith(MktmpResolver.class)
final class MnPostponedTest {

    @Test
    void writesMassively(@Mktmp final Path temp) throws Exception {
        final Mono mono = new MnJson(temp.resolve("big-data.json"));
        final Tojos tojos = new TjDefault(new MnPostponed(mono, 500L));
        final int total = 200;
        for (int idx = 0; idx < total; ++idx) {
            tojos.add(String.format("key-%d", idx))
                .set(String.format("k%d", idx), String.format("v%d", idx));
        }
        tojos.close();
        MatcherAssert.assertThat(
            "must persist all entries to disk",
            new TjDefault(mono).select(r -> true),
            Matchers.iterableWithSize(total)
        );
    }

    @Test
    void avoidsSavingAfterDestruction(@Mktmp final Path temp) {
        MatcherAssert.assertThat(
            "must add entry without saving to disk immediately",
            new TjDefault(
                new MnPostponed(
                    new MnJson(temp.resolve("x/y/z/data.json")),
                    500L
                )
            ).add("hello"),
            Matchers.notNullValue()
        );
    }

    @Test
    void stopsFlushingThreadWhenFinalWriteFails() throws Exception {
        final AtomicReference<Mono> postponed = new AtomicReference<>();
        final CompletableFuture<Thread> flushing = new CompletableFuture<>();
        postponed.set(
            new MnPostponed(
                new Mono() {
                    @Override
                    public Collection<Map<String, String>> read() {
                        return new ArrayList<>(0);
                    }

                    @Override
                    public void write(final Collection<Map<String, String>> rows) {
                        if (flushing.isDone()) {
                            throw new IllegalStateException("disk is full");
                        }
                        MnPostponedTest.stall(postponed.get(), rows, flushing);
                    }

                    @Override
                    public void close() {
                        // nothing to close here
                    }
                },
                1L
            )
        );
        postponed.get().write(new ArrayList<>(0));
        try {
            final Thread flush = flushing.get(5L, TimeUnit.SECONDS);
            Assertions.assertThrows(
                IllegalStateException.class,
                postponed.get()::close,
                "must propagate the failure of the final write"
            );
            flush.join(TimeUnit.SECONDS.toMillis(5L));
            MatcherAssert.assertThat(
                "must stop the flushing thread even if the final write fails",
                flush.isAlive(),
                Matchers.is(false)
            );
        } finally {
            flushing.thenAccept(Thread::interrupt).get(5L, TimeUnit.SECONDS);
        }
    }

    private static void stall(final Mono mono, final Collection<Map<String, String>> rows,
        final CompletableFuture<Thread> flushing) {
        mono.write(rows);
        flushing.complete(Thread.currentThread());
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
