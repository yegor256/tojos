/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

/**
 * YAML file.
 *
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
        final Collection<Map<String, String>> result = new ArrayList<>(0);
        try {
            result.addAll(
                new Yaml().<List<Map<String, String>>>load(
                    Files.newInputStream(this.destination)
                )
            );
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
            new Yaml().dump(
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

    @Override
    public void close() {
        // nothing to close here
    }

}
