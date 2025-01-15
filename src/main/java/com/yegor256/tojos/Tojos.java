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
import java.util.List;
import java.util.function.Predicate;

/**
 * Text Object Java Object (TOJO) in a storage.
 *
 * <p>Use it like this:</p>
 *
 * <pre> Tojos tojos = new MonoTojos(new MnCsv("hello.csv"));
 * Tojo tojo = tojos.add("Jeff");
 * tojo.set("age", 35);
 * </pre>
 *
 * @since 0.3.0
 */
public interface Tojos extends Closeable {

    /**
     * Name of ID attribute in all tojos.
     */
    String ID_KEY = "id";

    /**
     * Add new tojo with the given ID.
     *
     * <p>If another tojo already exists with this ID, it will be returned.</p>
     *
     * @param name The ID of the tojo
     * @return The tojo created or found
     */
    Tojo add(String name);

    /**
     * Select some tojos.
     *
     * @param filter The filter
     * @return Collection of them
     */
    List<Tojo> select(Predicate<Tojo> filter);

}
