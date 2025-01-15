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
import java.util.Collections;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test case for {@link MnSynchronized}.
 *
 * @since 0.3.0
 */
@ExtendWith(MktmpResolver.class)
final class MnSynchronizedTest {

    @RepeatedTest(10)
    void writesConcurrently(@Mktmp final Path temp) {
        final Mono mono = new MnSynchronized(new MnJson(temp.resolve("bar/baz/a.json")));
        MatcherAssert.assertThat(
            "works without concurrency conflicts",
            new Together<>(
                thread -> {
                    mono.write(
                        Collections.nCopies(
                            thread + 1,
                            Collections.singletonMap(
                                Tojos.ID_KEY,
                                String.valueOf(10)
                            )
                        )
                    );
                    return mono.read().size();
                }
            ),
            Matchers.not(Matchers.hasItem(0))
        );
    }
}
