package com.form.coverage.diff.git

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.should
import io.kotest.matchers.string.match
import io.kotest.matchers.string.shouldContainOnlyOnce
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.startWith
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.GitCommand
import org.eclipse.jgit.lib.ConfigConstants
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.File
import java.nio.file.Files

class JgitDiffTest : StringSpec() {
    private lateinit var rootProjectDir: File

    init {
        beforeEach {
            rootProjectDir = Files.createTempDirectory("JgitDiffTest").toFile()
        }
        afterEach {
            rootProjectDir.delete()
        }

        "JgitDiff should throw when git is not initialized"{
            val exception = shouldThrow<IllegalArgumentException> {
                JgitDiff(rootProjectDir)
            }
            // assert
            exception.message should startWith("Git directory not found in the project root")
        }

        "jgit diff must encode file path with special symbols" {
            Git(initRepository(rootProjectDir)).use { git ->
                rootProjectDir.resolve("# 1 } 2.txt").appendText("new-text\n")
                git.command(Git::add) { addFilepattern(".") }
            }

            val diff: String = JgitDiff(rootProjectDir).obtain("HEAD")

            diff shouldContainOnlyOnce "+++ \"b/\\043 1 \\175 2.txt\""
        }

        "jgit diff must throw if branch name is unknown" {
            // setup
            val branchName = "unknown-branch"
            initRepository(rootProjectDir)

            // run
            val exception = shouldThrow<UnknownRevisionException> {
                JgitDiff(rootProjectDir).obtain(branchName)
            }

            // assert
            exception.message should match(
                "Unknown revision '$branchName'. Available branches: refs/heads/master"
            )
        }

        "jgit diff must merge changes from target branch" {
            // setup
            val master = "master"
            val testBranch = "current_branch"

            val testFile: File = rootProjectDir.resolve("test-file.txt").apply {
                writeText("""
                    1
                    2
                    3
                    
                """.trimIndent())
            }
            Git(initRepository(rootProjectDir)).use { git ->
                // create new branch, checkout and apply changes with further commit
                git.apply {
                    checkout(testBranch, true)
                    writeToFileAndCommit(
                            testFile,
                            """
                                1
                                2 $testBranch
                                3
                                
                            """.trimIndent()
                    )
                }
                // checkout master and apply changes with further commit
                git.apply {
                    checkout(master)
                    writeToFileAndCommit(
                            testFile,
                            """
                                1 $master
                                2
                                3
                                
                            """.trimIndent()
                    )
                }
                // checkout back to custom branch and apply changes without commit
                git.apply {
                    checkout(testBranch)
                    testFile.writeText(
                            """
                                1
                                2 $testBranch
                                3 $testBranch
                                
                            """.trimIndent()
                    )
                    git.command(Git::add) { addFilepattern(".") }
                }
            }

            // run
            val actualDiff: String = JgitDiff(rootProjectDir).obtain(master)

            // assert
            val expectedDiff = """
                 1
                -2
                -3
                +2 $testBranch
                +3 $testBranch
                
            """.trimIndent()
            actualDiff shouldEndWith expectedDiff
        }
    }

    private fun initRepository(rootProjectDir: File): Repository {
        val repository: Repository = FileRepositoryBuilder.create(File(rootProjectDir, ".git")).apply {
            config.setEnum(
                ConfigConstants.CONFIG_CORE_SECTION,
                null,
                ConfigConstants.CONFIG_KEY_AUTOCRLF,
                getCrlf()
            )
            create()
        }
        Git(repository).use { git ->
            git.command(Git::add) { addFilepattern(".") }
            git.command(Git::commit) { message = "Add all" }
        }
        return repository
    }

    private fun Git.writeToFileAndCommit(file: File, content: String) {
        file.writeText(content)
        command(Git::add) { addFilepattern(".") }
        command(Git::commit) { message = "commit msg" }
    }

    private fun Git.checkout(branchName: String, createNew: Boolean = false) {
        command(Git::checkout) {
            setName(branchName)
            setCreateBranch(createNew)
        }
    }

    private fun <R, T : GitCommand<R>> Git.command(
        command: Git.() -> T,
        configure: T.() -> Unit = {}
    ): R {
        return command().apply(configure).call()
    }

}
