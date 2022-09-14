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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Tojos thread-safe implementation.
 * <p>
 * The class is thread-safe.
 *
 * @since 1.0
 */
public class SynchronizedTojos implements Tojos {

    /**
     * The wrapped.
     */
    private final Tojos wrapped;

    /**
     * Ctor.
     *
     * @param wrapped The Tojos to decorate
     */
    public SynchronizedTojos(final Tojos wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * Get one tojo by ID.
     *
     * @param id The id
     * @return The tojo if found
     */
    public synchronized Tojo getById(final String id) {
        return Collections.synchronizedCollection(
            this.wrapped.select(tj -> true)
        )
            .stream()
            .filter(tojo -> Objects.equals(tojo.get(Tojos.KEY), id))
            .iterator()
            .next();
    }

    @Override
    public final synchronized Tojo add(final String id) {
        Collections.synchronizedCollection(
            this.wrapped.select(tj -> true)
        )
            .stream()
            .filter(tj -> Objects.equals(id, tj.get(Tojos.KEY)))
            .findFirst()
            .ifPresent(
                tj -> {
                    throw new IllegalStateException(
                        String.format(
                            "%s is already presented",
                            id
                        )
                    );
                }
            );
        return this.wrapped.add(id);
    }

    @Override
    public final List<Tojo> select(final Predicate<Tojo> filter) {
        return Collections.synchronizedCollection(
            this.wrapped.select(t -> true)
        )
            .stream()
            .filter(filter)
            .collect(Collectors.toList());
    }
}
