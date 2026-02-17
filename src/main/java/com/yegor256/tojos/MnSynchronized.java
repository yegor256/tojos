/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

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
     * Lock for synchronization.
     */
    private final ReentrantLock lock;

    /**
     * Ctor.
     *
     * @param mono The mono
     */
    public MnSynchronized(final Mono mono) {
        this.origin = mono;
        this.lock = new ReentrantLock();
    }

    @Override
    public Collection<Map<String, String>> read() {
        this.lock.lock();
        try {
            return this.origin.read();
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void write(final Collection<Map<String, String>> rows) {
        this.lock.lock();
        try {
            this.origin.write(rows);
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void close() throws IOException {
        this.origin.close();
    }
}
