package com.form.coverage

import com.form.coverage.diff.parser.CodeUpdateInfo
import com.form.coverage.diff.DiffSource
import com.form.coverage.diff.FileDiffSource
import com.form.coverage.diff.GitDiffSource
import com.form.coverage.diff.UrlDiffSource
import com.form.coverage.report.*
import org.jacoco.core.analysis.ICoverageNode
import org.jacoco.report.check.Limit
import org.jacoco.report.check.Rule
import java.io.File
import java.nio.file.Path

fun ChangesetCoverageConfiguration.toReports(baseReportDir: Path, diffSource: DiffSource): Set<FullReport> {
    val reports: Set<Report> = toReportTypes()

    val report: MutableSet<FullReport> = mutableSetOf(
            DiffReport(
                    baseReportDir.resolve("diffCoverage"),
                    reports,
                    diffSource,
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

fun getDiffSource(projectRoot: File, diffConfig: DiffSourceConfiguration): DiffSource = when {

    diffConfig.file.isNotBlank() && diffConfig.url.isNotBlank() -> throw IllegalStateException(
            "Expected only Git configuration or file or URL diff source more than one: " +
                    "git.diffBase=${diffConfig.git.diffBase} file=${diffConfig.file}, url=${diffConfig.url}"
    )

    diffConfig.file.isNotBlank() -> FileDiffSource(diffConfig.file)
    diffConfig.url.isNotBlank() -> UrlDiffSource(diffConfig.url)
    diffConfig.git.diffBase.isNotBlank() -> GitDiffSource(projectRoot, diffConfig.git.diffBase)

    else -> throw IllegalStateException("Expected Git configuration or file or URL diff source but all are blank")
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
