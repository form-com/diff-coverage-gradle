package com.worldapp.coverage.configuration.diff

import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.endWith
import io.kotlintest.should
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import org.junit.rules.TemporaryFolder
import java.nio.file.Files
import java.nio.file.StandardOpenOption

class FileDiffSourceTest : StringSpec() {

    private val testProjectDir = TemporaryFolder().apply {
        create()
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
            val expectedLines = listOf( "1", "2", "3" )
            val newFile = testProjectDir.newFile().apply {
                Files.write(
                        toPath(),
                        expectedLines,
                        StandardOpenOption.APPEND
                )
            }

            val fileDiffSource = FileDiffSource(newFile.absolutePath)

            // run
            val diffLines = fileDiffSource.pullDiff()

            // assert
            diffLines shouldContainExactly expectedLines
        }

    }

    override fun closeResources() {
        super.closeResources()
        testProjectDir.delete()
    }
}

