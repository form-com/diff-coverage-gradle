package com.form.plugins

import com.form.coverage.tasks.git.JgitDiff
import com.form.coverage.tasks.git.getCrlf
import io.kotest.matchers.string.shouldContainOnlyOnce
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ConfigConstants
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class JGitTest {

    @get:Rule
    var tempTestDir: TemporaryFolder = TemporaryFolder()
    lateinit var rootProjectDir: File

    @Before
    fun setUp() {
        rootProjectDir = tempTestDir.root.resolve("git-test").apply {
            mkdir()
        }
        initGit()
    }

    @Test
    fun `jgit diff must encode file path with special symbols`() {
        rootProjectDir.resolve("# 1 } 2.txt").appendText("new-text\n")

        val diff: String = JgitDiff(rootProjectDir).obtain("HEAD")

        println(diff)
        diff shouldContainOnlyOnce "+++ \"b/\\043 1 \\175 2.txt\""
    }

    private fun initGit() {
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
