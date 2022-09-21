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

/**
 * One tojo.
 *
 * @since 0.3.0
 */
public interface Tojo {

    /**
     * This attribute exists.
     *
     * @param key The name of the attribute
     * @return TRUE if exists
     */
    boolean exists(String key);

    /**
     * Get attribute.
     *
     * @param key The name of the attribute
     * @return The value
     */
    String get(String key);

    /**
     * Set attribute.
     *
     * <p>You can't set {@link Tojos#KEY} attribute. If you try to do
     * so, you will get a runtime exception.</p>
     *
     * @param key The name of the attribute
     * @param value The value
     * @return Itself
     */
    Tojo set(String key, Object value);

}
