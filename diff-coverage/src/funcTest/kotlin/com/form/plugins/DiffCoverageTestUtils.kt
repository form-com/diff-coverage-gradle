package com.form.plugins

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.rules.TemporaryFolder
import java.io.File

const val DIFF_COVERAGE_TASK = "diffCoverage"

fun buildGradleRunner(testProjectDir: TemporaryFolder): GradleRunner {
    return GradleRunner.create()
        .withPluginClasspath()
        .withProjectDir(testProjectDir.root)
        .withTestKitDir(testProjectDir.newFolder())
        .apply {
            // gradle testkit jacoco support
            javaClass.classLoader.getResourceAsStream("testkit-gradle.properties")?.use { inputStream ->
                File(projectDir, "gradle.properties").outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
}

fun GradleRunner.runTask(task: String): BuildResult = withArguments(task).build()

fun GradleRunner.runTaskAndFail(task: String): BuildResult = withArguments(task).buildAndFail()

