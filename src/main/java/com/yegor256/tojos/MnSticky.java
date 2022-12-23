/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2022 Yegor Bugayenko
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

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This decorator prevents multiple reads.
 *
 * You should use this one ONLY if you are sure that nobody else
 * is touching the file/mono. Otherwise, there will be synchronization
 * issues.
 *
 * The class is NOT thread-safe.
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
        synchronized (this.mem) {
            if (this.first.compareAndSet(true, false)) {
                this.mem.write(this.origin.read());
            }
            return this.mem.read();
        }
    }

    @Override
    public void write(final Collection<Map<String, String>> rows) {
        synchronized (this.mem) {
            this.mem.write(rows);
            this.origin.write(rows);
        }
    }

    @Override
    public void close() throws IOException {
        this.origin.close();
    }
}
