/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.yegor256.MktmpResolver;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test case for {@link Tojo}.
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
            .check(TojoTest.classes());
    }

    @Test
    @Disabled
    void ensuresEveryTojoImplementsToString() {
        ArchRuleDefinition.classes()
            .that().implement(Tojo.class)
            .should().implement(Tojo.class)
            .check(TojoTest.classes());
    }

    /**
     * Import all production classes of the package once.
     * @return Imported classes
     */
    private static JavaClasses classes() {
        return new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("com.yegor256.tojos");
    }
}
