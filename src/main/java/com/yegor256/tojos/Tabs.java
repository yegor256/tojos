/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2021 Yegor Bugayenko
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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Text file where each line contains columns separated by tabs.
 *
 * <p>For example:
 *
 * <pre> id:Jeff%20Lebowski   salary: $5,000   age: 35
 * id:Walter%20Sobchak   salary: $4,000   age: 40
 * </pre>
 *
 * The class is NOT thread-safe.
 *
 * @since 0.7.0
 */
public final class Tabs implements Mono {

    /**
     * The file where to keep them.
     */
    private final Path file;

    /**
     * Ctor.
     *
     * @param path The path to the file
     * @since 0.4.0
     */
    public Tabs(final String path) {
        this(Paths.get(path));
    }

    /**
     * Ctor.
     *
     * @param path The path to the file
     * @since 0.4.0
     */
    public Tabs(final File path) {
        this(path.toPath());
    }

    /**
     * Ctor.
     *
     * <p>If the directory doesn't exist, it will automatically be created.
     *
     * @param path The path to the file
     */
    public Tabs(final Path path) {
        this.file = path;
    }

    @Override
    public String toString() {
        return this.file.toString();
    }

    @Override
    public Collection<Map<String, String>> read() {
        final Collection<Map<String, String>> rows = new LinkedList<>();
        if (Files.exists(this.file)) {
            final List<String> lines;
            try {
                lines = Files.readAllLines(
                    this.file, StandardCharsets.UTF_8
                );
            } catch (final IOException ex) {
                throw new IllegalArgumentException(
                    String.format("Failed to read JSON from '%s'", this.file),
                    ex
                );
            }
            for (final String line : lines) {
                final Map<String, String> row = new HashMap<>(1);
                final String[] cols = line.split("\t");
                for (final String part : cols) {
                    final String[] parts = part.split(":", 2);
                    row.put(Tabs.decode(parts[0]), Tabs.decode(parts[1]));
                }
                rows.add(row);
            }
        }
        return rows;
    }

    @Override
    public void write(final Collection<Map<String, String>> rows) {
        final Collection<String> lines = new ArrayList<>(rows.size());
        for (final Map<String, String> row : rows) {
            final Collection<String> cols = new ArrayList<>(row.size());
            for (final Map.Entry<String, String> ent : row.entrySet()) {
                cols.add(
                    String.format(
                        "%s:%s",
                        Tabs.encode(ent.getKey()),
                        Tabs.encode(ent.getValue())
                    )
                );
            }
            lines.add(String.join("\t", cols));
        }
        this.file.toFile().getParentFile().mkdirs();
        try {
            Files.write(this.file, lines, StandardCharsets.UTF_8);
        } catch (final IOException ex) {
            throw new IllegalArgumentException(
                String.format(
                    "Failed to write %d rows into '%s'",
                    rows.size(), this.file
                ),
                ex
            );
        }
    }

    /**
     * Encode.
     * @param txt The text to encode
     * @return Encoded
     */
    private static String encode(final String txt) {
        try {
            return URLEncoder.encode(txt, StandardCharsets.UTF_8.toString());
        } catch (final UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Decode.
     * @param txt The text to decode
     * @return Decoded
     */
    private static String decode(final String txt) {
        try {
            return URLDecoder.decode(txt, StandardCharsets.UTF_8.toString());
        } catch (final UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
