package com.worldapp.plugins

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.nio.file.Paths

class DiffCoveragePluginTest {

    @Rule
    @JvmField
    var testProjectDir = TemporaryFolder()

    private lateinit var buildFile: File
    private lateinit var diffFilePath: String
    private lateinit var gradleRunner: GradleRunner

    @Before
    fun setup() {
        buildFile = testProjectDir.newFile("build.gradle")

        buildFile.appendText("""
            plugins {
                id 'com.worldapp.diff-coverage'
                id 'java'
                id 'jacoco'
            }
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                testImplementation 'junit:junit:4.12'
            }
        """.trimIndent())

        diffFilePath = getResourceFile<DiffCoveragePluginTest>("test.diff.file")
                .copyTo(testProjectDir.newFile("1.diff"), true)
                .absolutePath

        getResourceFile<DiffCoveragePluginTest>("src")
                .copyRecursively(
                        testProjectDir.newFolder("src"),
                        true
                )

        gradleRunner = GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(testProjectDir.root).apply {
                    withArguments("test").build()
                }
    }

    @Test
    fun `diff-coverage should create diffCoverage dir with html report`() {
        // setup
        buildFile.appendText("""
            
            diffCoverageReport {
                diffFile = '$diffFilePath' 
                reports {
                    html = true
                }
            }
        """.trimIndent())

        // run
        val result = gradleRunner
                .withArguments("diffCoverage")
                .build()

        // assert
        assertTrue(result.output.contains("diffCoverage"))
        assertEquals(SUCCESS, result.task(":diffCoverage")!!.outcome)

        val diffCoverageReportDir = Paths.get(
                testProjectDir.root.absolutePath,
                "build/reports/jacoco/diffCoverage"
        ).toFile()!!

        assertTrue(diffCoverageReportDir.list()!!.isNotEmpty())
    }

    @Test
    fun `diff-coverage should fail on violation`() {
        // setup
        buildFile.appendText("""
            
            diffCoverageReport {
                diffFile = '$diffFilePath' 
                violationRules {
                    minBranches = 0.6
                    minLines = 0.7
                    minInstructions = 0.8 
                    failOnViolation = true 
                }
            }
        """.trimIndent())

        // run
        val result = gradleRunner
                .withArguments("diffCoverage")
                .buildAndFail()

        // assert
        assertTrue(result.output.contains("instructions covered ratio is 0.5, but expected minimum is 0.8"))
        assertTrue(result.output.contains("branches covered ratio is 0.5, but expected minimum is 0.6"))
        assertTrue(result.output.contains("lines covered ratio is 0.6, but expected minimum is 0.7"))
        assertEquals(FAILED, result.task(":diffCoverage")!!.outcome)
    }

    @Test
    fun `diff-coverage should not fail on violation when failOnViolation is false`() {
        // setup
        buildFile.appendText("""
            
            diffCoverageReport {
                diffFile = '$diffFilePath' 
                violationRules {
                    minBranches = 1.0
                    minLines = 1.0
                    minInstructions = 1.0 
                    failOnViolation = false 
                }
            }
        """.trimIndent())

        // run
        val result = gradleRunner
                .withArguments("diffCoverage")
                .build()

        // assert
        assertTrue(result.output.contains("diffCoverage"))
        assertEquals(SUCCESS, result.task(":diffCoverage")!!.outcome)
    }
}