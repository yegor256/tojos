/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2024 Yegor Bugayenko
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

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import com.opencsv.RFC4180ParserBuilder;
import com.opencsv.exceptions.CsvValidationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * CSV file.
 *
 * <p>The class is NOT thread-safe.</p>
 *
 * @see <a href="https://geekprompt.github.io/Properly-handling-backshlash-while-using-openCSV/"/>
 * @since 0.3.0
 */
public final class MnCsv implements Mono {

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
    public MnCsv(final String path) {
        this(Paths.get(path));
    }

    /**
     * Ctor.
     *
     * @param path The path to the file
     * @since 0.4.0
     */
    public MnCsv(final File path) {
        this(path.toPath());
    }

    /**
     * Ctor.
     *
     * <p>If the directory doesn't exist, it will automatically be created.</p>
     *
     * @param path The path to the file
     */
    public MnCsv(final Path path) {
        this.file = path;
    }

    @Override
    public String toString() {
        return this.file.toString();
    }

    @Override
    @SuppressWarnings("PMD.CognitiveComplexity")
    public Collection<Map<String, String>> read() {
        final Collection<Map<String, String>> rows = new LinkedList<>();
        if (Files.exists(this.file)) {
            try (
                CSVReader reader = new CSVReaderBuilder(
                    Files.newBufferedReader(this.file)
                ).withCSVParser(
                    new RFC4180ParserBuilder().build()
                ).build()
            ) {
                final String[] header = reader.readNext();
                while (true) {
                    final String[] next = reader.readNext();
                    if (next == null) {
                        break;
                    }
                    final Map<String, String> row = new HashMap<>(header.length);
                    for (int pos = 0; pos < next.length; ++pos) {
                        if (next[pos].isEmpty()) {
                            continue;
                        }
                        row.put(header[pos], next[pos]);
                    }
                    rows.add(row);
                }
            } catch (final IOException | CsvValidationException ex) {
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
        final Collection<Map<String, String>> copy = MnCsv.dup(rows);
        final Collection<String> keys = new HashSet<>(0);
        for (final Map<String, String> row : copy) {
            keys.addAll(row.keySet());
        }
        final List<String> header = new ArrayList<>(keys.size());
        header.addAll(keys);
        Collections.sort(header);
        if (header.contains(Tojos.ID_KEY)) {
            Collections.swap(header, 0, header.indexOf(Tojos.ID_KEY));
        }
        final String[] values = new String[header.size()];
        this.file.toFile().getParentFile().mkdirs();
        try (ICSVWriter writer = new CSVWriter(Files.newBufferedWriter(this.file))) {
            writer.writeNext(header.toArray(new String[] {}));
            for (final Map<String, String> row : copy) {
                Arrays.fill(values, "");
                for (final Map.Entry<String, String> ent : row.entrySet()) {
                    values[header.indexOf(ent.getKey())] = ent.getValue();
                }
                writer.writeNext(values);
            }
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

    @Override
    public void close() {
        // nothing to close here
    }

    /**
     * Make a duplicate of the provided rows.
     *
     * <p>This is necessary for making sure the list of rows is not updated
     * by another thread while we are using it.</p>
     *
     * @param rows Original rows
     * @return Duplicate
     */
    private static Collection<Map<String, String>> dup(final Collection<Map<String, String>> rows) {
        final Collection<Map<String, String>> list = new ArrayList<>(rows.size());
        final List<Map<String, String>> snapshot = new ArrayList<>(rows);
        for (final Map<String, String> map : snapshot) {
            final Map<String, String> copy = new HashMap<>(map.size());
            copy.putAll(map);
            list.add(copy);
        }
        return list;
    }
}
