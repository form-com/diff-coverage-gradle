package com.form.coverage.tasks

import com.form.coverage.configuration.ChangesetCoverageConfiguration
import com.form.coverage.configuration.DiffSourceConfiguration
import com.form.coverage.configuration.diff.getDiffSource
import com.form.coverage.configuration.toReport
import com.form.coverage.report.ReportGenerator
import com.form.diff.CodeUpdateInfo
import com.form.diff.ModifiedLinesDiffParser
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.slf4j.LoggerFactory
import java.io.File

open class DiffCoverageTask : DefaultTask() {

    internal lateinit var diffCoverageReport: ChangesetCoverageConfiguration

    @InputFiles
    fun getExecFiles(): FileCollection {
        return (diffCoverageReport.jacocoExecFiles ?: jacocoReport()?.executionData)
                ?: throw IllegalStateException("Execution data not specified")
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
        return File(project.getReportOutputDir())
    }

    init {
        group = "verification"
        description = "Builds coverage report only for modified code"
    }

    @TaskAction
    fun executeAction() {
        val fileNameToModifiedLineNumbers = obtainUpdatesInfo(diffCoverageReport.diffSource)
        fileNameToModifiedLineNumbers.forEach { (file, rows) ->
            log.info("File $file has ${rows.size} modified lines")
            log.debug("File $file has modified lines $rows")
        }
        val updatesInfo = CodeUpdateInfo(fileNameToModifiedLineNumbers)

        ReportGenerator(
                project.projectDir,
                getExecFiles().files.filter(File::exists).toSet(),
                getClassesFiles().files.filter(File::exists).toSet(),
                getSourcesFiles().files.filter(File::exists).toSet(),
                updatesInfo
        ).create(diffCoverageReport.toReport(
                project.getReportOutputDir()
        ))
    }

    private fun Project.getReportOutputDir(): String {
        return buildDir.toPath().resolve("reports/jacoco/diffCoverage").toString()
    }

    private fun jacocoReport(): JacocoReport? {
        return project.tasks.findByName("jacocoTestReport") as? JacocoReport
    }

    private fun obtainUpdatesInfo(diffFilePath: DiffSourceConfiguration): Map<String, Set<Int>> {
        val diffSource = getDiffSource(diffFilePath).apply {
            log.debug("Starting to retrieve modified lines from $sourceType $sourceLocation")
        }

        return ModifiedLinesDiffParser().collectModifiedLines(
                diffSource.pullDiff()
        )
    }

    companion object {
        val log = LoggerFactory.getLogger(DiffCoverageTask::class.java)
    }
}
