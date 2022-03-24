package com.form.coverage.report.analyzable

import com.form.coverage.report.FullReport
import com.form.coverage.report.ReportType
import org.jacoco.core.analysis.Analyzer
import org.jacoco.core.analysis.ICoverageVisitor
import org.jacoco.core.data.ExecutionDataStore
import org.jacoco.report.FileMultiReportOutput
import org.jacoco.report.IReportVisitor
import org.jacoco.report.MultiReportVisitor
import org.jacoco.report.csv.CSVFormatter
import org.jacoco.report.html.HTMLFormatter
import org.jacoco.report.xml.XMLFormatter
import java.io.File
import java.io.FileOutputStream

internal open class FullCoverageAnalyzableReport(
    private val report: FullReport
) : AnalyzableReport {

    override fun buildVisitor(): IReportVisitor {
        return report.reports.map {
            val reportFile: File = report.resolveReportAbsolutePath(it).toFile()
            when (it.reportType) {
                ReportType.HTML -> FileMultiReportOutput(reportFile).let(HTMLFormatter()::createVisitor)
                ReportType.XML -> reportFile.createFileOutputStream().let(XMLFormatter()::createVisitor)
                ReportType.CSV -> reportFile.createFileOutputStream().let(CSVFormatter()::createVisitor)
            }
        }.let(::MultiReportVisitor)
    }

    private fun File.createFileOutputStream(): FileOutputStream {
        parentFile.mkdirs()
        return FileOutputStream(this)
    }

    override fun buildAnalyzer(
        executionDataStore: ExecutionDataStore,
        coverageVisitor: ICoverageVisitor
    ): Analyzer {
        return Analyzer(executionDataStore, coverageVisitor)
    }

}
