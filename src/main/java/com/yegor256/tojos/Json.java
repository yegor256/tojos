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
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;
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
 * The class is NOT thread-safe.
 *
 * @since 0.3.0
 */
public final class Json implements Mono {

    /**
     * The factory of writers.
     */
    private static final JsonWriterFactory JWF = Json.factory();

    /**
     * The file path.
     */
    private final Path file;

    /**
     * Ctor.
     *
     * @param path The path to the file
     * @since 0.4.0
     */
    public Json(final String path) {
        this(Paths.get(path));
    }

    /**
     * Ctor.
     *
     * @param path The path to the file
     * @since 0.4.0
     */
    public Json(final File path) {
        this(path.toPath());
    }

    /**
     * Ctor.
     *
     * @param path The path to the file
     * @since 0.4.0
     */
    public Json(final Path path) {
        this.file = path;
    }

    @Override
    public Collection<Map<String, String>> read() {
        final LinkedList<Map<String, String>> rows = new LinkedList<>();
        if (this.file.toFile().exists()) {
            try (
                Reader reader = Files.newBufferedReader(this.file);
                JsonReader json = javax.json.Json.createReader(reader)
            ) {
                json
                    .readArray()
                    .stream()
                    .map(Json::asMap)
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
        final JsonArrayBuilder array = javax.json.Json.createArrayBuilder();
        for (final Map<String, String> row : rows) {
            final JsonObjectBuilder obj = javax.json.Json.createObjectBuilder();
            obj.add(Tojos.KEY, row.get(Tojos.KEY));
            for (final Map.Entry<String, String> ent : row.entrySet()) {
                if (ent.getKey().equals(Tojos.KEY)) {
                    continue;
                }
                obj.add(ent.getKey(), ent.getValue());
            }
            array.add(obj);
        }
        this.file.toFile().getParentFile().mkdirs();
        try (
            Writer writer = Files.newBufferedWriter(this.file, StandardOpenOption.CREATE)
        ) {
            final JsonWriter json = Json.JWF.createWriter(writer);
            json.write(array.build());
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
     * Covert JsonValue to Map.
     * @param value Value
     * @return Map of Strings.
     */
    private static Map<String, String> asMap(final JsonValue value) {
        return value.asJsonObject()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, ent -> Json.asString(ent.getValue())));
    }

    /**
     * Convert JsonValue to String.
     * @param value JsonValue.
     * @return String.
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
        return javax.json.Json.createWriterFactory(properties);
    }

}
