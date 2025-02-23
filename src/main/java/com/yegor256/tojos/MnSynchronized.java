/*
 * The MIT License (MIT)
 *
 * SPDX-FileCopyrightText: Copyright (c) 2021-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * This class is thread-safe.
 *
 * @since 0.3.0
 */
public final class MnSynchronized implements Mono {

    /**
     * The wrapped mono.
     */
    private final Mono origin;

    /**
     * Ctor.
     *
     * @param mono The mono
     */
    public MnSynchronized(final Mono mono) {
        this.origin = mono;
    }

    @Override
    public Collection<Map<String, String>> read() {
        synchronized (this.origin) {
            return this.origin.read();
        }
    }

    @Override
    public void write(final Collection<Map<String, String>> rows) {
        synchronized (this.origin) {
            this.origin.write(rows);
        }
    }

    @Override
    public void close() throws IOException {
        this.origin.close();
    }
}
