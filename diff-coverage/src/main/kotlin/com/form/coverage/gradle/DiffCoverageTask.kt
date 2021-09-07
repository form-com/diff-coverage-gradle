package com.form.coverage.gradle

import com.form.coverage.config.*
import com.form.coverage.report.ReportGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.*
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

open class DiffCoverageTask : DefaultTask() {

    @Nested
    var diffCoverageReport: ChangesetCoverageConfiguration = ChangesetCoverageConfiguration()

    @InputFiles
    fun getExecFiles(): FileCollection = getJacocoReportConfigurationOrThrow(
        "Execution data files not specified",
        diffCoverageReport.jacocoExecFiles
    ) {
        it.executionData
    }

    @InputFiles
    fun getClassesFiles(): FileCollection = getJacocoReportConfigurationOrThrow(
        "Classes directory not specified",
        diffCoverageReport.classesDirs
    ) {
        it.allClassDirs
    }

    private fun getSourcesFiles(): FileCollection = getJacocoReportConfigurationOrThrow(
        "Sources directory not specified",
        diffCoverageReport.srcDirs
    ) {
        it.allSourceDirs
    }

    @Input
    fun getDiffSource(): String = diffCoverageReport.diffSource.let { it.url + it.file }

    @OutputDirectory
    fun getOutputDir(): File {
        return project.getReportOutputDir().toFile().apply {
            log.debug(
                "Diff Coverage output dir: $absolutePath, " +
                        "exists=${exists()}, isDir=$isDirectory, canRead=${canRead()}, canWrite=${canWrite()}"
            )
        }
    }

    init {
        group = "verification"
        description = "Builds coverage report only for modified code"
    }

    @TaskAction
    fun executeAction() {
        log.info("DiffCoverage configuration: $diffCoverageReport")
        val reportDir: File = getOutputDir().apply {
            val isCreated = mkdirs()
            log.debug("Creating of report dir '$absolutePath' is successful: $isCreated")
        }

        val reportGenerator = ReportGenerator(project.rootProject.projectDir, buildDiffCoverageConfig())
        reportGenerator.saveDiffToDir(reportDir).apply {
            log.info("diff content saved to '$absolutePath'")
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

    private fun getJacocoReportConfigurationOrThrow(
        errorMessageOnMissed: String,
        diffCoverageResourceFileCollection: FileCollection?,
        jacocoResourceMapper: (JacocoReport) -> FileCollection
    ): FileCollection {
        return if (diffCoverageResourceFileCollection != null) {
            diffCoverageResourceFileCollection
        } else {
            jacocoTestReportsSettings(jacocoResourceMapper)
                .takeIf { !it.isEmpty }
                ?: throw IllegalStateException(errorMessageOnMissed)
        }
    }

    private fun jacocoTestReportsSettings(jacocoSettings: (JacocoReport) -> FileCollection): FileCollection {
        return listOf(project).union(project.subprojects).asSequence()
            .map { it.tasks.findByName(JACOCO_REPORT_TASK) }
            .filterNotNull()
            .map { jacocoSettings(it as JacocoReport) }
            .fold(project.files() as FileCollection) { aggregated, nextCollection ->
                aggregated.plus(nextCollection)
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
            execFiles = getExecFiles().files,
            classFiles = getClassesFiles().files,
            sourceFiles = getSourcesFiles().files
        )
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(DiffCoverageTask::class.java)
        const val JACOCO_REPORT_TASK = "jacocoTestReport"
    }

}
