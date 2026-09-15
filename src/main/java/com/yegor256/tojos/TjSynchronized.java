/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
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
     * Lock for synchronization.
     */
    private final ReentrantLock lock;

    /**
     * Ctor.
     *
     * @param tojos The tojos
     */
    public TjSynchronized(final Tojos tojos) {
        this.origin = tojos;
        this.lock = new ReentrantLock();
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    public Tojo add(final String name) {
        this.lock.lock();
        try {
            return new TjSynchronized.Synched(this.origin.add(name));
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public List<Tojo> select(final Predicate<Tojo> filter) {
        this.lock.lock();
        try {
            return this.origin.select(filter)
                .stream()
                .map(TjSynchronized.Synched::new)
                .collect(Collectors.toList());
        } finally {
            this.lock.unlock();
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
            TjSynchronized.this.lock.lock();
            try {
                return this.origin.toString();
            } finally {
                TjSynchronized.this.lock.unlock();
            }
        }

        @Override
        public boolean exists(final String key) {
            TjSynchronized.this.lock.lock();
            try {
                return this.origin.exists(key);
            } finally {
                TjSynchronized.this.lock.unlock();
            }
        }

        @Override
        public String get(final String key) {
            TjSynchronized.this.lock.lock();
            try {
                return this.origin.get(key);
            } finally {
                TjSynchronized.this.lock.unlock();
            }
        }

        @Override
        public Tojo set(final String key, final Object value) {
            TjSynchronized.this.lock.lock();
            try {
                return this.origin.set(key, value);
            } finally {
                TjSynchronized.this.lock.unlock();
            }
        }

        @Override
        public Map<String, String> toMap() {
            TjSynchronized.this.lock.lock();
            try {
                return this.origin.toMap();
            } finally {
                TjSynchronized.this.lock.unlock();
            }
        }
    }
}
