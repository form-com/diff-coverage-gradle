package com.form.coverage.tasks

import com.form.coverage.FullReport
import com.form.coverage.configuration.ChangesetCoverageConfiguration
import com.form.coverage.configuration.toReports
import com.form.coverage.report.ReportGenerator
import com.form.coverage.report.analyzable.AnalyzableReportFactory
import com.form.coverage.tasks.git.getDiffSource
import com.form.diff.CodeUpdateInfo
import com.form.diff.ModifiedLinesDiffParser
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
    fun getExecFiles(): FileCollection {
        return (diffCoverageReport.jacocoExecFiles ?: jacocoReport()?.executionData)
            ?: throw IllegalStateException("Execution data files not specified")
    }

    @InputFiles
    fun getClassesFiles(): FileCollection {
        return (diffCoverageReport.classesDirs ?: jacocoReport()?.allClassDirs)
            ?: throw IllegalStateException("Classes directory not specified")
    }

    private fun getSourcesFiles(): FileCollection {
        return (diffCoverageReport.srcDirs ?: jacocoReport()?.allSourceDirs)
            ?: throw IllegalStateException("Sources directory not specified")
    }

    @Input
    fun getDiffSource(): String = diffCoverageReport.diffSource.let { it.url + it.file }

    @OutputDirectory
    fun getOutputDir(): File {
        return project.getReportOutputDir().toFile().apply {
            log.debug("Diff Coverage output dir: $absolutePath, " +
                    "exists=${exists()}, isDir=$isDirectory, canRead=${canRead()}, canWrite=${canWrite()}")
        }
    }

    init {
        group = "verification"
        description = "Builds coverage report only for modified code"
    }

    @TaskAction
    fun executeAction() {
        log.info("DiffCoverage configuration: $diffCoverageReport")
        val fileNameToModifiedLineNumbers = obtainUpdatesInfo(diffCoverageReport)
        fileNameToModifiedLineNumbers.forEach { (file, rows) ->
            log.info("File $file has ${rows.size} modified lines")
            log.debug("File $file has modified lines $rows")
        }

        val reports: Set<FullReport> = diffCoverageReport.toReports(
            project.getReportOutputDir(),
            CodeUpdateInfo(fileNameToModifiedLineNumbers)
        )
        log.info("Starting task with configuration:")
        reports.forEach {
            log.info("\t$it")
        }

        val analyzableReports = reports.let(AnalyzableReportFactory()::create)

        ReportGenerator(
            project.projectDir,
            getExecFiles().files.filter(File::exists).toSet(),
            getClassesFiles().files.filter(File::exists).toSet(),
            getSourcesFiles().files.filter(File::exists).toSet()
        ).create(analyzableReports)
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

    private fun jacocoReport(): JacocoReport? {
        return project.tasks.findByName("jacocoTestReport") as? JacocoReport
    }

    private fun obtainUpdatesInfo(configuration: ChangesetCoverageConfiguration): Map<String, Set<Int>> {
        val reportDir: File = getOutputDir().apply {
            val isCreated = mkdirs()
            log.debug("Creating of report dir '$absolutePath' is successful: $isCreated")
        }
        val diffSource = getDiffSource(project.rootProject.projectDir, configuration.diffSource).apply {
            log.debug("Starting to retrieve modified lines from $sourceDescription'")
            saveDiffTo(reportDir).apply {
                log.info("diff content saved to '$absolutePath'")
            }
        }
        return ModifiedLinesDiffParser().collectModifiedLines(
            diffSource.pullDiff()
        )
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(DiffCoverageTask::class.java)
    }
}
