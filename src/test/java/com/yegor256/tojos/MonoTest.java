/*
 * SPDX-FileCopyrightText: Copyright (c) 2021-2026 Yegor Bugayenko
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
 * Test case for {@link Mono}.
 *
 * @since 0.16
 */
@ExtendWith(MktmpResolver.class)
@SuppressWarnings("JTCOP.RuleAssertionMessage")
final class MonoTest {

    @Test
    void ensuresEveryMonoHaveProperPrefix() {
        ArchRuleDefinition.classes()
            .that().haveSimpleNameStartingWith("Mn")
            .should().implement(Mono.class)
            .check(
                new ClassFileImporter()
                    .withImportOption(new ImportOption.DoNotIncludeTests())
                    .importPackages("com.yegor256.tojos")
            );
    }
}
