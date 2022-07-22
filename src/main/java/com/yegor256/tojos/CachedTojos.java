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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The wrapper which caches underlying tojos.
 * Modify operation clears the cache.
<<<<<<< HEAD
 * @since 1.0
=======
 * This class is NOT thread-safe.
>>>>>>> 092c3da1070fedf4870d6b4b750bc1ab4752427c
 */
public final class CachedTojos implements Tojos {

    /**
     * Underlying tojos.
     */
    private final Tojos wrapped;

    /**
     * Cache for tojos.
     */
    private final List<Tojo> cache;

    /**
     * Ctor.
     * @param wrapped Tojos which need to be cached
     */
    public CachedTojos(final Tojos wrapped) {
        this.wrapped = wrapped;
        this.cache = new ArrayList<>(0);
    }

    @Override
    public Tojo add(final String name) {
        this.cache.clear();
        return this.wrapped.add(name);
    }

    @Override
    public List<Tojo> select(final Predicate<Tojo> filter) {
        if (this.cache.isEmpty()) {
            this.cache.addAll(this.wrapped.select(x -> true));
        }
        return this.cache.stream()
            .filter(filter)
            .collect(Collectors.toList());
    }
}
