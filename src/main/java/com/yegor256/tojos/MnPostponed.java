/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This decorator prevents immediate write to the disk.
 *
 * <p>You should use this one ONLY if you are sure that nobody else
 * is touching the file/mono. Otherwise, there will be synchronization
 * issues.</p>
 *
 * <p>The class is thread-safe.</p>
 *
 * @since 0.12.0
 */
public final class MnPostponed implements Mono {

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
     * The cache is dirty and need to be flashed to the read mono?
     */
    private final AtomicBoolean dirty;

    /**
     * Lock for synchronization.
     */
    private final ReentrantLock lock;

    /**
     * Flushing thread.
     */
    @SuppressWarnings("PMD.UnusedPrivateField")
    private final Thread flush;

    /**
     * Ctor.
     *
     * @param mono The original one
     */
    public MnPostponed(final Mono mono) {
        this(mono, 100L);
    }

    /**
     * Ctor.
     *
     * @param mono The original one
     * @param msec Delay between write operations, in milliseconds
     */
    public MnPostponed(final Mono mono, final long msec) {
        this.origin = mono;
        this.mem = new MnMemory();
        this.first = new AtomicBoolean(true);
        this.dirty = new AtomicBoolean(false);
        this.lock = new ReentrantLock();
        this.flush = MnPostponed.start(mono, this.mem, msec, this.dirty, this.lock);
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
        this.lock.lock();
        try {
            this.mem.write(rows);
            this.dirty.set(true);
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void close() {
        if (this.dirty.compareAndSet(true, false)) {
            this.origin.write(this.mem.read());
        }
    }

    /**
     * Make a thread that writes.
     *
     * @param main The main one
     * @param cache The cache
     * @param msec Delay between write operations, in milliseconds
     * @param flag Is it required to flush?
     * @param lck Lock for synchronization
     * @return A writing thread
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    private static Thread start(final Mono main, final Mono cache,
        final long msec, final AtomicBoolean flag, final ReentrantLock lck) {
        final Thread thread = new Thread(
            () -> {
                while (true) {
                    try {
                        Thread.sleep(msec);
                    } catch (final InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    lck.lock();
                    try {
                        if (flag.compareAndSet(true, false)) {
                            main.write(cache.read());
                        }
                    } finally {
                        lck.unlock();
                    }
                }
            }
        );
        thread.setDaemon(false);
        thread.start();
        return thread;
    }

}
