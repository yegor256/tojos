/*
 * The MIT License (MIT)
 *
 * SPDX-FileCopyrightText: Copyright (c) 2021-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In memory {@link Mono}.
 *
 * <p>The class is thread-safe.</p>
 *
 * @since 0.12.0
 */
public final class MnMemory implements Mono {

    /**
     * The list of rows.
     */
    private final Collection<Map<String, String>> mem = new CopyOnWriteArrayList<>();

    @Override
    public Collection<Map<String, String>> read() {
        final Collection<Map<String, String>> list = new ArrayList<>(this.mem.size());
        list.addAll(this.mem);
        return list;
    }

    @Override
    public void write(final Collection<Map<String, String>> rows) {
        this.mem.clear();
        this.mem.addAll(rows);
    }

    @Override
    public void close() {
        // nothing to close here
    }
}
