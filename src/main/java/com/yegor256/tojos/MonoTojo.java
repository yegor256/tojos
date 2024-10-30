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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * One tojo in a {@link Mono}.
 *
 * The class is thread-safe.
 *
 * @since 0.3.0
 */
final class MonoTojo implements Tojo {

    /**
     * The file.
     */
    private final Mono mono;

    /**
     * The name.
     */
    private final String name;

    /**
     * Ctor.
     *
     * @param mno The CSV
     * @param nme The name
     */
    MonoTojo(final Mono mno, final String nme) {
        this.mono = mno;
        this.name = nme;
    }

    @Override
    public boolean exists(final String key) {
        synchronized (this.mono) {
            return this.mono.read().stream()
                .filter(row -> row.get(Tojos.ID_KEY).equals(this.name))
                .findFirst()
                .get()
                .containsKey(key);
        }
    }

    @Override
    public String get(final String key) {
        synchronized (this.mono) {
            final String value = this.mono.read().stream()
                .filter(row -> row.get(Tojos.ID_KEY).equals(this.name))
                .findFirst()
                .get()
                .get(key);
            if (value == null) {
                throw new IllegalStateException(
                    String.format(
                        "There is no '%s' key in the tojo", key
                    )
                );
            }
            return value;
        }
    }

    @Override
    public Tojo set(final String key, final Object value) {
        synchronized (this.mono) {
            if (key.equals(Tojos.ID_KEY)) {
                throw new IllegalArgumentException(
                    String.format(
                        "It's illegal to use #set() to change '%s' attribute",
                        Tojos.ID_KEY
                    )
                );
            }
            final Collection<Map<String, String>> rows = this.mono.read();
            final Map<String, String> row = rows.stream()
                .filter(r -> r.get(Tojos.ID_KEY).equals(this.name))
                .findFirst()
                .orElseThrow(NotFoundException::new);
            row.put(key, value.toString());
            this.mono.write(rows);
            return this;
        }
    }

    @Override
    public Map<String, String> toMap() {
        return Collections.unmodifiableMap(
            this.mono.read().stream()
                .filter(row -> row.get(Tojos.ID_KEY).equals(this.name))
                .findFirst().orElseThrow(NotFoundException::new)
        );
    }

    /**
     * Not found exception.
     *
     * @since 0.19.0
     */
    private final class NotFoundException extends IllegalStateException {

        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = 0x7529FAFEL;

        /**
         * Ctor.
         */
        private NotFoundException() {
            super(String.format("The tojo with id='%s' not found", MonoTojo.this.name));
        }
    }
}
