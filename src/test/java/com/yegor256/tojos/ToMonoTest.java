/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021-2025 Yegor Bugayenko
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test case for MonoTojo usage in concurrent environment.
 *
 * @since 0.16
 */
@ExtendWith(MktmpResolver.class)
final class ToMonoTest {

    @Test
    void readsAndWrites(@Mktmp final Path temp) {
        final Mono mono = new MnJson(temp.resolve("mono.json"));
        new TjDefault(mono).add("foo");
        final Tojo tojo = new ToMono(mono, "foo");
        tojo.set("k", "v");
        MatcherAssert.assertThat(
            "reads back",
            tojo.get("k"),
            Matchers.equalTo("v")
        );
    }

    @Test
    void setsConcurrentlyToTheSameMono(@Mktmp final Path temp) {
        final Tojo tojo = new TjDefault(
            new MnJson(temp.resolve("mono.json"))
        ).add("foo");
        MatcherAssert.assertThat(
            "must work fine",
            new Together<>(
                thread -> {
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
