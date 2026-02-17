/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

/**
 * All Tojos in a {@link Mono}.
 *
 * <p>The class is NOT thread-safe.</p>
 *
 * @since 0.3.0
 */
public final class TjDefault implements Tojos {

    /**
     * The mono.
     */
    private final Mono mono;

    /**
     * Shared lock for all ToMono instances.
     */
    private final ReentrantLock lock;

    /**
     * Ctor.
     *
     * @param mno The Mono (CSV or JSON)
     */
    public TjDefault(final Mono mno) {
        this.mono = mno;
        this.lock = new ReentrantLock();
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
        return new ToMono(this.mono, name, this.lock);
    }

    @Override
    public List<Tojo> select(final Predicate<Tojo> filter) {
        final Collection<Map<String, String>> rows = this.mono.read();
        final List<Tojo> tojos = new ArrayList<>(rows.size());
        for (final Map<String, String> row : rows) {
            final Tojo tojo = new ToMono(this.mono, row.get(Tojos.ID_KEY), this.lock);
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
