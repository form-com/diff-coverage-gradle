package com.form.coverage.configuration

import com.form.coverage.*
import com.form.diff.CodeUpdateInfo
import org.jacoco.core.analysis.ICoverageNode
import org.jacoco.report.check.Limit
import org.jacoco.report.check.Rule
import java.nio.file.Path

fun ChangesetCoverageConfiguration.toReports(baseReportDir: Path, codeUpdateInfo: CodeUpdateInfo): Set<FullReport> {
    val reports: Set<Report> = toReportTypes()

    val report: MutableSet<FullReport> = mutableSetOf(
            DiffReport(
                    baseReportDir.resolve("diffCoverage"),
                    reports,
                    codeUpdateInfo,
                    Violation(
                            violationRules.failOnViolation,
                            listOf(buildRules(violationRules))
                    )
            )
    )

    if (reportConfiguration.fullCoverageReport) {
        report += FullReport(
                baseReportDir.resolve("fullReport"),
                reports
        )
    }

    return report
}

private fun ChangesetCoverageConfiguration.toReportTypes(): Set<Report> = sequenceOf(
        ReportType.HTML to reportConfiguration.html,
        ReportType.CSV to reportConfiguration.csv,
        ReportType.XML to reportConfiguration.xml
).filter { it.second }.map {
    Report(it.first, it.first.defaultOutputFileName())
}.toSet()

private fun ReportType.defaultOutputFileName(): String = when(this) {
    ReportType.XML -> "report.xml"
    ReportType.CSV -> "report.csv"
    ReportType.HTML -> "html"
}

private fun buildRules(
        violationRules: ViolationRules
): Rule {
    return sequenceOf(
            ICoverageNode.CounterEntity.INSTRUCTION to violationRules.minInstructions,
            ICoverageNode.CounterEntity.BRANCH to violationRules.minBranches,
            ICoverageNode.CounterEntity.LINE to violationRules.minLines
    ).filter {
        it.second > 0.0
    }.map {
        Limit().apply {
            setCounter(it.first.name)
            minimum = it.second.toString()
        }
    }.toList().let {
        Rule().apply {
            limits = it
        }
    }
}
