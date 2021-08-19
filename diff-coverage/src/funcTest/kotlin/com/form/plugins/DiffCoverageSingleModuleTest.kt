package com.form.plugins

import com.form.coverage.tasks.git.getCrlf
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ConfigConstants
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.nio.file.Paths

class DiffCoverageSingleModuleTest {

    @get:Rule
    var testProjectDir: TemporaryFolder = TemporaryFolder()

    companion object {
        const val TEST_PROJECT_RESOURCE_NAME = "single-module-test-project"

        val EXPECTED_REPORT_FILES = arrayOf(
            "com.java.test",
            "index.html",
            "jacoco-resources",
            "jacoco-sessions.html"
        )
    }

    private lateinit var buildFile: File
    private lateinit var diffFilePath: String
    private lateinit var gradleRunner: GradleRunner

    @Before
    fun setup() {
        testProjectDir.copyResourceDir<DiffCoverageSingleModuleTest>("$TEST_PROJECT_RESOURCE_NAME/src", "src")

        diffFilePath = testProjectDir.copyResourceFile<DiffCoverageSingleModuleTest>(
            "$TEST_PROJECT_RESOURCE_NAME/test.diff.file",
            "1.diff"
        ).toUnixAbsolutePath()
        buildFile = testProjectDir.copyResourceFile<DiffCoverageSingleModuleTest>(
            "$TEST_PROJECT_RESOURCE_NAME/build.gradle",
            "build.gradle"
        )

        gradleRunner = buildGradleRunner(testProjectDir).apply {
            runTask("test")
        }
    }

    @Test
    fun `diff-coverage should create diffCoverage dir and full coverage with html, csv and xml reports`() {
        // setup
        val baseReportDir = "build/custom/reports/dir/jacoco/"
        buildFile.appendText(
            """
            
            diffCoverageReport {
                diffSource {
                    file = '$diffFilePath'
                }
                reports {
                    html = true
                    xml = true
                    csv = true
                    fullCoverageReport = true
                    baseReportDir = '$baseReportDir'
                }
            }
        """.trimIndent()
        )

        // run
        val result = gradleRunner.runTask(DIFF_COVERAGE_TASK)

        // assert
        result.assertDiffCoverageStatusEqualsTo(SUCCESS)
        testProjectDir.root.resolve(baseReportDir).apply {
            assertAllReportsCreated(resolve("diffCoverage"))
            assertAllReportsCreated(resolve("fullReport"))
        }
    }

    @Test
    fun `diff-coverage should use git to generate diff`() {
        // setup
        prepareTestProjectWithGit()

        buildFile.appendText(
            """

            diffCoverageReport {
                diffSource {
                    git.compareWith 'HEAD'
                }
                violationRules {
                    minLines = 0.7
                    failOnViolation = true
                }
            }
        """.trimIndent()
        )

        // run
        val result = gradleRunner.runTaskAndFail(DIFF_COVERAGE_TASK)

        // assert
        result.assertDiffCoverageStatusEqualsTo(FAILED)
            .assertOutputContainsStrings("lines covered ratio is 0.6, but expected minimum is 0.7")
    }

    @Test
    fun `diff-coverage should fail on violation and generate html report`() {
        // setup
        val absolutePathBaseReportDir = testProjectDir.root.toPath()
            .resolve("build/absolute/path/reports/jacoco/")
            .toFile().toUnixAbsolutePath()

        buildFile.appendText(
            """

            diffCoverageReport {
                diffSource.file = '$diffFilePath'
                reports {
                    html = true
                    baseReportDir = '$absolutePathBaseReportDir'
                }
                violationRules {
                    minBranches = 0.6
                    minLines = 0.7
                    minInstructions = 0.8
                    failOnViolation = true
                }
            }
        """.trimIndent()
        )

        // run
        val result = gradleRunner.runTaskAndFail(DIFF_COVERAGE_TASK)

        // assert
        result.assertDiffCoverageStatusEqualsTo(FAILED)
            .assertOutputContainsStrings(
                "instructions covered ratio is 0.5, but expected minimum is 0.8",
                "branches covered ratio is 0.5, but expected minimum is 0.6",
                "lines covered ratio is 0.6, but expected minimum is 0.7"
            )

        val diffCoverageReportDir = Paths.get(absolutePathBaseReportDir, "diffCoverage", "html").toFile()
        assertThat(diffCoverageReportDir.list())
            .containsExactlyInAnyOrder(*EXPECTED_REPORT_FILES)
    }

    @Test
    fun `diff-coverage should not fail on violation when failOnViolation is false`() {
        // setup
        buildFile.appendText(
            """

            diffCoverageReport {
                diffSource.file = '$diffFilePath'
                violationRules {
                    failIfCoverageLessThan 1.0
                    failOnViolation = false
                }
            }
        """.trimIndent()
        )

        // run
        val result = gradleRunner.runTask(DIFF_COVERAGE_TASK)

        // assert
        result.assertDiffCoverageStatusEqualsTo(SUCCESS)
            .assertOutputContainsStrings("Fail on violations: false. Found violations: 3")
    }

    @Test
    fun `diff-coverage should get diff info by url`() {
        // setup
        buildFile.appendText(
            """

            diffCoverageReport {
                diffSource.url = '${File(diffFilePath).toURI().toURL()}'
                violationRules {
                    minInstructions = 1
                    failOnViolation = true
                }
            }
        """.trimIndent()
        )

        // run
        val result = gradleRunner.runTaskAndFail(DIFF_COVERAGE_TASK)

        // assert
        result.assertDiffCoverageStatusEqualsTo(FAILED)
            .assertOutputContainsStrings("instructions covered ratio is 0.5, but expected minimum is 1")
    }

    private fun BuildResult.assertOutputContainsStrings(vararg expectedString: String): BuildResult {
        assertThat(output).contains(*expectedString)
        return this
    }

    private fun assertAllReportsCreated(baseReportDir: File) {
        assertThat(baseReportDir.list()).containsExactlyInAnyOrder("report.xml", "report.csv", "html")
        assertThat(baseReportDir.resolve("html").list()).containsExactlyInAnyOrder(*EXPECTED_REPORT_FILES)
    }

    private fun BuildResult.assertDiffCoverageStatusEqualsTo(status: TaskOutcome): BuildResult {
        assertThat(output).contains("diffCoverage")
        assertThat(task(":diffCoverage")!!.outcome).isEqualTo(status)
        return this
    }

    private fun prepareTestProjectWithGit() {
        testProjectDir.root.resolve(".gitignore").apply {
            appendText("\n*")
            appendText("\n!*.java")
            appendText("\n!gitignore")
            appendText("\n!*/")
        }
        val repository: Repository = FileRepositoryBuilder.create(File(testProjectDir.root, ".git")).apply {
            config.setEnum(
                ConfigConstants.CONFIG_CORE_SECTION,
                null,
                ConfigConstants.CONFIG_KEY_AUTOCRLF,
                getCrlf()
            )
            create()
        }
        Git(repository).use { git ->
            git.add().addFilepattern(".").call()
            git.commit().setMessage("Add all").call()

            val oldVersionFile = "src/main/java/com/java/test/Class1.java"
            val targetFile = testProjectDir.root.resolve(oldVersionFile)
            getResourceFile<DiffCoverageSingleModuleTest>("git-diff-source-test-files/Class1GitTest.java")
                .copyTo(targetFile, true)

            git.add().addFilepattern(oldVersionFile).call()
            git.commit().setMessage("Added old file version").call()

            getResourceFile<DiffCoverageSingleModuleTest>("$TEST_PROJECT_RESOURCE_NAME/src").copyRecursively(
                testProjectDir.root.resolve("src"),
                true
            )
        }
    }
}
