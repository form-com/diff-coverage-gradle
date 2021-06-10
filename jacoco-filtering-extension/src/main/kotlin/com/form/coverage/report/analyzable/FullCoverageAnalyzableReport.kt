package com.form.coverage.report.analyzable

import com.form.coverage.FullReport
import com.form.coverage.ReportType
import org.jacoco.core.analysis.Analyzer
import org.jacoco.core.analysis.ICoverageVisitor
import org.jacoco.core.data.ExecutionDataStore
import org.jacoco.report.FileMultiReportOutput
import org.jacoco.report.IReportVisitor
import org.jacoco.report.MultiReportVisitor
import org.jacoco.report.csv.CSVFormatter
import org.jacoco.report.html.HTMLFormatter
import org.jacoco.report.xml.XMLFormatter
import java.io.FileOutputStream

internal open class FullCoverageAnalyzableReport(
        private val report: FullReport
) : AnalyzableReport {

    override fun buildVisitor(): IReportVisitor {
        return report.reports.map {
            val reportFile = report.resolveReportAbsolutePath(it).toFile()
            when(it.reportType) {
                ReportType.HTML -> FileMultiReportOutput(reportFile).let(HTMLFormatter()::createVisitor)
                ReportType.XML -> FileOutputStream(reportFile).let(XMLFormatter()::createVisitor)
                ReportType.CSV -> FileOutputStream(reportFile).let(CSVFormatter()::createVisitor)
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
