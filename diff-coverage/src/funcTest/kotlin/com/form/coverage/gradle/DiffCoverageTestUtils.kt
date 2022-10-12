package com.form.coverage.gradle

import com.form.coverage.gradle.DiffCoveragePlugin.Companion.DIFF_COV_TASK
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

fun buildGradleRunner(
    projectRoot: File
): GradleRunner {
    return GradleRunner.create()
        .withPluginClasspath()
        .withProjectDir(projectRoot)
        .withTestKitDir(projectRoot.resolve("TestKitDir").apply {
            mkdir()
        })
        .apply {
            // gradle testkit jacoco support
            javaClass.classLoader.getResourceAsStream("testkit-gradle.properties")?.use { inputStream ->
                File(projectDir, "gradle.properties").outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
}

fun GradleRunner.runTask(vararg task: String): BuildResult {
    return tasksWithDebugOption(*task).build()
}

fun GradleRunner.runTaskAndFail(vararg task: String): BuildResult {
    return tasksWithDebugOption(*task).buildAndFail()
}

private fun GradleRunner.tasksWithDebugOption(vararg task: String): GradleRunner {
    val arguments: List<String> = mutableListOf(*task) + "-si"
    return withArguments(*arguments.toTypedArray())
}

fun expectedHtmlReportFiles(vararg packages: String): Array<String> = arrayOf(
    "index.html",
    "jacoco-resources",
    "jacoco-sessions.html"
) + packages

fun BuildResult.assertOutputContainsStrings(vararg expectedString: String): BuildResult {
    assertThat(output).contains(*expectedString)
    return this
}

fun BuildResult.assertDiffCoverageStatusEqualsTo(status: TaskOutcome): BuildResult {
    assertThat(task(":$DIFF_COV_TASK"))
        .isNotNull
        .extracting { it?.outcome }
        .isEqualTo(status)
    return this
}
