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
