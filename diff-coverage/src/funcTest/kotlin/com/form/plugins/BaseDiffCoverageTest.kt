package com.form.plugins

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File

abstract class BaseDiffCoverageTest {

    @get:Rule
    var tempTestDir: TemporaryFolder = TemporaryFolder()

    lateinit var rootProjectDir: File
    lateinit var buildFile: File
    lateinit var diffFilePath: String
    lateinit var gradleRunner: GradleRunner

    /**
     * should be invoked in @Before test class method
     */
    fun initializeGradleTest() {
        val configuration: TestConfiguration = buildTestConfiguration()

        rootProjectDir = tempTestDir.copyDirFromResources<BaseDiffCoverageTest>(configuration.resourceTestProject)
        diffFilePath = rootProjectDir.resolve(configuration.diffFilePath).toUnixAbsolutePath()
        buildFile = rootProjectDir.resolve(configuration.rootBuildFilePath)

        gradleRunner = buildGradleRunner(rootProjectDir).apply {
            runTask("test")
        }
    }

    abstract fun buildTestConfiguration(): TestConfiguration
}

class TestConfiguration(
    val resourceTestProject: String,
    val rootBuildFilePath: String,
    val diffFilePath: String,
)
