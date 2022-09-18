package com.form.coverage.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.internal.file.FileOperations
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReportBase
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

open class DiffCoveragePlugin @Inject constructor(
    private val fileOperations: FileOperations
) : Plugin<Project> {

    override fun apply(project: Project) {

        if (project.isAutoApplyJacocoEnabled()) {
            autoApplyJacocoPlugin(project)
        }

        project.tasks.create(DIFF_COV_TASK, DiffCoverageTask::class.java) { diffCoverageTask ->
            diffCoverageTask.projectDirProperty.set(project.projectDir)
            diffCoverageTask.rootProjectDirProperty.set(project.rootProject.projectDir)

            diffCoverageTask.diffCoverageReport.set(
                project.extensions.create(
                    DIFF_COVERAGE_REPORT_EXTENSION,
                    ChangesetCoverageConfiguration::class.java
                )
            )

            diffCoverageTask.applyInputsFromJacocoPlugin()

            configureDependencies(project, diffCoverageTask)
        }
    }

    private fun configureDependencies(
        project: Project,
        diffCoverageTask: DiffCoverageTask
    ) = project.gradle.taskGraph.whenReady {
        project.allprojects.asSequence().map { it.tasks }.forEach { projectTasks ->
            projectTasks.named(JavaPlugin.CLASSES_TASK_NAME).configure { classesTask ->
                diffCoverageTask.dependsOn(classesTask)
            }

            projectTasks.withType(Test::class.java).forEach { testTask ->
                val jacocoExtensionConfigured: Boolean =
                    testTask.extensions.findByType(JacocoTaskExtension::class.java) != null
                if (jacocoExtensionConfigured) {
                    diffCoverageTask.mustRunAfter(testTask)
                }
            }
        }
    }

    private fun DiffCoverageTask.applyInputsFromJacocoPlugin() = project.gradle.taskGraph.whenReady {
        val jacocoPluginInputs: JacocoInputs = collectJacocoPluginInputs(project)
        jacocoExecFiles.set(jacocoPluginInputs.allExecFiles)
        jacocoClassesFiles.set(jacocoPluginInputs.allClasses)
        jacocoSourceFiles.set(jacocoPluginInputs.allSources)
    }

    private fun autoApplyJacocoPlugin(project: Project) {
        val jacocoApplied: Boolean = project.allprojects.any {
            it.pluginManager.hasPlugin(JACOCO_PLUGIN)
        }
        if (!jacocoApplied) {
            project.allprojects.forEach {
                log.info("Auto-applying $JACOCO_PLUGIN plugin to project '{}'", it.name)
                it.pluginManager.apply(JACOCO_PLUGIN)
            }
        }
    }

    private fun Project.isAutoApplyJacocoEnabled(): Boolean {
        val autoApplyValue = project.properties.getOrDefault(AUTO_APPLY_JACOCO_PROPERTY_NAME, "true")!!
        return autoApplyValue.toString().toBoolean()
    }

    private fun collectJacocoPluginInputs(project: Project): JacocoInputs {
        return listOf(project).union(project.subprojects)
            .asSequence()
            .map { it.tasks.findByName(DiffCoverageTask.JACOCO_REPORT_TASK) }
            .filterNotNull()
            .map { it as JacocoReportBase }
            .fold(newJacocoInputs()) { jacocoInputs, jacocoReport ->
                log.debug("Found JaCoCo configuration in gradle project '{}'", jacocoReport.project.name)

                jacocoInputs.apply {
                    allExecFiles.from(jacocoReport.executionData)
                    allClasses.from(jacocoReport.allClassDirs)
                    allSources.from(jacocoReport.allSourceDirs)
                }
            }
    }

    private fun newJacocoInputs() = JacocoInputs(
        // FileOperations is used to support Gradle < v5.3
        // If min supported Gradle version is 5.3 then it could be replaced with ObjectFactory#fileCollection
        fileOperations.configurableFiles(),
        fileOperations.configurableFiles(),
        fileOperations.configurableFiles()
    )

    private class JacocoInputs(
        val allExecFiles: ConfigurableFileCollection,
        val allClasses: ConfigurableFileCollection,
        val allSources: ConfigurableFileCollection
    )

    companion object {
        const val AUTO_APPLY_JACOCO_PROPERTY_NAME = "com.form.diff-coverage.auto-apply-jacoco"
        const val DIFF_COVERAGE_REPORT_EXTENSION = "diffCoverageReport"
        const val DIFF_COV_TASK = "diffCoverage"
        const val JACOCO_PLUGIN = "jacoco"

        val log: Logger = LoggerFactory.getLogger(DiffCoveragePlugin::class.java)
    }

}
