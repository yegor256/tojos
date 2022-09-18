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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The wrapper which caches underlying tojos.
 * Modify operation clears the cache.
 * This class is NOT thread-safe.
 * @since 1.0
 */
public final class TjCached implements Tojos {

    /**
     * Underlying tojos.
     */
    private final Tojos origin;

    /**
     * Cache for tojos.
     */
    private final List<Tojo> cache;

    /**
     * Ctor.
     * @param tojos Tojos which need to be cached
     */
    public TjCached(final Tojos tojos) {
        this.origin = tojos;
        this.cache = new ArrayList<>(0);
    }

    @Override
    public Tojo add(final String name) {
        this.cache.clear();
        return this.origin.add(name);
    }

    @Override
    public List<Tojo> select(final Predicate<Tojo> filter) {
        if (this.cache.isEmpty()) {
            this.cache.addAll(this.origin.select(x -> true));
        }
        return this.cache.stream()
            .filter(filter)
            .collect(Collectors.toList());
    }

    @Override
    public void close() throws IOException {
        this.origin.close();
    }
}
