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
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This decorator prevents multiple reads.
 *
 * <p>You should use this one ONLY if you are sure that nobody else
 * is touching the file/mono. Otherwise, there will be synchronization
 * issues.</p>
 *
 * <p>The class is NOT thread-safe.</p>
 *
 * @since 0.12.0
 */
public final class MnSticky implements Mono {

    /**
     * Original Mono.
     */
    private final Mono origin;

    /**
     * Cached rows.
     */
    private final MnMemory mem;

    /**
     * Is it the first time?
     */
    private final AtomicBoolean first;

    /**
     * Ctor.
     *
     * @param mono The original one
     */
    public MnSticky(final Mono mono) {
        this.origin = mono;
        this.mem = new MnMemory();
        this.first = new AtomicBoolean(true);
    }

    @Override
    public Collection<Map<String, String>> read() {
        if (this.first.compareAndSet(true, false)) {
            this.mem.write(this.origin.read());
        }
        return this.mem.read();
    }

    @Override
    public void write(final Collection<Map<String, String>> rows) {
        this.mem.write(rows);
        this.origin.write(rows);
    }

    @Override
    public void close() throws IOException {
        this.origin.close();
    }
}
