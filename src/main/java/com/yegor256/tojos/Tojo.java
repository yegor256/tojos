/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import java.util.Map;

/**
 * One tojo.
 *
 * <p>It is expected that all implementations of this this interface
 * will return tojo ID from the {@link Tojo#toString()} method.</p>
 *
 * @since 0.3.0
 */
public interface Tojo {

    /**
     * The name of it.
     *
     * @return The value of its {@link Tojos#ID_KEY} attribute
     */
    String toString();

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
     * <p>You can't set {@link Tojos#ID_KEY} attribute. If you try to do
     * so, you will get a runtime exception.</p>
     *
     * @param key The name of the attribute
     * @param value The value
     * @return Itself
     */
    Tojo set(String key, Object value);

    /**
     * Get all attributes as a map.
     * @return The map of attributes.
     */
    Map<String, String> toMap();
}
