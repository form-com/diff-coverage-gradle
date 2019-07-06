package com.worldapp.coverage

import com.worldapp.coverage.configuration.ChangesetCoverageConfiguration
import com.worldapp.coverage.configuration.toReport
import com.worldapp.coverage.report.ReportGenerator
import com.worldapp.diff.CodeUpdateInfo
import com.worldapp.diff.ModifiedLinesDiffParser
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.io.File


class DiffCoveragePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create(
                "diffCoverageReport",
                ChangesetCoverageConfiguration::class.java
        )

        val jacocoExtension = project.extensions.getByType(JacocoPluginExtension::class.java)

        project.task("diffCoverage").apply {
            group = "verification"
            dependsOn += "test"

            doLast {
                val updatesInfo = CodeUpdateInfo(obtainUpdatesInfo(extension.diffFile))

                val jacocoTask = project.tasks.findByName("jacocoTestReport")
                        as? JacocoReport
                        ?: throw IllegalStateException("jacocoTestReport task wasn't found")

               ReportGenerator(
                        project.projectDir,
                        jacocoTask.executionData.files,
                        jacocoTask.allClassDirs.files,
                        jacocoTask.allSourceDirs.files,
                        updatesInfo
                ).create(
                       extension.toReport(jacocoExtension)
                )
            }
        }
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


