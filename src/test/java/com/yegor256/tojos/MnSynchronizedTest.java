/*
 * The MIT License (MIT)
 *
 * SPDX-FileCopyrightText: Copyright (c) 2021-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
