package com.form.coverage.config

import java.io.File

data class DiffSourceConfig(
    val file: String = "",
    val url: String = "",
    val diffBase: String = ""
)

data class ViolationRuleConfig(
    val minLines: Double = 0.0,
    val minBranches: Double = 0.0,
    val minInstructions: Double = 0.0,
    val failOnViolation: Boolean = false
)

data class ReportsConfig(
    val html: ReportConfig,
    val xml: ReportConfig,
    val csv: ReportConfig,
    val baseReportDir: String = "",
    val fullCoverageReport: Boolean = false
)

data class ReportConfig(
    val enabled: Boolean,
    val outputFileName: String
)

data class DiffCoverageConfig(
    val reportName: String,
    val diffSourceConfig: DiffSourceConfig,
    val reportsConfig: ReportsConfig,
    val violationRuleConfig: ViolationRuleConfig,
    val execFiles: Set<File>,
    val classFiles: Set<File>,
    val sourceFiles: Set<File>
)

