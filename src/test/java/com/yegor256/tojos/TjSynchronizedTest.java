/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2025 Yegor Bugayenko
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

import com.yegor256.Mktmp;
import com.yegor256.MktmpResolver;
import com.yegor256.Together;
import java.nio.file.Path;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.RepeatedTest;
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
            "must work fine",
            new Together<>(
                threads,
                thread -> tojos.add(Integer.toString(thread))
            ),
            Matchers.iterableWithSize(threads)
        );
        for (int idx = 0; idx < threads; ++idx) {
            final int thread = idx;
            MatcherAssert.assertThat(
                "the tojo is present",
                tojos.select(t -> t.get(Tojos.ID_KEY).equals(Integer.toString(thread))),
                Matchers.iterableWithSize(1)
            );
        }
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
            "must work fine",
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
}
