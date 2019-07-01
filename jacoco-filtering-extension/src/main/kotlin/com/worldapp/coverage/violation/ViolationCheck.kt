package com.worldapp.coverage.violation

import org.jacoco.report.IReportVisitor
import org.jacoco.report.check.Rule
import org.jacoco.report.check.RulesChecker
import java.util.*


fun createViolationCheckVisitor(
        failOnViolation: Boolean = true,
        rules: List<Rule> = ArrayList()
): IReportVisitor {
    val violations = mutableListOf<String>()

    class CoverageRulesVisitor(
            rulesCheckerVisitor: IReportVisitor
    ) : IReportVisitor by rulesCheckerVisitor {
        override fun visitEnd() {
            if (violations.isNotEmpty() && failOnViolation) {
                throw Exception(violations.joinToString("\n"))
            }
        }
    }

    return RulesChecker().apply {
        setRules(rules)
    }.createVisitor { _, _, _, message ->
        violations += message
    }.let { CoverageRulesVisitor(it) }
}



