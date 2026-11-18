/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import com.yegor256.Mktmp;
import com.yegor256.MktmpResolver;
import com.yegor256.Together;
import java.nio.file.Path;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test case for {@link TjSynchronized}.
 *
 * @since 0.3.0
 */
@ExtendWith(MktmpResolver.class)
final class TjSynchronizedTest {

    @ParameterizedTest
    @ValueSource(strings = {"x.csv", "x.json"})
    void addsTojoParallel(final String file, @Mktmp final Path temp) {
        final Tojos tojos = new TjSynchronized(new TjDefault(new MnJson(temp.resolve(file))));
        final int threads = 50;
        MatcherAssert.assertThat(
            String.format("must add %d tojos in parallel into %s", threads, tojos),
            new Together<>(threads, thread -> tojos.add(Integer.toString(thread))),
            Matchers.iterableWithSize(threads)
        );
    }

    @RepeatedTest(10)
    void readsAndWritesConcurrentlyWithHighFrequency(@Mktmp final Path temp) {
        final Tojos tojos = new TjSynchronized(
            new TjDefault(
                new MnSynchronized(
                    new MnJson(temp.resolve("mono.json"))
                )
            )
        );
        MatcherAssert.assertThat(
            String.format("must work fine with %s", tojos),
            new Together<>(
                thread -> {
                    final Tojo tojo = tojos.add(UUID.randomUUID().toString());
                    final String key = "foo";
                    final String uuid = UUID.randomUUID().toString();
                    tojo.set(key, uuid);
                    tojo.get(key);
                    tojo.set(key, uuid);
                    tojo.get(key);
                    tojo.set(key, uuid);
                    tojo.get(key);
                    return 1;
                }
            ),
            Matchers.not(Matchers.hasItem(0))
        );
    }

    @Test
    void returnsItselfFromSingleSetCall(@Mktmp final Path temp) {
        final Tojos tojos = new TjSynchronized(
            new TjDefault(new MnJson(temp.resolve("chain.json")))
        );
        final Tojo alice = tojos.add("alice");
        MatcherAssert.assertThat(
            "set() must return the synchronized wrapper itself, not the raw tojo",
            alice.set("first", "1"),
            Matchers.sameInstance(alice)
        );
    }

    @Test
    void returnsItselfThroughChainedSetCalls(@Mktmp final Path temp) {
        final Tojos tojos = new TjSynchronized(
            new TjDefault(new MnJson(temp.resolve("chain.json")))
        );
        final Tojo alice = tojos.add("alice");
        MatcherAssert.assertThat(
            "a chain of set() calls must stay on the synchronized wrapper",
            alice.set("first", "1").set("second", "2"),
            Matchers.sameInstance(alice)
        );
    }

    @Test
    void keepsValuesThroughChainedSetCalls(@Mktmp final Path temp) {
        final Tojos tojos = new TjSynchronized(
            new TjDefault(new MnJson(temp.resolve("chain.json")))
        );
        final Tojo alice = tojos.add("alice");
        alice.set("first", "1").set("second", "2");
        MatcherAssert.assertThat(
            "all attributes must be visible after the chained set() calls",
            alice.get("second"),
            Matchers.equalTo("2")
        );
    }

    @Test
    void returnsItselfFromSetOnTojoFromSelect(@Mktmp final Path temp) {
        final Tojos tojos = new TjSynchronized(
            new TjDefault(new MnJson(temp.resolve("chain.json")))
        );
        tojos.add("alice");
        final Tojo found = tojos.select(tojo -> true).get(0);
        MatcherAssert.assertThat(
            "tojos from select() must stay synchronized while chained",
            found.set("first", "1").set("second", "2"),
            Matchers.sameInstance(found)
        );
    }

    @RepeatedTest(10)
    void chainedSetDoesNotLoseParallelAdds(@Mktmp final Path temp) {
        final Tojos tojos = new TjSynchronized(
            new TjDefault(new MnJson(temp.resolve("lost-update.json")))
        );
        final int threads = 20;
        this.runAddsAndChainedSets(tojos, tojos.add("shared"), threads);
        MatcherAssert.assertThat(
            "parallel adds must not be lost by chained set() calls on another tojo",
            tojos.select(tojo -> true),
            Matchers.iterableWithSize(threads + 1)
        );
    }

    @RepeatedTest(10)
    void chainedSetDoesNotLoseParallelWrites(@Mktmp final Path temp) {
        final Tojos tojos = new TjSynchronized(
            new TjDefault(new MnJson(temp.resolve("lost-writes.json")))
        );
        final int threads = 20;
        final Tojo shared = tojos.add("shared");
        this.runAddsAndChainedSets(tojos, shared, threads);
        MatcherAssert.assertThat(
            "all attributes written through the chain must survive",
            shared.toMap().size(),
            Matchers.equalTo(2 * threads + 1)
        );
    }

    private void runAddsAndChainedSets(
        final Tojos tojos,
        final Tojo shared,
        final int threads
    ) {
        new Together<>(
            threads,
            thread -> {
                tojos.add(String.format("t-%d", thread));
                shared.set(String.format("a-%d", thread), thread)
                    .set(String.format("b-%d", thread), thread);
                return thread;
            }
        ).asList();
    }
}
