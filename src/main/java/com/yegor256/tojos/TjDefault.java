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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * All Tojos in a {@link Mono}.
 *
 * The class is NOT thread-safe.
 *
 * @since 0.3.0
 */
public final class TjDefault implements Tojos {

    /**
     * The mono.
     */
    private final Mono mono;

    /**
     * Ctor.
     *
     * @param mno The Mono (CSV or JSON)
     */
    public TjDefault(final Mono mno) {
        this.mono = mno;
    }

    @Override
    public String toString() {
        return this.mono.toString();
    }

    @Override
    public Tojo add(final String name) {
        final Collection<Map<String, String>> rows = this.mono.read();
        final Optional<Map<String, String>> before = rows.stream().filter(
            r -> r.get(Tojos.ID_KEY).equals(name)
        ).findFirst();
        if (!before.isPresent()) {
            final Map<String, String> row = new HashMap<>(1);
            row.put(Tojos.ID_KEY, name);
            rows.add(row);
            this.mono.write(rows);
        }
        return new MonoTojo(this.mono, name);
    }

    @Override
    public List<Tojo> select(final Predicate<Tojo> filter) {
        final Collection<Map<String, String>> rows = this.mono.read();
        final List<Tojo> tojos = new ArrayList<>(rows.size());
        for (final Map<String, String> row : rows) {
            final Tojo tojo = new MonoTojo(this.mono, row.get(Tojos.ID_KEY));
            if (filter.test(tojo)) {
                tojos.add(tojo);
            }
        }
        return tojos;
    }

    @Override
    public void close() throws IOException {
        this.mono.close();
    }
}
