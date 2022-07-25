package com.form.coverage.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class DiffCoveragePlugin : Plugin<Project> {

    override fun apply(project: Project) {

        if(project.isAutoApplyJacocoEnabled()) {
            autoApplyJacocoPlugin(project)
        }

        project.tasks.create(DIFF_COV_TASK, DiffCoverageTask::class.java) {
            it.diffCoverageReport = project.extensions.create(
                DIFF_COVERAGE_REPORT_EXTENSION,
                ChangesetCoverageConfiguration::class.java
            )
        }
    }

    private fun autoApplyJacocoPlugin(project: Project) {
        val jacocoApplied: Boolean = project.allprojects.any {
            it.pluginManager.hasPlugin(JACOCO_PLUGIN)
        }
        if (!jacocoApplied) {
            project.allprojects.forEach {
                project.logger.info("Auto-applying $JACOCO_PLUGIN plugin to project '{}'", it.name)
                it.pluginManager.apply(JACOCO_PLUGIN)
            }
        }
    }

    private fun Project.isAutoApplyJacocoEnabled(): Boolean {
        val autoApplyValue = project.properties.getOrDefault(AUTO_APPLY_JACOCO_PROPERTY_NAME, "true")!!
        return autoApplyValue.toString().toBoolean()
    }

    companion object {
        const val AUTO_APPLY_JACOCO_PROPERTY_NAME = "com.form.diff-coverage.auto-apply-jacoco"
        const val DIFF_COVERAGE_REPORT_EXTENSION = "diffCoverageReport"
        const val DIFF_COV_TASK = "diffCoverage"
        const val JACOCO_PLUGIN = "jacoco"
    }

}
