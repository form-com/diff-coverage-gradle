package com.form.coverage.tasks.git

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.spec.tempfile
import io.kotest.matchers.should
import io.kotest.matchers.string.endWith
import io.kotest.matchers.string.startWith
import java.io.File
import java.lang.IllegalArgumentException
import java.nio.file.Files

class JgitDiffTest : StringSpec({

    val file : File by lazy {
        val file = Files.createTempDirectory("JgitDiffTest").toFile()
        afterSpec {
            file.delete()
        }
        file
    }

    "JgitDiff should throw when git is not initialized" {
        val exception = shouldThrow<IllegalArgumentException> {
            JgitDiff(file)
        }
        // assert
        exception.message should startWith("Git directory not found in the project root")
    }
})
