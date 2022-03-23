package com.form.coverage.gradle

import com.form.coverage.gradle.DiffCoveragePlugin.Companion.DIFF_COV_TASK
import com.form.coverage.gradle.DiffCoverageTask.Companion.JACOCO_REPORT_TASK
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.string.shouldBeEqualIgnoringCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.tasks.DefaultTaskContainer
import org.gradle.testfixtures.ProjectBuilder

class DiffCoverageTaskTest : StringSpec() {

    init {
        "get input file collection should throw when file collection is not specified" {
            forAll(
                row("'diffCoverageReport.jacocoExecFiles' is not configured.", DiffCoverageTask::obtainExecFiles),
                row("'diffCoverageReport.classesDirs' is not configured.", DiffCoverageTask::obtainClassesFiles),
                row("'diffCoverageReport.srcDirs' is not configured.", DiffCoverageTask::obtainSourcesFiles)
            ) { expectedError, sourceAccessor ->
                // setup
                val coverageConfiguration = ChangesetCoverageConfiguration()
                val diffCoverageTask: DiffCoverageTask = spyDiffCoverageTask(coverageConfiguration)

                // run
                val exception = shouldThrow<IllegalArgumentException> {
                    sourceAccessor(diffCoverageTask)
                }

                // assert
                exception.message shouldBeEqualIgnoringCase expectedError
            }
        }

        "get input file collection should throw when file collection is empty" {
            forAll(
                row("'diffCoverageReport.jacocoExecFiles' file collection is empty.", DiffCoverageTask::obtainExecFiles),
                row("'diffCoverageReport.classesDirs' file collection is empty.", DiffCoverageTask::obtainClassesFiles),
                row("'diffCoverageReport.srcDirs' file collection is empty.", DiffCoverageTask::obtainSourcesFiles)
            ) { expectedError, sourceAccessor ->
                // setup
                val emptyFileCollection: FileCollection = mockk {
                    every { isEmpty } returns true
                }
                val diffCoverageTask: DiffCoverageTask = spyDiffCoverageTask(
                    ChangesetCoverageConfiguration().apply {
                        jacocoExecFiles = emptyFileCollection
                        classesDirs = emptyFileCollection
                        srcDirs = emptyFileCollection
                    }
                )

                // run
                val exception = shouldThrow<IllegalArgumentException> {
                    sourceAccessor(diffCoverageTask)
                }

                // assert
                exception.message shouldBeEqualIgnoringCase expectedError
            }
        }

    }

    private fun spyDiffCoverageTask(configuration: ChangesetCoverageConfiguration): DiffCoverageTask {
        val coverageTask = ProjectBuilder.builder().build().tasks
            .create(DIFF_COV_TASK, DiffCoverageTask::class.java) {
                it.diffCoverageReport = configuration
            }
        return spyk(coverageTask) {
            every { project } returns mockProject()
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
