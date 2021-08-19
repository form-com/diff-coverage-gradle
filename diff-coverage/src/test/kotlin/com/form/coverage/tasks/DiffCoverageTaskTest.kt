package com.form.coverage.tasks

import com.form.coverage.DiffCoveragePlugin.Companion.DIFF_COV_TASK
import com.form.coverage.configuration.ChangesetCoverageConfiguration
import com.form.coverage.tasks.DiffCoverageTask.Companion.JACOCO_REPORT_TASK
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.should
import io.kotest.matchers.string.startWith
import io.kotest.matchers.types.shouldBeTypeOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.gradle.api.Project
import org.gradle.api.internal.tasks.DefaultTaskContainer
import org.gradle.testfixtures.ProjectBuilder

class DiffSourceKtTest : StringSpec() {

    init {
        "getExecFiles should throw when exec files not specified" {
            // setup
            val projectMock = mockProject()

            val coverageTask = ProjectBuilder.builder().build().tasks
                .create(DIFF_COV_TASK, DiffCoverageTask::class.java) {
                    it.diffCoverageReport = ChangesetCoverageConfiguration()
                }

            val diffCoverageTask: DiffCoverageTask = spyk(coverageTask) {
                every { project } returns projectMock
            }

            // run
            val exception = shouldThrow<Exception> {
                diffCoverageTask.getExecFiles()
            }

            // assert
            exception.shouldBeTypeOf<IllegalStateException>()
            exception.message should startWith("Execution data files not specified")
        }
    }

    private fun mockProject() = mockk<Project> {
        every { subprojects } returns emptySet<Project>()
        every { tasks } returns mockk<DefaultTaskContainer> {
            every { findByName(JACOCO_REPORT_TASK) } returns null
        }
        every { files() } returns mockk {
            every { isEmpty } returns true
        }
    }
}
