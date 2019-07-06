package com.worldapp.coverage.tasks

import com.worldapp.coverage.configuration.ChangesetCoverageConfiguration
import com.worldapp.coverage.configuration.toReport
import com.worldapp.coverage.report.ReportGenerator
import com.worldapp.diff.CodeUpdateInfo
import com.worldapp.diff.ModifiedLinesDiffParser
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.io.File

open class DiffCoverageTask : DefaultTask() {

    internal var diffCoverageReportExtension: ChangesetCoverageConfiguration? = null

    init {
        group = "verification"
        description = "Builds coverage report only for modified code"
        dependsOn += "test"
    }

    @TaskAction
    fun executeAction() {
        val diffCoverageReport = diffCoverageReportExtension
                ?: throw RuntimeException("Expected not null")

        val updatesInfo = CodeUpdateInfo(obtainUpdatesInfo(diffCoverageReport.diffFile))
        val report = diffCoverageReport.toReport(getJacocoExtension())

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

    private fun jacocoReport(): JacocoReport {
        val jacocoReport = project.tasks.findByName("jacocoTestReport")
                as? JacocoReport
                ?: throw IllegalStateException("jacocoTestReport task wasn't found")
        return jacocoReport
    }

    private fun getJacocoExtension(): JacocoPluginExtension {
        return project.extensions.getByType(JacocoPluginExtension::class.java)
    }

    private fun obtainUpdatesInfo(diffFilePath: String?): Map<String, Set<Int>> {
        val diffFile = diffFilePath
                ?.let(::File)
                ?: throw RuntimeException("Diff file path not specified: $diffFilePath")

        diffFile.takeIf { it.exists() }
                ?.takeIf { it.isFile }
                ?: throw RuntimeException("No such file: $diffFile")

        return ModifiedLinesDiffParser().collectModifiedLines(diffFile)
    }
}