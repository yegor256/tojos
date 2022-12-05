/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2022 Yegor Bugayenko
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

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test case for MonoTojo usage in concurrent environment.
 *
 * @since 0.16
 */
class MonoTojoTest {

    /**
     * Number of threads.
     */
    private static final int N_THREADS = 10;

    /**
     * Tasks executor - emulates concurrent environment.
     */
    private final ExecutorService service = Executors.newFixedThreadPool(MonoTojoTest.N_THREADS);

    /**
     * All tojos.
     */
    private TjDefault tojos;

    /**
     * Shared mono.
     */
    private MnJson mono;

    @BeforeEach
    void setUp(@TempDir final Path temp) {
        this.mono = new MnJson(temp.resolve("mono.json"));
        this.tojos = new TjDefault(this.mono);
    }

    @Test
    void setsConcurrentlyToTheSameMono() throws InterruptedException {
        this.service.invokeAll(
            IntStream.range(0, MonoTojoTest.N_THREADS)
                .mapToObj(String::valueOf)
                .map(this.tojos::add)
                .map(
                    tojo -> Executors.callable(
                        () -> {
                            tojo.set(tojo.toString(), tojo.toString());
                        }
                    )
                )
                .collect(Collectors.toList())
        );
        this.service.shutdown();
        this.service.awaitTermination(20, TimeUnit.SECONDS);
        final Collection<Map<String, String>> result = this.mono.read();
        MatcherAssert.assertThat(result, Matchers.hasSize(MonoTojoTest.N_THREADS));
        for (final Map<String, String> tojo : result) {
            MatcherAssert.assertThat(tojo.entrySet(), Matchers.hasSize(2));
        }
    }
}
