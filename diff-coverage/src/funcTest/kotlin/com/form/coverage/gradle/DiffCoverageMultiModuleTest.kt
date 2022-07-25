package com.form.coverage.gradle

import com.form.coverage.gradle.DiffCoveragePlugin.Companion.DIFF_COV_TASK
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class DiffCoverageMultiModuleTest : BaseDiffCoverageTest() {

    companion object {
        const val TEST_PROJECT_RESOURCE_NAME = "multi-module-test-project"
    }

    override fun buildTestConfiguration() = TestConfiguration(
        TEST_PROJECT_RESOURCE_NAME,
        "build.gradle",
        "test.diff"
    )

    @BeforeEach
    fun setup() {
        initializeGradleTest()
    }

    @Test
    fun `diff-coverage should automatically collect jacoco configuration from submodules in multimodule project`() {
        // setup
        val baseReportDir = "build/custom/"
        val htmlReportDir = rootProjectDir.resolve(baseReportDir).resolve(File("diffCoverage", "html"))
        buildFile.appendText(
            """
            
            diffCoverageReport {
                diffSource.file = '$diffFilePath'
                reports {
                    html = true
                    baseReportDir = '$baseReportDir'
                }
                violationRules.failIfCoverageLessThan 0.9
            }
        """.trimIndent()
        )

        // run
        val result = gradleRunner.runTaskAndFail(DIFF_COV_TASK)

        // assert
        result.assertDiffCoverageStatusEqualsTo(FAILED)
            .assertOutputContainsStrings(
                "Fail on violations: true. Found violations: 1.",
                "Rule violated for bundle $TEST_PROJECT_RESOURCE_NAME: " +
                        "branches covered ratio is 0.5, but expected minimum is 0.9"
            )
        assertThat(htmlReportDir.list()).containsExactlyInAnyOrder(
            *expectedHtmlReportFiles("com.module1", "com.module2")
        )
    }

    @Test
    fun `diff-coverage plugin should auto-apply jacoco to project and subprojects`() {
        // setup
        val expectedCoverageRatio = 0.8
        buildFile.writeText(rootBuildScriptWithoutJacocoPlugin(expectedCoverageRatio))

        // run // assert
        gradleRunner.runTaskAndFail(DIFF_COV_TASK)
            .assertDiffCoverageStatusEqualsTo(FAILED)
            .assertOutputContainsStrings(
                "Fail on violations: true. Found violations: 1.",
                "Rule violated for bundle $TEST_PROJECT_RESOURCE_NAME: " +
                        "branches covered ratio is 0.5, but expected minimum is $expectedCoverageRatio"
            )
    }

    @Test
    fun `diff-coverage plugin should not apply jacoco plugin if jacoco auto-apply is disabled`() {
        // setup
        buildFile.writeText(rootBuildScriptWithoutJacocoPlugin(1.0))

        // disable jacoco auto-apply
        rootProjectDir.resolve("gradle.properties").appendText("""
            com.form.diff-coverage.auto-apply-jacoco=false
        """.trimIndent())

        // manually apply jacoco only to 'module1'
        rootProjectDir.resolve("module1").resolve("build.gradle").appendText("""

            apply plugin: 'jacoco'
        """.trimIndent())

        // run // assert
        gradleRunner
            .runTask(DIFF_COV_TASK)
            .assertDiffCoverageStatusEqualsTo(SUCCESS)
    }

    private fun rootBuildScriptWithoutJacocoPlugin(expectedCoverageRatio: Double) = """
        plugins {
                id 'java'
                id 'com.form.diff-coverage'
            }
            repositories {
                mavenCentral()
            }
            subprojects {
                apply plugin: 'java'
                repositories {
                    mavenCentral()
                }
                test {
                    useJUnitPlatform()
                }
            }
            diffCoverageReport {
                diffSource.file = '$diffFilePath'
                violationRules.failIfCoverageLessThan $expectedCoverageRatio
            }
    """.trimIndent()

}
