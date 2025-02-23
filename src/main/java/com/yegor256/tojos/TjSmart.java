/*
 * The MIT License (MIT)
 *
 * SPDX-FileCopyrightText: Copyright (c) 2021-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

/**
 * All file-objects.
 *
 * <p>The class is NOT thread-safe.</p>
 *
 * @since 0.3.0
 */
public final class TjSmart implements Tojos {

    /**
     * The original.
     */
    private final Tojos origin;

    /**
     * Ctor.
     *
     * @param tojos The origin
     */
    public TjSmart(final Tojos tojos) {
        this.origin = tojos;
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    /**
     * Get one tojo by ID.
     * @param name The id
     * @return The tojo if found
     */
    public Tojo getById(final String name) {
        return this.origin
            .select(tojo -> name.equals(tojo.get(Tojos.ID_KEY)))
            .iterator()
            .next();
    }

    /**
     * Get size.
     * @return Total count
     */
    public int size() {
        return this.origin.select(t -> true).size();
    }

    @Override
    public Tojo add(final String name) {
        return this.origin.add(name);
    }

    @Override
    public List<Tojo> select(final Predicate<Tojo> filter) {
        return this.origin.select(filter);
    }

    @Override
    public void close() throws IOException {
        this.origin.close();
    }
}
