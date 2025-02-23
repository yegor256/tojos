/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The cached wrapper around Tojo class.
 *
 * <p>Caches simple tojo keys and values in order to avoid excessive
 * reading from filesystem or any other expensive resource.</p>
 *
 * <p>This class is NOT thread-safe.</p>
 *
 * @since 0.18
 */
public final class ToCached implements Tojo {

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
    ToCached(final Tojo tojo) {
        this(tojo, new HashMap<>(tojo.toMap()));
    }

    /**
     * Constructor.
     * @param tojo The original tojo
     * @param cache Cache container.
     */
    private ToCached(
        final Tojo tojo,
        final Map<String, String> cache
    ) {
        this.origin = tojo;
        this.cache = cache;
    }

    @Override
    public String toString() {
        return this.origin.toString();
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
