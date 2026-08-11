/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * All Tojos of a {@link Mono}, kept in memory and keyed by their ids.
 *
 * <p>This decorator is for building a table. Every other route through the
 * library reads the whole table and searches it for a row whenever a cell is
 * written, so writing n rows costs n squared; here a row is found by its id
 * and a cell written touches that row alone, so it costs n. The mono is read
 * once, when the first row is asked for, and written once, when this object
 * is closed.</p>
 *
 * <p>You should use this one ONLY if you are sure that nobody else is
 * touching the mono, and only if you close it: what was added lives in
 * memory until then.</p>
 *
 * <p>The class is NOT thread-safe.</p>
 *
 * @since 1.0
 */
public final class TjDeferred implements Tojos {

    /**
     * The mono.
     */
    private final Mono mono;

    /**
     * The rows, by id, in the order they arrived.
     */
    private final Map<String, Map<String, String>> rows;

    /**
     * Is it the first time?
     */
    private final AtomicBoolean first;

    /**
     * Ctor.
     * @param mno The mono to read the rows from and write them back to
     */
    public TjDeferred(final Mono mno) {
        this.mono = mno;
        this.rows = new LinkedHashMap<>(0);
        this.first = new AtomicBoolean(true);
    }

    @Override
    public String toString() {
        return this.mono.toString();
    }

    @Override
    public Tojo add(final String name) {
        return new ToRow(
            this.load().computeIfAbsent(
                name,
                id -> new HashMap<>(Collections.singletonMap(Tojos.ID_KEY, id))
            )
        );
    }

    @Override
    public List<Tojo> select(final Predicate<Tojo> filter) {
        return this.load()
            .values()
            .stream()
            .map(ToRow::new)
            .filter(filter)
            .collect(Collectors.toList());
    }

    @Override
    public void close() throws IOException {
        if (!this.first.get()) {
            this.mono.write(this.rows.values());
        }
        this.mono.close();
    }

    /**
     * The rows, read from the mono the first time they are asked for.
     * @return The rows, by id
     */
    private Map<String, Map<String, String>> load() {
        if (this.first.compareAndSet(true, false)) {
            for (final Map<String, String> row : this.mono.read()) {
                this.rows.put(row.get(Tojos.ID_KEY), row);
            }
        }
        return this.rows;
    }
}
