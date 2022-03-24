package com.form.coverage.report.analyzable

import com.form.coverage.config.DiffCoverageConfig
import com.form.coverage.diff.DiffSource
import com.form.coverage.report.DiffReport
import com.form.coverage.report.reportFactory
import org.jacoco.core.analysis.Analyzer
import org.jacoco.core.analysis.ICoverageVisitor
import org.jacoco.core.data.ExecutionDataStore
import org.jacoco.report.IReportVisitor

internal interface AnalyzableReport {

    fun buildVisitor(): IReportVisitor
    fun buildAnalyzer(executionDataStore: ExecutionDataStore, coverageVisitor: ICoverageVisitor): Analyzer
}

internal fun analyzableReportFactory(
    diffCoverageConfig: DiffCoverageConfig,
    diffSource: DiffSource
): Set<AnalyzableReport> {
    return reportFactory(diffCoverageConfig, diffSource)
        .map { reportMode ->
            when (reportMode) {
                is DiffReport -> DiffCoverageAnalyzableReport(reportMode)
                else -> FullCoverageAnalyzableReport(reportMode)
            }
        }.toSet()
}


