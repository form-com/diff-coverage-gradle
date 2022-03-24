package com.form.coverage.report.analyzable

import com.form.coverage.diff.CodeUpdateInfo
import com.form.coverage.diff.parse.ModifiedLinesDiffParser
import com.form.coverage.filters.ModifiedLinesFilter
import com.form.coverage.report.DiffReport
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
    private val diffReport: DiffReport
) : FullCoverageAnalyzableReport(diffReport) {

    override fun buildVisitor(): IReportVisitor {
        val visitors: MutableList<IReportVisitor> = mutableListOf(super.buildVisitor())

        visitors += createViolationCheckVisitor(
            diffReport.violation.failOnViolation,
            diffReport.violation.violationRules
        )

        return MultiReportVisitor(visitors)
    }

    override fun buildAnalyzer(
        executionDataStore: ExecutionDataStore,
        coverageVisitor: ICoverageVisitor
    ): Analyzer {
        val codeUpdateInfo = obtainCodeUpdateInfo()
        return FilteringAnalyzer(
            executionDataStore,
            coverageVisitor,
            codeUpdateInfo::isInfoExists
        ) {
            ModifiedLinesFilter(codeUpdateInfo)
        }
    }

    private fun obtainCodeUpdateInfo(): CodeUpdateInfo {
        val changesMap = ModifiedLinesDiffParser().collectModifiedLines(
            diffReport.diffSource.pullDiff()
        )
        changesMap.forEach { (file, rows) ->
            log.debug("File $file has ${rows.size} modified lines")
        }
        return CodeUpdateInfo(changesMap)
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

    private companion object {
        val log = LoggerFactory.getLogger(DiffCoverageAnalyzableReport::class.java)
    }
}
