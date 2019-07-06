package com.worldapp.coverage

import com.worldapp.coverage.configuration.ChangesetCoverageConfiguration
import com.worldapp.coverage.tasks.DiffCoverageTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class DiffCoveragePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.getExtension<ChangesetCoverageConfiguration>(DIFF_COV_EXTENSION)

        project.createTask<DiffCoverageTask>(DIFF_COV_TASK).apply {
            diffCoverageReport = extension
        }
    }

    private companion object {
        const val DIFF_COV_EXTENSION = "diffCoverageReport"
        const val DIFF_COV_TASK = "diffCoverage"
    }
}

private inline fun <reified T: Task> Project.createTask(taskName: String): T {
    return tasks.create(taskName, T::class.java)
}

private inline fun <reified T> Project.getExtension(extensionName: String): T {
    return extensions.create(extensionName, T::class.java)
}

