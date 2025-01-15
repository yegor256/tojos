/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021-2025 Yegor Bugayenko
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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

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
     * Flushing thread.
     */
    @SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField" })
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
        this.flush = MnPostponed.start(mono, this.mem, msec, this.dirty);
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
        synchronized (this.dirty) {
            this.mem.write(rows);
            this.dirty.set(true);
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
     * @return A writing thread
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    private static Thread start(final Mono main, final Mono cache,
        final long msec, final AtomicBoolean flag) {
        final Thread thr = new Thread(
            () -> {
                while (true) {
                    try {
                        Thread.sleep(msec);
                    } catch (final InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    synchronized (flag) {
                        if (flag.compareAndSet(true, false)) {
                            main.write(cache.read());
                        }
                    }
                }
            }
        );
        thr.setDaemon(false);
        thr.start();
        return thr;
    }

}
