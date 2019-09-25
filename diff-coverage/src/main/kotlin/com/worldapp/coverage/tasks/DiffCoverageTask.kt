package com.worldapp.coverage.tasks

import com.worldapp.coverage.configuration.ChangesetCoverageConfiguration
import com.worldapp.coverage.configuration.toReport
import com.worldapp.coverage.report.ReportGenerator
import com.worldapp.diff.CodeUpdateInfo
import com.worldapp.diff.ModifiedLinesDiffParser
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.slf4j.LoggerFactory
import java.io.File

open class DiffCoverageTask : DefaultTask() {

    internal lateinit var diffCoverageReport: ChangesetCoverageConfiguration

    @InputFiles
    fun getExecFiles(): FileCollection = jacocoReport().executionData

    @InputFiles
    fun getClassesFiles(): FileCollection = jacocoReport().allClassDirs

    @InputFiles
    fun getSourceDirs(): FileCollection = jacocoReport().allSourceDirs

    @InputFile
    fun getDiffFile(): File = File(diffCoverageReport.diffFile)

    @OutputDirectory
    fun getOutputDir(): File {
        return diffCoverageReport.toReport(project.getJacocoExtension()).htmlReportOutputDir
    }

    init {
        group = "verification"
        description = "Builds coverage report only for modified code"
        dependsOn += "test"
    }

    @TaskAction
    fun executeAction() {
        val report = diffCoverageReport.toReport(project.getJacocoExtension())
        val fileNameToModifiedLineNumbers = obtainUpdatesInfo(diffCoverageReport.diffFile)
        fileNameToModifiedLineNumbers.forEach { (file, rows) ->
            log.info("File $file has ${rows.size} modified lines")
            log.debug("File $file has modified lines $rows")
        }
        val updatesInfo = CodeUpdateInfo(fileNameToModifiedLineNumbers)

        jacocoReport().let {
            ReportGenerator(
                    project.projectDir,
                    it.executionData.files,
                    it.allClassDirs.files,
                    it.allSourceDirs.files,
                    updatesInfo
            )
        }.create(report)
    }

    private fun Project.getJacocoExtension(): JacocoPluginExtension {
        return extensions.getByType(JacocoPluginExtension::class.java)
    }

    private fun jacocoReport(): JacocoReport {
        return project.tasks.findByName("jacocoTestReport")
                as? JacocoReport
                ?: throw IllegalStateException("jacocoTestReport task wasn't found")
    }

    private fun obtainUpdatesInfo(diffFilePath: String?): Map<String, Set<Int>> {
        val diffFile = diffFilePath
                ?.let(::File)
                ?: throw RuntimeException("Diff file path not specified: $diffFilePath")

        diffFile.takeIf(File::exists)
                ?.takeIf(File::isFile)
                ?: throw RuntimeException("No such file: $diffFile")

        log.debug("Starting to retrieve modified lines from $diffFile")
        return ModifiedLinesDiffParser().collectModifiedLines(diffFile)
    }

    companion object {
        val log = LoggerFactory.getLogger(DiffCoverageTask::class.java)
    }
}