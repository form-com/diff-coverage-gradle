package com.form.coverage.diff.git

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.should
import io.kotest.matchers.string.startWith
import java.io.File
import java.lang.IllegalArgumentException
import java.nio.file.Files
import io.kotest.matchers.string.shouldContainOnlyOnce
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ConfigConstants
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

class JgitDiffTest : StringSpec() {

    init {
        val rootProjectDir: File by lazy {
            val file = Files.createTempDirectory("JgitDiffTest").toFile()
            afterSpec {
                file.delete()
            }
            file
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

            println(diff)
            diff shouldContainOnlyOnce "+++ \"b/\\043 1 \\175 2.txt\""
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
            git.add().addFilepattern(".").call()
            git.commit().setMessage("Add all").call()
        }
    }
}
