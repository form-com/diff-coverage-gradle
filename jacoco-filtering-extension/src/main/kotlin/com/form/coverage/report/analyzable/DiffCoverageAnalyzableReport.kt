package com.form.coverage.report.analyzable

import com.form.coverage.DiffReport
import com.form.coverage.filters.ModifiedLinesFilter
import org.jacoco.core.analysis.Analyzer
import org.jacoco.core.analysis.ICoverageVisitor
import org.jacoco.core.data.ExecutionDataStore
import org.jacoco.core.internal.analysis.FilteringAnalyzer
import org.jacoco.report.IReportVisitor
import org.jacoco.report.MultiReportVisitor
import org.jacoco.report.check.Rule
import org.jacoco.report.check.RulesChecker
import org.slf4j.LoggerFactory

internal class DiffCoverageAnalyzableReport(
        private val reportMode: DiffReport
) : FullCoverageAnalyzableReport(reportMode) {

    override fun buildVisitor(): IReportVisitor {
        val visitors: MutableList<IReportVisitor> = mutableListOf(
                super.buildVisitor()
        )

        if (reportMode.violation.violationRules.isNotEmpty()) {
            visitors += createViolationCheckVisitor(
                    reportMode.violation.failOnViolation,
                    reportMode.violation.violationRules
            )
        }

        return MultiReportVisitor(visitors)
    }

    override fun buildAnalyzer(
            executionDataStore: ExecutionDataStore,
            coverageVisitor: ICoverageVisitor
    ): Analyzer {
        return FilteringAnalyzer(
                executionDataStore,
                coverageVisitor,
                reportMode.codeUpdateInfo::isInfoExists
        ) {
            ModifiedLinesFilter(reportMode.codeUpdateInfo)
        }
    }

    private fun createViolationCheckVisitor(
            failOnViolation: Boolean,
            rules: List<Rule>
    ): IReportVisitor {
        val log = LoggerFactory.getLogger("ViolationRules")
        val violations = mutableListOf<String>()

        class CoverageRulesVisitor(
                rulesCheckerVisitor: IReportVisitor
        ) : IReportVisitor by rulesCheckerVisitor {
            override fun visitEnd() {
                log.warn("Fail on violations: $failOnViolation. Found violations: ${violations.size}.")
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
}
