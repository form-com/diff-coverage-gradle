package com.form.coverage.report.analyzable

import com.form.coverage.DiffReport
import com.form.coverage.FullReport
import org.jacoco.core.analysis.Analyzer
import org.jacoco.core.analysis.ICoverageVisitor
import org.jacoco.core.data.ExecutionDataStore
import org.jacoco.report.IReportVisitor

interface AnalyzableReport {

    fun buildVisitor(): IReportVisitor
    fun buildAnalyzer(executionDataStore: ExecutionDataStore, coverageVisitor: ICoverageVisitor): Analyzer
}

class AnalyzableReportFactory {
    fun create(reports: Set<FullReport>): Set<AnalyzableReport> {
        return reports.map { reportMode ->
            when(reportMode) {
                is DiffReport -> DiffCoverageAnalyzableReport(reportMode)
                else -> FullCoverageAnalyzableReport(reportMode)
            }
        }.toSet()
    }
}


