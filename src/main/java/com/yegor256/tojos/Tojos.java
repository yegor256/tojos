/*
 * The MIT License (MIT)
 *
 * SPDX-FileCopyrightText: Copyright (c) 2021-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
