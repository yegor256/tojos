/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import java.io.Closeable;
import java.util.Collection;
import java.util.Map;

/**
 * Collection of rows, read/write.
 *
 * <p>Each row is a map of unique keys and their values. The rows
 * are either read or written all together. This is why the name
 * of the interface: "mono."</p>
 *
 * @since 0.3.0
 */
public interface Mono extends Closeable {

    /**
     * Read them all.
     *
     * <p>The list returned is modifiable.</p>
     *
     * @return The list of all lines
     */
    Collection<Map<String, String>> read();

    /**
     * Write them all back.
     * @param rows The list of all lines
     */
    void write(Collection<Map<String, String>> rows);

}
