/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import java.util.Collections;
import java.util.Map;

/**
 * One tojo, holding its own cells.
 *
 * <p>Unlike {@link ToMono}, which knows only an id and goes to the mono for
 * every cell it is asked about, this one is the row: reading a cell is a map
 * lookup and writing one touches nothing else.</p>
 *
 * <p>This class is NOT thread-safe.</p>
 *
 * @since 1.0
 */
final class ToRow implements Tojo {

    /**
     * The cells of this row, by name.
     */
    private final Map<String, String> cells;

    /**
     * Ctor.
     * @param row The cells of the row, by name
     */
    ToRow(final Map<String, String> row) {
        this.cells = row;
    }

    @Override
    public String toString() {
        return this.cells.get(Tojos.ID_KEY);
    }

    @Override
    public boolean exists(final String key) {
        return this.cells.containsKey(key);
    }

    @Override
    public String get(final String key) {
        final String value = this.cells.get(key);
        if (value == null) {
            throw new IllegalStateException(
                String.format(
                    "There is no '%s' key in the tojo id=%s, among %d keys: %s",
                    key, this, this.cells.size(), this.cells.keySet()
                )
            );
        }
        return value;
    }

    @Override
    public Tojo set(final String key, final Object value) {
        if (key.equals(Tojos.ID_KEY)) {
            throw new IllegalArgumentException(
                String.format(
                    "It's illegal to use #set() to change '%s' attribute",
                    Tojos.ID_KEY
                )
            );
        }
        this.cells.put(key, value.toString());
        return this;
    }

    @Override
    public Map<String, String> toMap() {
        return Collections.unmodifiableMap(this.cells);
    }
}
