package com.form.coverage.gradle

import com.form.coverage.gradle.DiffCoveragePlugin.Companion.DIFF_COV_TASK
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.name
import kotlin.streams.toList

class DiffCoverageExcludesTest : BaseDiffCoverageTest() {

    companion object {
        const val TEST_PROJECT_RESOURCE_NAME = "test-excludes-classes-project"
    }

    override fun buildTestConfiguration() = TestConfiguration(
        TEST_PROJECT_RESOURCE_NAME,
        "build.gradle",
        "test.diff.file"
    )

    @BeforeEach
    fun setup() {
        initializeGradleTest()
    }

    @Test
    fun `diff-coverage should not fail when all not covered classes are excluded`() {
        // setup
        val dollarSign = '$'
        buildFile.appendText(
            """

            diffCoverageReport {
                diffSource.file = '$diffFilePath'
               
                violationRules {
                    failIfCoverageLessThan 1.0
                }
                
                excludeClasses = [
                    '**/CoveredClass${dollarSign}UncoveredNestedClass.*',
                    '**/excludes/**/UncoveredClass.*', 
                    '**/excludes/sub/**/*.*'
                ]
                
                reports {
                    html = true
                }
            }
        """.trimIndent()
        )

        // run
        val result = gradleRunner.runTask(DIFF_COV_TASK)

        // assert
        println(result.output)
        result.assertDiffCoverageStatusEqualsTo(SUCCESS)
            .assertOutputContainsStrings("Fail on violations: true. Found violations: 0")

        val htmlReportDir: Path = rootProjectDir.toPath().resolve("build/reports/jacoco/diffCoverage/html/")
        val classReportFiles: List<Path> = findAllFiles(htmlReportDir) { file ->
            file.name.endsWith("Class.html")
        }
        assertThat(classReportFiles)
            .hasSize(1).first()
            .extracting(Path::name)
            .isEqualTo("CoveredClass.html")
    }

    @Test
    fun `diff-coverage should fail when not covered classes are not excluded`() {
        // setup
        buildFile.appendText(
            """

            diffCoverageReport {
                diffSource.file = '$diffFilePath'
               
                violationRules {
                    failIfCoverageLessThan 1.0
                }
                
                excludeClasses = []
            }
        """.trimIndent()
        )

        // run // assert
        gradleRunner.runTaskAndFail(DIFF_COV_TASK)
            .assertDiffCoverageStatusEqualsTo(FAILED)
            .assertOutputContainsStrings("Fail on violations: true. Found violations: 2")
    }

    private fun findAllFiles(rootDir: Path, fileFilter: (Path) -> Boolean): List<Path> {
        return Files.find(
            rootDir,
            Int.MAX_VALUE,
            { filePath: Path, _: BasicFileAttributes -> fileFilter(filePath) }
        ).toList()
    }

}
