/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A mono that remembers how many times it was read and written.
 * @since 1.0
 */
final class MnCounted implements Mono {

    /**
     * The mono that does the work.
     */
    private final Mono origin;

    /**
     * How many times it was read.
     */
    private final AtomicInteger reading;

    /**
     * How many times it was written.
     */
    private final AtomicInteger writing;

    /**
     * How many times it was closed.
     */
    private final AtomicInteger closing;

    /**
     * Ctor.
     * @param mono The mono that does the work
     */
    MnCounted(final Mono mono) {
        this.origin = mono;
        this.reading = new AtomicInteger();
        this.writing = new AtomicInteger();
        this.closing = new AtomicInteger();
    }

    @Override
    public Collection<Map<String, String>> read() {
        this.reading.incrementAndGet();
        return this.origin.read();
    }

    @Override
    public void write(final Collection<Map<String, String>> rows) {
        this.writing.incrementAndGet();
        this.origin.write(rows);
    }

    @Override
    public void close() throws IOException {
        this.closing.incrementAndGet();
        this.origin.close();
    }

    /**
     * How many times this mono was read.
     * @return The number of reads
     */
    int reads() {
        return this.reading.get();
    }

    /**
     * How many times this mono was written.
     * @return The number of writes
     */
    int writes() {
        return this.writing.get();
    }

    /**
     * How many times this mono was closed.
     * @return The number of closes
     */
    int closes() {
        return this.closing.get();
    }
}
