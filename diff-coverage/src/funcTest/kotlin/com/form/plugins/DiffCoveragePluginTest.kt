package com.form.plugins

import com.form.coverage.Git
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.rules.TemporaryFolder
import org.mockserver.integration.ClientAndServer
import org.mockserver.integration.ClientAndServer.startClientAndServer
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import java.io.File
import java.nio.file.Paths


class DiffCoveragePluginTest {

    @get:Rule
    var testProjectDir = TemporaryFolder()

    companion object {

        lateinit var mockServer: ClientAndServer

        const val SERVER_PORT = 1080

        val expectedReportFiles = arrayOf(
                "com.java.test",
                "index.html",
                "jacoco-resources",
                "jacoco-sessions.html"
        )

        @BeforeClass
        @JvmStatic
        fun startServer() {
            mockServer = startClientAndServer(SERVER_PORT)
        }

        @AfterClass
        @JvmStatic
        fun stopServer() {
            mockServer.stop()
        }
    }

    private lateinit var buildFile: File
    private lateinit var diffFilePath: String
    private lateinit var gradleRunner: GradleRunner

    @Before
    fun setup() {
        buildFile = testProjectDir.newFile("build.gradle")

        buildFile.appendText("""
            plugins {
                id 'com.form.diff-coverage'
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
                .absolutePath.replace("\\", "/")

        getResourceFile<DiffCoveragePluginTest>("src")
                .copyRecursively(
                        testProjectDir.newFolder("src"),
                        true
                )

        gradleRunner = GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(testProjectDir.root)
                .withTestKitDir(testProjectDir.newFolder())
                .apply {
                    // gradle testkit jacoco support
                    File("./build/testkit/testkit-gradle.properties")
                            .copyTo(File(projectDir, "gradle.properties"))
                }
                .apply {
                    withArguments("test").build()
                }
    }

    @Test
    fun `diff-coverage should create diffCoverage dir and full coverage with html report`() {
        // setup
        val baseReportDir = "build/custom/reports/dir/jacoco/"
        buildFile.appendText("""
            
            diffCoverageReport {
                diffSource {
                    file = '$diffFilePath'
                }
                reports {
                    html = true
                    fullCoverageReport = true
                    baseReportDir = '$baseReportDir'
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

        val reportDir = Paths.get(
                testProjectDir.root.absolutePath,
                baseReportDir
        )

        val diffCoverageReportDir = reportDir.resolve(
                Paths.get("diffCoverage", "html")
        ).toFile()
        assertThat(diffCoverageReportDir.list())
                .containsExactlyInAnyOrder(*expectedReportFiles)

        val fullReportDir = reportDir.resolve(
                Paths.get("fullReport", "html")
        ).toFile()
        assertThat(fullReportDir.list())
                .containsExactlyInAnyOrder(*expectedReportFiles)
    }

    @Test
    fun `diff-coverage should use git to generate diff`() {
        // setup
        File("../.gitignore").copyTo(testProjectDir.root.resolve(".gitignore"))
        val git = Git(testProjectDir.root)
        git.apply {
            exec("init")
            exec("add", ".gitignore")
            exec("commit", "-m", "\"initial commit\"")
        }

        val oldVersionFile = "src/main/java/com/java/test/Class1.java"
        testProjectDir.root.toPath().resolve(oldVersionFile).let {
            getResourceFile<DiffCoveragePluginTest>("Class1GitTest.java")
                    .copyTo(it.toFile(), true)
        }
        git.apply {
            exec("add", oldVersionFile)
            exec("commit", "-m", "\"add all\"")
        }

        getResourceFile<DiffCoveragePluginTest>("src").copyRecursively(
                testProjectDir.root.resolve(File("src")),
                true
        )

        buildFile.appendText("""
            
            diffCoverageReport {
                diffSource {
                    git.compareWith 'HEAD'
                }
                violationRules {
                    minLines = 0.7
                    failOnViolation = true 
                }
            }
        """.trimIndent())

        // run
        val result = gradleRunner
                .withArguments("diffCoverage")
                .buildAndFail()

        // assert
        assertTrue(result.output.contains("lines covered ratio is 0.6, but expected minimum is 0.7"))
        assertEquals(FAILED, result.task(":diffCoverage")!!.outcome)
    }

    @Test
    fun `diff-coverage should fail when git is not available`() {
        // setup
        buildFile.appendText("""
            
            diffCoverageReport {
                diffSource {
                    git.diffBase = 'HEAD'
                }
            }
        """.trimIndent())

        // run
        val result = gradleRunner
                .withArguments("diffCoverage")
                .buildAndFail()

        // assert
        assertTrue(result.output.contains("Git command 'git diff' exited with code: '129'."))
        assertEquals(FAILED, result.task(":diffCoverage")!!.outcome)
    }

    @Test
    fun `diff-coverage should fail on violation and generate html report`() {
        // setup
        val absolutePathBaseReportDir = testProjectDir.root.toPath()
                .resolve("build/absolute/path/reports/jacoco/")
                .toAbsolutePath()
                .toString()
                .replace("\\", "/")

        buildFile.appendText("""
            
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

        val diffCoverageReportDir = Paths.get(absolutePathBaseReportDir, "diffCoverage", "html").toFile()
        assertThat(diffCoverageReportDir.list())
                .containsExactlyInAnyOrder(*expectedReportFiles)
    }

    @Test
    fun `diff-coverage should not fail on violation when failOnViolation is false`() {
        // setup
        buildFile.appendText("""
            
            diffCoverageReport {
                diffSource.file = '$diffFilePath' 
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

    @Test
    fun `diff-coverage should get diff info by url`() {
        // setup
        val url = "http://localhost:$SERVER_PORT"
        mockServer.`when`(
                request().withMethod("GET")
        ).respond(response().withBody(
                File(diffFilePath).readText()
        ))

        buildFile.appendText("""
            
            diffCoverageReport {
                diffSource.url = '$url'
                violationRules {
                    minInstructions = 1 
                    failOnViolation = true 
                }
            }
        """.trimIndent())

        // run
        val result = gradleRunner
                .withArguments("diffCoverage")
                .buildAndFail()

        // assert
        assertTrue(result.output.contains("diffCoverage"))
        assertTrue(result.output.contains("instructions covered ratio is 0.5, but expected minimum is 1"))
        assertEquals(FAILED, result.task(":diffCoverage")!!.outcome)
    }
}
