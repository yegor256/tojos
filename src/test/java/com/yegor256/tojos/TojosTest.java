/*
 * The MIT License (MIT)
 *
 * SPDX-FileCopyrightText: Copyright (c) 2021-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.yegor256.tojos;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.yegor256.MktmpResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test case for {@link Tojos}.
 *
 * @since 0.16
 */
@ExtendWith(MktmpResolver.class)
@SuppressWarnings("JTCOP.RuleAssertionMessage")
final class TojosTest {

    @Test
    void ensuresEveryTojoHaveProperPrefix() {
        ArchRuleDefinition.classes()
            .that().haveSimpleNameStartingWith("Tj")
            .should().implement(Tojos.class)
            .check(new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages("com.yegor256.tojos")
            );
    }
}
