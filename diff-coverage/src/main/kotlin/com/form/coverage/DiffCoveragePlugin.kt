package com.form.coverage

import com.form.coverage.configuration.ChangesetCoverageConfiguration
import com.form.coverage.tasks.DiffCoverageTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class DiffCoveragePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create(DIFF_COV_EXTENSION, ChangesetCoverageConfiguration::class.java)

        project.tasks.create(DIFF_COV_TASK, DiffCoverageTask::class.java) {
            it.diffCoverageReport = extension
        }
    }

    companion object {
        const val DIFF_COV_EXTENSION = "diffCoverageReport"
        const val DIFF_COV_TASK = "diffCoverage"
    }

}
