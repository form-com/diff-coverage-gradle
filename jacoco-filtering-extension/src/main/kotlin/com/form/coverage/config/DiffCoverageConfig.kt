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

data class ReportConfig(
    val html: Boolean = false,
    val xml: Boolean = false,
    val csv: Boolean = false,
    val baseReportDir: String = "",
    val fullCoverageReport: Boolean = false
)

data class DiffCoverageConfig(
    val reportName: String,
    val diffSourceConfig: DiffSourceConfig = DiffSourceConfig(),
    val reportConfig: ReportConfig = ReportConfig(),
    val violationRuleConfig: ViolationRuleConfig = ViolationRuleConfig(),
    val execFiles: Set<File>,
    val classFiles: Set<File>,
    val sourceFiles: Set<File>
)

