package com.yegor256.tojos;

import java.io.*;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * YAML file
 */
public final class Yaml implements Mono {
    private final File destination;

    /**
     * Ctor.
     * @param file Destination where to keep the data
     */
    public Yaml(final File file) {
        this.destination = file;
    }

    /**
     * Ctor.
     * @param path Path to the file
     */
    public Yaml(final Path path) {
        this(path.toFile());
    }

    /**
     * Ctor.
     * @param path Path to the file
     */
    public Yaml(final String path) {
        this(new File(path));
    }

    @Override
    public Collection<Map<String, String>> read() {
        if (!this.destination.exists()) {
            return new LinkedList<>();
        }
        try {
            final InputStream source = new FileInputStream(this.destination);
            return new org.yaml.snakeyaml.Yaml().<List<Map<String, String>>>load(source);
        } catch (final IOException e) {
            throw new IllegalArgumentException(
                String.format("Failed to read YAML from '%s'", this.destination)
            );
        }
    }

    @Override
    public void write(final Collection<Map<String, String>> rows) {
        try {
            if (!destination.exists()) {
                if (!destination.createNewFile()) {
                    throw new IOException(
                        String.format("Failed to create file '%s'", this.destination)
                    );
                }
            }
            new org.yaml.snakeyaml.Yaml().dump(
                rows,
                new PrintWriter(this.destination)
            );
        } catch (IOException e) {
            throw new IllegalArgumentException(
                String.format("Failed to write YAML to '%s'", this.destination),
                e
            );
        }
    }
}
