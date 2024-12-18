/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2024 Yegor Bugayenko
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
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

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
            return new Synched(this.origin.add(name));
        }
    }

    @Override
    public List<Tojo> select(final Predicate<Tojo> filter) {
        synchronized (this.origin) {
            return this.origin.select(filter);
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
