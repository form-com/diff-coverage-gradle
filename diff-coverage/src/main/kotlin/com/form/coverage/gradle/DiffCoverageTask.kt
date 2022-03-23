package com.form.coverage.gradle

import com.form.coverage.config.DiffCoverageConfig
import com.form.coverage.config.DiffSourceConfig
import com.form.coverage.config.ReportConfig
import com.form.coverage.config.ReportsConfig
import com.form.coverage.config.ViolationRuleConfig
import com.form.coverage.report.ReportGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

open class DiffCoverageTask : DefaultTask() {

    init {
        group = "verification"
        description = "Builds coverage report only for modified code"
    }

    @Nested
    lateinit var diffCoverageReport: ChangesetCoverageConfiguration

    internal fun obtainExecFiles(): FileCollection = collectFileCollectionOrThrow(ConfigurationSourceType.EXEC)

    internal fun obtainClassesFiles(): FileCollection = collectFileCollectionOrThrow(ConfigurationSourceType.CLASSES)

    internal fun obtainSourcesFiles(): FileCollection = collectFileCollectionOrThrow(ConfigurationSourceType.SOURCES)

    @OutputDirectory
    fun getOutputDir(): File {
        return project.getReportOutputDir().toFile().apply {
            logger.debug(
                "Diff Coverage output dir: $absolutePath, " +
                        "exists=${exists()}, isDir=$isDirectory, canRead=${canRead()}, canWrite=${canWrite()}"
            )
        }
    }

    @TaskAction
    fun executeAction() {
        logger.info("DiffCoverage configuration: $diffCoverageReport")
        val reportDir: File = getOutputDir().apply {
            val isCreated = mkdirs()
            logger.debug("Creating of report dir '$absolutePath' is successful: $isCreated")
        }

        val reportGenerator = ReportGenerator(project.rootProject.projectDir, buildDiffCoverageConfig())
        reportGenerator.saveDiffToDir(reportDir).apply {
            logger.info("diff content saved to '$absolutePath'")
        }
        reportGenerator.create()
    }

    private fun Project.getReportOutputDir(): Path {
        return Paths.get(diffCoverageReport.reportConfiguration.baseReportDir).let {
            if (it.isAbsolute) {
                it
            } else {
                project.projectDir.toPath().resolve(it)
            }
        }
    }

    private fun buildDiffCoverageConfig(): DiffCoverageConfig {
        return DiffCoverageConfig(
            reportName = project.projectDir.name,
            diffSourceConfig = DiffSourceConfig(
                file = diffCoverageReport.diffSource.file,
                url = diffCoverageReport.diffSource.url,
                diffBase = diffCoverageReport.diffSource.git.diffBase
            ),
            reportsConfig = ReportsConfig(
                baseReportDir = project.getReportOutputDir().toAbsolutePath().toString(),
                html = ReportConfig(enabled = diffCoverageReport.reportConfiguration.html, "html"),
                csv = ReportConfig(enabled = diffCoverageReport.reportConfiguration.csv, "report.csv"),
                xml = ReportConfig(enabled = diffCoverageReport.reportConfiguration.xml, "report.xml"),
                fullCoverageReport = diffCoverageReport.reportConfiguration.fullCoverageReport
            ),
            violationRuleConfig = ViolationRuleConfig(
                minBranches = diffCoverageReport.violationRules.minBranches,
                minInstructions = diffCoverageReport.violationRules.minInstructions,
                minLines = diffCoverageReport.violationRules.minLines,
                failOnViolation = diffCoverageReport.violationRules.failOnViolation
            ),
            execFiles = obtainExecFiles().files,
            classFiles = obtainClassesFiles().files,
            sourceFiles = obtainSourcesFiles().files
        )
    }

    companion object {
        const val JACOCO_REPORT_TASK = "jacocoTestReport"
    }

}
