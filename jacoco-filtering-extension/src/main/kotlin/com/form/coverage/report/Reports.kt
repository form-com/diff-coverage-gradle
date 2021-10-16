package com.form.coverage.report

import com.form.coverage.diff.DiffSource
import org.jacoco.report.check.Rule
import java.nio.file.Path

open class FullReport(
    private val baseReportDir: Path,
    val reports: Set<Report>
) {
    fun resolveReportAbsolutePath(report: Report): Path {
        return baseReportDir.resolve(report.reportDirName)
    }
}

class DiffReport(
    baseReportDir: Path,
    reports: Set<Report>,
    val diffSource: DiffSource,
    val violation: Violation
) : FullReport(baseReportDir, reports)

data class Report(
    val reportType: ReportType,
    val reportDirName: String
)

enum class ReportType {
    HTML, XML, CSV
}

data class Violation(
    val failOnViolation: Boolean,
    val violationRules: List<Rule>
)

