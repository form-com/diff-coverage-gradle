package com.form.coverage.diff

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.should
import io.kotest.matchers.string.endWith
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption

class FileDiffSourceTest : StringSpec() {

    private val testProjectDir: File by lazy {
        val file = Files.createTempDirectory("JgitDiffTest").toFile()
        afterSpec {
            file.delete()
        }
        file
    }

    init {
        "pullDiff should throw when file doesn't exist" {
            // setup
            val fileDiffSource = FileDiffSource("file-doesn't-exist")

            // run
            val exception = shouldThrow<RuntimeException> {
                fileDiffSource.pullDiff()
            }

            // assert
            exception.message should endWith("not a file or doesn't exist")
        }

        "pullDiff should throw when specified path is dir" {
            // setup
            val fileDiffSource = FileDiffSource(testProjectDir.newFolder().absolutePath)

            // run
            val exception = shouldThrow<RuntimeException> {
                fileDiffSource.pullDiff()
            }

            // assert
            exception.message should endWith("not a file or doesn't exist")
        }

        "pullDiff should return file lines" {
            // setup
            val expectedLines = listOf("1", "2", "3")
            val newFile = testProjectDir.newFile().apply {
                withContext(Dispatchers.IO) {
                    Files.write(toPath(), expectedLines, StandardOpenOption.APPEND)
                }
            }

            val fileDiffSource = FileDiffSource(newFile.absolutePath)

            // run
            val diffLines = fileDiffSource.pullDiff()

            // assert
            diffLines shouldContainExactly expectedLines
        }

    }

    private fun File.newFolder(): File {
        return resolve("${System.nanoTime()}").apply {
            mkdir()
        }
    }

    private fun File.newFile(): File {
        return resolve("${System.nanoTime()}").apply {
            createNewFile()
        }
    }
}

