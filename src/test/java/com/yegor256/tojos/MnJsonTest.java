/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import com.yegor256.Mktmp;
import com.yegor256.MktmpResolver;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Test case for {@link MnJson}.
 * @since 0.3.0
 */
@ExtendWith(MktmpResolver.class)
final class MnJsonTest {

    @Test
    void readsEmptyFile(@Mktmp final Path temp) {
        MatcherAssert.assertThat(
            "must read empty collection from new file",
            new MnJson(temp.resolve("foo/bar/empty.json")).read().size(),
            Matchers.equalTo(0)
        );
    }

    @Test
    void writesAndReadsRow(@Mktmp final Path temp) {
        final Mono json = new MnJson(temp.resolve("foo/bar/a.json"));
        final Collection<Map<String, String>> rows = json.read();
        final Map<String, String> row = new HashMap<>(0);
        final String key = Tojos.ID_KEY;
        final String value = "привет,\t\015\012 друг!";
        row.put(key, value);
        rows.add(row);
        json.write(rows);
        MatcherAssert.assertThat(
            "must read back written value",
            json.read().iterator().next().get(key),
            Matchers.equalTo(value)
        );
    }

    @Test
    void writesEmptyCollection(@Mktmp final Path temp) {
        final Mono json = new MnJson(temp.resolve("foo/bar/b.json"));
        json.write(Collections.emptyList());
        MatcherAssert.assertThat(
            "must be empty",
            json.read(),
            Matchers.empty()
        );
    }

    @Test
    void prints(@Mktmp final Path temp) throws IOException {
        final Path path = temp.resolve("z.json");
        final Mono json = new MnJson(path);
        final Collection<Map<String, String>> rows = json.read();
        final Map<String, String> row = new HashMap<>(0);
        row.put(Tojos.ID_KEY, "hello, world!");
        rows.add(row);
        rows.add(row);
        json.write(rows);
        MatcherAssert.assertThat(
            "must work fine",
            new String(Files.readAllBytes(path), StandardCharsets.UTF_8),
            Matchers.containsString("},\012")
        );
    }

    @Test
    void truncatesFileWhenRewritingWithShorterContent(@Mktmp final Path temp) throws IOException {
        final Path path = temp.resolve("rewrite.json");
        final Mono json = new MnJson(path);
        final Collection<Map<String, String>> many = new ArrayList<>(32);
        for (int idx = 0; idx < 32; ++idx) {
            final Map<String, String> row = new HashMap<>();
            row.put(Tojos.ID_KEY, String.format("long-identifier-payload-number-%03d", idx));
            many.add(row);
        }
        json.write(many);
        final Collection<Map<String, String>> few = new ArrayList<>(1);
        final Map<String, String> one = new HashMap<>();
        one.put(Tojos.ID_KEY, "x");
        few.add(one);
        json.write(few);
        final Path reference = temp.resolve("reference.json");
        new MnJson(reference).write(few);
        MatcherAssert.assertThat(
            "rewritten file must not contain stale bytes from the previous longer write",
            Files.readAllBytes(path),
            Matchers.equalTo(Files.readAllBytes(reference))
        );
    }

    @Test
    void hasJavaxJsonApiAsNonOptionalDependency() throws Exception {
        MatcherAssert.assertThat(
            "javax.json:javax.json-api must not be optional, otherwise downstream consumers of MnJson hit NoClassDefFoundError at runtime (see #94)",
            MnJsonTest.optionalIn("javax.json", "javax.json-api"),
            Matchers.is(false)
        );
    }

    @Test
    void hasGlassfishJsonAsNonOptionalDependency() throws Exception {
        MatcherAssert.assertThat(
            "org.glassfish:javax.json must not be optional, otherwise downstream consumers of MnJson hit NoClassDefFoundError at runtime (see #94)",
            MnJsonTest.optionalIn("org.glassfish", "javax.json"),
            Matchers.is(false)
        );
    }

    @Test
    void retrievesKeyAtFirstPosition(@Mktmp final Path temp) throws IOException {
        final Path path = temp.resolve("key-test.json");
        final Mono json = new MnJson(path);
        final Collection<Map<String, String>> rows = json.read();
        final Map<String, String> row = new HashMap<>(0);
        row.put(Tojos.ID_KEY, "xyz");
        row.put("_x", "");
        row.put("zzzz", "");
        rows.add(row);
        json.write(rows);
        MatcherAssert.assertThat(
            "must work fine",
            new String(Files.readAllBytes(path), StandardCharsets.UTF_8),
            Matchers.matchesPattern(
                Pattern.compile(
                    String.format(".*\\{\\s+\"%s\":.*", Tojos.ID_KEY),
                    Pattern.MULTILINE | Pattern.DOTALL
                )
            )
        );
    }

    /**
     * Read the {@code <optional>} flag of a top-level dependency declared in
     * the project's {@code pom.xml}.
     * @param group The {@code <groupId>} of the dependency to look up
     * @param artifact The {@code <artifactId>} of the dependency to look up
     * @return Whether the dependency is declared as optional
     * @throws Exception When the {@code pom.xml} cannot be read or parsed,
     *  or when the requested dependency is not declared at all
     */
    private static boolean optionalIn(final String group, final String artifact)
        throws Exception {
        final Document doc = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(Path.of("pom.xml").toFile());
        final NodeList deps = doc.getElementsByTagName("dependency");
        Boolean optional = null;
        for (int idx = 0; idx < deps.getLength(); ++idx) {
            final Element dep = (Element) deps.item(idx);
            if (group.equals(MnJsonTest.child(dep, "groupId"))
                && artifact.equals(MnJsonTest.child(dep, "artifactId"))) {
                optional = "true".equals(MnJsonTest.child(dep, "optional"));
                break;
            }
        }
        if (optional == null) {
            throw new IllegalStateException(
                String.format(
                    "dependency %s:%s not declared in pom.xml",
                    group, artifact
                )
            );
        }
        return optional;
    }

    /**
     * Read the trimmed text content of the first direct child of the given
     * element with the given tag name.
     * @param parent The parent {@link Element} to look in
     * @param tag The local name of the child element to look up
     * @return Trimmed text content of the matching child, or an empty
     *  string when no such child exists
     */
    private static String child(final Element parent, final String tag) {
        final NodeList kids = parent.getChildNodes();
        String text = "";
        for (int idx = 0; idx < kids.getLength(); ++idx) {
            final Node kid = kids.item(idx);
            if (kid.getNodeType() == Node.ELEMENT_NODE
                && tag.equals(kid.getNodeName())) {
                text = kid.getTextContent().trim();
                break;
            }
        }
        return text;
    }
}
