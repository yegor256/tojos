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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The cached wrapper around Tojo class.
 *
 * <p>
 *   Caches simple tojo keys and values in order to avoid excessive
 *   reading from filesystem or any other expensive resource.
 * </p>
 * <p>
 *   This class is NOT thread-safe.
 * </p>
 * @since 0.18
 */
public final class CachedTojo implements Tojo {


    /**
     * The original tojo.
     */
    private final Tojo origin;

    /**
     * Cached keys and values.
     */
    private final Map<String, String> cache;

    /**
     * Constructor.
     * @param tojo The original tojo.
     */
    CachedTojo(final Tojo tojo) {
        this(tojo, new HashMap<>(tojo.toMap()));
    }

    /**
     * Constructor.
     * @param tojo The original tojo
     * @param cache Cache container.
     */
    private CachedTojo(
        final Tojo tojo,
        final Map<String, String> cache
    ) {
        this.origin = tojo;
        this.cache = cache;
    }

    @Override
    public boolean exists(final String key) {
        return this.cache.containsKey(key);
    }

    @Override
    public String get(final String key) {
        return this.cache.get(key);
    }

    @Override
    public Tojo set(final String key, final Object value) {
        this.origin.set(key, value);
        this.cache.put(key, String.valueOf(value));
        return this;
    }

    @Override
    public Map<String, String> toMap() {
        return Collections.unmodifiableMap(this.cache);
    }
}
