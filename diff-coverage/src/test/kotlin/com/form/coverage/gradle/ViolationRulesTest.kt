package com.form.coverage.gradle

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equality.shouldBeEqualToComparingFields

class ViolationRulesTest : StringSpec({

    "failOnCoverageLessThan should set all coverage values to a single value and set failOnViolation=true" {
        val expectedCoverage = 0.9
        val violationRules = ViolationRules().apply {
            failIfCoverageLessThan(expectedCoverage)
        }

        violationRules shouldBeEqualToComparingFields ViolationRules(
            expectedCoverage,
            expectedCoverage,
            expectedCoverage,
            true
        )
    }
})
