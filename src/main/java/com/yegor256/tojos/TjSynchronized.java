/*
 * The MIT License (MIT)
 *
 * SPDX-FileCopyrightText: Copyright (c) 2021-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Thread-safe version of {@link Tojos}.
 *
 * <p>The class is thread-safe.</p>
 *
 * @since 0.3.0
 */
public final class TjSynchronized implements Tojos {

    /**
     * The wrapped {@link Tojos}.
     */
    private final Tojos origin;

    /**
     * Ctor.
     *
     * @param tojos The tojos
     */
    public TjSynchronized(final Tojos tojos) {
        this.origin = tojos;
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    public Tojo add(final String name) {
        synchronized (this.origin) {
            return new TjSynchronized.Synched(this.origin.add(name));
        }
    }

    @Override
    public List<Tojo> select(final Predicate<Tojo> filter) {
        synchronized (this.origin) {
            return this.origin.select(filter)
                .stream()
                .map(TjSynchronized.Synched::new)
                .collect(Collectors.toList());
        }
    }

    @Override
    public void close() throws IOException {
        this.origin.close();
    }

    /**
     * Synchronized tojo.
     *
     * @since 0.19.0
     */
    private final class Synched implements Tojo {

        /**
         * The wrapped {@link Tojo}.
         */
        private final Tojo origin;

        /**
         * Ctor.
         *
         * @param tojo The tojo
         */
        Synched(final Tojo tojo) {
            this.origin = tojo;
        }

        @Override
        public String toString() {
            synchronized (TjSynchronized.this.origin) {
                return this.origin.toString();
            }
        }

        @Override
        public boolean exists(final String key) {
            synchronized (TjSynchronized.this.origin) {
                return this.origin.exists(key);
            }
        }

        @Override
        public String get(final String key) {
            synchronized (TjSynchronized.this.origin) {
                return this.origin.get(key);
            }
        }

        @Override
        public Tojo set(final String key, final Object value) {
            synchronized (TjSynchronized.this.origin) {
                return this.origin.set(key, value);
            }
        }

        @Override
        public Map<String, String> toMap() {
            synchronized (TjSynchronized.this.origin) {
                return this.origin.toMap();
            }
        }
    }
}
