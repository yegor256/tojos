/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021-2025 Yegor Bugayenko
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

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.yegor256.MktmpResolver;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test case for {@link Tojo}.
 *
 * @since 0.16
 */
@ExtendWith(MktmpResolver.class)
@SuppressWarnings("JTCOP.RuleAssertionMessage")
final class TojoTest {

    @Test
    void ensuresEveryTojoHaveProperPrefix() {
        ArchRuleDefinition.classes()
            .that().haveSimpleNameStartingWith("To")
            .and().doNotHaveSimpleName("Tojo")
            .and().doNotHaveSimpleName("Tojos")
            .should().implement(Tojo.class)
            .check(new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages("com.yegor256.tojos")
            );
    }

    @Test
    @Disabled
    void ensuresEveryTojoImplementsToString() {
        ArchRuleDefinition.classes()
            .that().implement(Tojo.class)
            .should().implement(Tojo.class)
            .check(new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages("com.yegor256.tojos")
            );
    }
}
