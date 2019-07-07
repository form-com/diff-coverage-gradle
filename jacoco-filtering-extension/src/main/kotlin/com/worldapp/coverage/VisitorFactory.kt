package com.worldapp.coverage

import org.jacoco.report.FileMultiReportOutput
import org.jacoco.report.IReportVisitor
import org.jacoco.report.MultiReportVisitor
import org.jacoco.report.check.Rule
import org.jacoco.report.check.RulesChecker
import org.jacoco.report.html.HTMLFormatter
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*


data class Report(
        val buildHtmlReport: Boolean,
        val htmlReportOutputDir: File,

        val failOnViolation: Boolean,
        val violationRules: List<Rule>
)

internal fun createVisitor(
        report: Report
): IReportVisitor {
    val visitors = mutableListOf<IReportVisitor>()

    if(report.violationRules.isNotEmpty()) {
        visitors += createViolationCheckVisitor(
                report.failOnViolation,
                report.violationRules
        )
    }

    if(report.buildHtmlReport) {
        val fileMultiReportOutput = FileMultiReportOutput(report.htmlReportOutputDir)
        visitors += HTMLFormatter().createVisitor(fileMultiReportOutput)
    }

    return visitors.let(::MultiReportVisitor)
}

private fun createViolationCheckVisitor(
        failOnViolation: Boolean = true,
        rules: List<Rule> = ArrayList()
): IReportVisitor {
    val log = LoggerFactory.getLogger("ViolationRules")
    val violations = mutableListOf<String>()

    class CoverageRulesVisitor(
            rulesCheckerVisitor: IReportVisitor
    ) : IReportVisitor by rulesCheckerVisitor {
        override fun visitEnd() {
            log.info("Fail on violations: $failOnViolation. Found violations: ${violations.size}.")
            if (violations.isNotEmpty() && failOnViolation) {
                throw Exception(violations.joinToString("\n"))
            }
        }
    }

    return RulesChecker().apply {
        setRules(rules)
    }.createVisitor { _, _, _, message ->
        log.info("New violation: $message")
        violations += message
    }.let { CoverageRulesVisitor(it) }
}
