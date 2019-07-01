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
//            dependsOn += "test"

            doLast {
                val updatesInfo = CodeUpdateInfo(obtainUpdatesInfo(extension.diffFile))

                val jacocoTask = project.tasks.findByName("jacocoTestReport")
                        as? JacocoReport
                        ?: throw IllegalStateException("jacocoTestReport task wasn't found")
                // TODO process all dirs
                val binDir = jacocoTask.allClassDirs.files.filter {
                    it.absolutePath.contains("java")
                }.first().absolutePath
                val jacocoExec = jacocoTask.executionData.files.first().absolutePath
                val src = jacocoTask.allSourceDirs.files.filter {
                    it.absolutePath.contains("java")
                }.first().absolutePath
                // TODO end section

               ReportGenerator(
                        project.projectDir,
                        jacocoExec,
                        binDir,
                        src,
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


