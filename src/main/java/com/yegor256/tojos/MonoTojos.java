/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2021 Yegor Bugayenko
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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * All tojos in a {@link Mono}.
 *
 * The class is NOT thread-safe.
 *
 * @since 0.12
 */
public final class MonoTojos implements Tojos {

    /**
     * The mono.
     */
    private final Mono mono;

    /**
     * Ctor.
     *
     * @param path The path to the file
     * @since 0.4.0
     */
    public MonoTojos(final String path) {
        this(Paths.get(path));
    }

    /**
     * Ctor.
     *
     * @param path The path to the file
     */
    public MonoTojos(final File path) {
        this(path.toPath());
    }

    /**
     * Ctor.
     *
     * @param path The path to the file
     * @checkstyle AvoidInlineConditionalsCheck (5 lines)
     */
    public MonoTojos(final Path path) {
        this(new Json(path));
    }

    /**
     * Ctor.
     *
     * @param mno The Mono (CSV or JSON)
     */
    public MonoTojos(final Mono mno) {
        this.mono = mno;
    }

    @Override
    public Tojo add(final String name) {
        final Collection<Map<String, String>> rows = this.mono.read();
        final Optional<Map<String, String>> before = rows.stream().filter(
            r -> r.get("id").equals(name)
        ).findFirst();
        if (!before.isPresent()) {
            final Map<String, String> row = new HashMap<>(1);
            row.put("id", name);
            rows.add(row);
            this.mono.write(rows);
        }
        return new MonoTojo(this.mono, name);
    }

    @Override
    public Collection<Tojo> select(final Function<Tojo, Boolean> filter) {
        final Collection<Map<String, String>> rows = this.mono.read();
        final Collection<Tojo> tojos = new ArrayList<>(rows.size());
        for (final Map<String, String> row : rows) {
            final Tojo tojo = new MonoTojo(this.mono, row.get("id"));
            if (filter.apply(tojo)) {
                tojos.add(tojo);
            }
        }
        return tojos;
    }
}
