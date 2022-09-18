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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * YAML file.
 * @since 0.9.2
 */
public final class MnYaml implements Mono {
    /**
     * Where the YAML is stored.
     */
    private final Path destination;

    /**
     * Ctor.
     * @param path Destination where to keep the data
     */
    public MnYaml(final Path path) {
        this.destination = path;
    }

    /**
     * Ctor.
     * @param path Path to the file
     */
    public MnYaml(final File path) {
        this(path.toPath());
    }

    /**
     * Ctor.
     * @param path Path to the file
     */
    public MnYaml(final String path) {
        this(new File(path));
    }

    @Override
    public String toString() {
        return this.destination.toString();
    }

    @Override
    public Collection<Map<String, String>> read() {
        final Collection<Map<String, String>> result = new LinkedList<>();
        try {
            final InputStream source = Files.newInputStream(this.destination);
            result.addAll(new org.yaml.snakeyaml.Yaml().<List<Map<String, String>>>load(source));
        } catch (final IOException exception) {
            throw new IllegalArgumentException(
                String.format("Failed to read YAML from '%s'", this.destination),
                exception
            );
        }
        return result;
    }

    @Override
    public void write(final Collection<Map<String, String>> rows) {
        try {
            new org.yaml.snakeyaml.Yaml().dump(
                rows,
                Files.newBufferedWriter(this.destination)
            );
        } catch (final IOException exception) {
            throw new IllegalArgumentException(
                String.format("Failed to write YAML to '%s'", this.destination),
                exception
            );
        }
    }
}
