/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * One tojo in a {@link Mono}.
 *
 * <p>The class is thread-safe.</p>
 *
 * @since 0.3.0
 */
final class ToMono implements Tojo {

    /**
     * The file.
     */
    private final Mono mono;

    /**
     * The name.
     */
    private final String name;

    /**
     * Lock for synchronization.
     */
    private final ReentrantLock lock;

    /**
     * Ctor.
     *
     * @param mno The CSV
     * @param nme The name
     * @param lck Shared lock
     */
    ToMono(final Mono mno, final String nme, final ReentrantLock lck) {
        this.mono = mno;
        this.name = nme;
        this.lock = lck;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean exists(final String key) {
        this.lock.lock();
        try {
            return this.readMap(this.mono.read()).containsKey(key);
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public String get(final String key) {
        this.lock.lock();
        try {
            final Map<String, String> map = this.readMap(this.mono.read());
            final String value = map.get(key);
            if (value == null) {
                throw new IllegalStateException(
                    String.format(
                        "There is no '%s' key in the tojo id=%s, among %d keys: %s",
                        key, this.name, map.size(), map.keySet()
                    )
                );
            }
            return value;
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public Tojo set(final String key, final Object value) {
        this.lock.lock();
        try {
            if (key.equals(Tojos.ID_KEY)) {
                throw new IllegalArgumentException(
                    String.format(
                        "It's illegal to use #set() to change '%s' attribute",
                        Tojos.ID_KEY
                    )
                );
            }
            final Collection<Map<String, String>> rows = this.mono.read();
            final Map<String, String> row = this.readMap(rows);
            row.put(key, value.toString());
            this.mono.write(rows);
            return this;
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public Map<String, String> toMap() {
        return Collections.unmodifiableMap(this.readMap(this.mono.read()));
    }

    /**
     * Read the map from the collection.
     * @param rows The rows
     * @return The map
     */
    private Map<String, String> readMap(final Collection<Map<String, String>> rows) {
        return rows
            .stream()
            .filter(row -> row.get(Tojos.ID_KEY).equals(this.name))
            .findFirst()
            .orElseThrow(
                () -> new IllegalArgumentException(
                    String.format(
                        "The tojo with id='%s' not found among %d rows",
                        this.name, rows.size()
                    )
                )
            );
    }
}
