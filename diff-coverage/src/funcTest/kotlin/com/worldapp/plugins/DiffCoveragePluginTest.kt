package com.worldapp.plugins

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DiffCoveragePluginTest {

    @Rule
    @JvmField
    var testProjectDir = TemporaryFolder()

    private lateinit var buildFile: File

    @Before
    fun setup() {
        buildFile = testProjectDir.newFile("build.gradle")

        buildFile.appendText("""
            plugins {
                id 'com.worldapp.diff-coverage'
            }
            
        """.trimIndent())
    }

    @Test
    fun `diff-coverage plugin should add diffCoverage task`() {
        // run
        val result = GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(testProjectDir.root)
                .withArguments("tasks")
                .build()

        // assert
        assertTrue(result.output.contains("diffCoverage"))
        assertEquals(SUCCESS, result.task(":tasks")!!.outcome)
    }
}