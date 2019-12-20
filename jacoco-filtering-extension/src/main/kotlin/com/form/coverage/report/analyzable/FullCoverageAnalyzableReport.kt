package com.form.coverage.report.analyzable

import com.form.coverage.FullReport
import com.form.coverage.ReportType
import org.jacoco.core.analysis.Analyzer
import org.jacoco.core.analysis.ICoverageVisitor
import org.jacoco.core.data.ExecutionDataStore
import org.jacoco.report.FileMultiReportOutput
import org.jacoco.report.IReportVisitor
import org.jacoco.report.MultiReportVisitor
import org.jacoco.report.html.HTMLFormatter

internal open class FullCoverageAnalyzableReport(
        private val report: FullReport
) : AnalyzableReport {

    override fun buildVisitor(): IReportVisitor {
        return report.reports.map {
            when(it.reportType) {
                ReportType.HTML -> report.resolveReportAbsolutePath(it)
                        .toFile().let(::FileMultiReportOutput)
                        .let(HTMLFormatter()::createVisitor)

            }
        }.let(::MultiReportVisitor)
    }

    override fun buildAnalyzer(
            executionDataStore: ExecutionDataStore,
            coverageVisitor: ICoverageVisitor
    ): Analyzer {
        return Analyzer(executionDataStore, coverageVisitor)
    }

}
