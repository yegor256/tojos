/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The wrapper which caches underlying tojos.
 *
 * <p>This class is NOT thread-safe.</p>
 *
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
    private final Map<String, Tojo> cache;

    /**
     * Ctor.
     * @param tojos Tojos which need to be cached
     */
    public TjCached(final Tojos tojos) {
        this(tojos, new HashMap<>(0));
    }

    /**
     * Ctor.
     * @param origin Tojos which need to be cached
     * @param cache Cache container for tojos
     */
    public TjCached(
        final Tojos origin,
        final Map<String, Tojo> cache
    ) {
        this.origin = origin;
        this.cache = cache;
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    public Tojo add(final String name) {
        final Tojo tojo = new ToCached(this.origin.add(name));
        this.cache.put(name, tojo);
        return tojo;
    }

    @Override
    public List<Tojo> select(final Predicate<Tojo> filter) {
        if (this.cache.isEmpty()) {
            this.fill();
        }
        return this.cache.values()
            .stream()
            .filter(filter)
            .collect(Collectors.toList());
    }

    @Override
    public void close() throws IOException {
        this.origin.close();
    }

    /**
     * Fill cache with all tojos.
     */
    private void fill() {
        this.cache.putAll(
            this.origin.select(x -> true)
                .stream()
                .map(ToCached::new)
                .collect(
                    Collectors.toMap(
                        x -> x.get(Tojos.ID_KEY),
                        x -> x,
                        (x, y) -> x
                    )
                )
        );
    }
}
