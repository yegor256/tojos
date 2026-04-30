/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;

/**
 * JSON file.
 *
 * <p>The class is NOT thread-safe.</p>
 *
 * @since 0.3.0
 */
public final class MnJson implements Mono {

    /**
     * The factory of writers.
     */
    private static final JsonWriterFactory JWF = MnJson.factory();

    /**
     * The file path.
     */
    private final Path file;

    /**
     * Ctor.
     * @param path The path to the file
     * @since 0.4.0
     */
    public MnJson(final Path path) {
        this.file = path;
    }

    @Override
    public String toString() {
        return this.file.toString();
    }

    @Override
    public Collection<Map<String, String>> read() {
        final List<Map<String, String>> rows = new ArrayList<>(0);
        if (this.file.toFile().exists()) {
            try (JsonReader json = Json.createReader(Files.newBufferedReader(this.file))) {
                json
                    .readArray()
                    .stream()
                    .map(MnJson::asMap)
                    .forEach(rows::add);
            } catch (final IOException ex) {
                throw new IllegalArgumentException(
                    String.format("Failed to read JSON from '%s'", this.file),
                    ex
                );
            }
        }
        return rows;
    }

    @Override
    public void write(final Collection<Map<String, String>> rows) {
        final JsonArrayBuilder array = Json.createArrayBuilder();
        for (final Map<String, String> row : rows) {
            final JsonObjectBuilder obj = Json.createObjectBuilder();
            if (row.containsKey(Tojos.ID_KEY)) {
                obj.add(Tojos.ID_KEY, row.get(Tojos.ID_KEY));
            }
            for (final Map.Entry<String, String> ent : row.entrySet()) {
                if (ent.getKey().equals(Tojos.ID_KEY)) {
                    continue;
                }
                obj.add(ent.getKey(), ent.getValue());
            }
            array.add(obj);
        }
        this.file.toFile().getParentFile().mkdirs();
        try (JsonWriter json = MnJson.JWF.createWriter(MnJson.bufferedWriterTo(this.file))) {
            json.write(array.build());
        }
    }

    @Override
    public void close() {
        // nothing to close here
    }

    /**
     * Covert JsonValue to Map.
     * @param value Value
     * @return Map of Strings
     */
    private static Map<String, String> asMap(final JsonValue value) {
        return value.asJsonObject()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, ent -> MnJson.asString(ent.getValue())));
    }

    /**
     * Convert JsonValue to String.
     * @param value JsonValue
     * @return String
     */
    private static String asString(final JsonValue value) {
        return JsonString.class.cast(value).getString();
    }

    /**
     * Make factory for JSON printing.
     * @return Factory
     */
    private static JsonWriterFactory factory() {
        final Map<String, Object> properties = new HashMap<>(1);
        properties.put(JsonGenerator.PRETTY_PRINTING, true);
        return Json.createWriterFactory(properties);
    }

    /**
     * Open a buffered writer to the given file with truncate semantics.
     * @param target Target file
     * @return Buffered writer ready to be wrapped in a JsonWriter
     */
    private static BufferedWriter bufferedWriterTo(final Path target) {
        try {
            return Files.newBufferedWriter(
                target,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (final IOException ex) {
            throw new IllegalArgumentException(
                String.format("Failed to open '%s' for writing", target),
                ex
            );
        }
    }
}
