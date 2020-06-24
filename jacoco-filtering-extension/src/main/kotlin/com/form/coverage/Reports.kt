package com.form.coverage

import com.form.diff.CodeUpdateInfo
import org.jacoco.report.check.Rule
import java.nio.file.Path

open class FullReport(
        private val baseReportDir: Path,
        val reports: Set<Report>
) {
    fun resolveReportAbsolutePath(report: Report): Path {
        return baseReportDir.resolve(report.reportDirName)
    }

    override fun toString(): String {
        return "FullReport(baseReportDir=$baseReportDir, reports=$reports)"
    }
 }

class DiffReport(
        baseReportDir: Path,
        reports: Set<Report>,
        val codeUpdateInfo: CodeUpdateInfo,
        val violation: Violation
) : FullReport(baseReportDir, reports)

data class Report(
        val reportType: ReportType,
        val reportDirName: String = reportType.name.toLowerCase()
)

enum class ReportType {
    HTML, XML, CSV
}

data class Violation(
        val failOnViolation: Boolean,
        val violationRules: List<Rule>
)

