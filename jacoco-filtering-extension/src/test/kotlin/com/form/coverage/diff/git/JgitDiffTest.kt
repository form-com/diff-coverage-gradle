package com.form.coverage.diff.git

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.should
import io.kotest.matchers.string.match
import io.kotest.matchers.string.shouldContainOnlyOnce
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
            initGit(rootProjectDir)
            rootProjectDir.resolve("# 1 } 2.txt").appendText("new-text\n")

            val diff: String = JgitDiff(rootProjectDir).obtain("HEAD")

            diff shouldContainOnlyOnce "+++ \"b/\\043 1 \\175 2.txt\""
        }

        "jgit diff must throw if branch name is unknown" {
            // setup
            val branchName = "unknown-branch"
            initGit(rootProjectDir)

            // run
            val exception = shouldThrow<UnknownRevisionException> {
                JgitDiff(rootProjectDir).obtain(branchName)
            }

            // assert
            exception.message should match("Unknown revision '$branchName'")
        }

    }

    private fun initGit(rootProjectDir: File) {
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
    }

    private fun <R, T : GitCommand<R>> Git.command(
        command: Git.() -> T,
        configure: T.() -> Unit = {}
    ): R {
        return command().apply(configure).call()
    }

}
