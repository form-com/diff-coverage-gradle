package com.form.coverage.tasks.git

import com.form.coverage.configuration.DiffSourceConfiguration
import com.form.coverage.configuration.GitConfiguration
import com.form.coverage.tasks.git.FileDiffSource
import com.form.coverage.tasks.git.GitDiffSource
import com.form.coverage.tasks.git.UrlDiffSource
import com.form.coverage.tasks.git.getDiffSource
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.startWith
import io.kotest.matchers.types.shouldBeTypeOf
import java.io.File

class DiffSourceKtTest : StringSpec({

    "getDiffSource should return file diff source" {
        // setup
        val filePath = "someFile"
        val diffConfig = DiffSourceConfiguration(file = filePath)

        // run
        val diffSource = getDiffSource(File("."), diffConfig)

        // assert
        diffSource.shouldBeTypeOf<FileDiffSource>()
        diffSource.sourceDescription shouldBe "File: $filePath"
    }

    "getDiffSource should return url diff source" {
        // setup
        val url = "someUrl"
        val diffConfig = DiffSourceConfiguration(url = url)

        // run
        val diffSource = getDiffSource(File("."), diffConfig)

        // assert
        diffSource.shouldBeTypeOf<UrlDiffSource>()
        diffSource.sourceDescription shouldBe "URL: $url"
    }

    "getDiffSource should return git diff source" {
        // setup
        val compareWith = "develop"
        val diffConfig = DiffSourceConfiguration(git = GitConfiguration(diffBase = compareWith))

        // run
        val diffSource = getDiffSource(File("."), diffConfig)

        // assert
        diffSource.shouldBeTypeOf<GitDiffSource>()
        diffSource.sourceDescription shouldBe "Git: diff $compareWith"
    }

    "getDiffSource should throw when no source specified" {
        // setup
        val diffConfig = DiffSourceConfiguration()

        // run
        val exception = shouldThrow<IllegalStateException> {
            getDiffSource(File("."), diffConfig)
        }

        // assert
        exception.message should startWith("Expected Git configuration or file or URL diff source but all are blank")
    }

    "getDiffSource should throw when both source specified" {
        // setup
        val diffConfig = DiffSourceConfiguration(file = "file", url = "url", git = GitConfiguration("master"))

        // run
        val exception = shouldThrow<IllegalStateException> {
            getDiffSource(File("."), diffConfig)
        }

        // assert
        exception.message should startWith(
                "Expected only Git configuration or file or URL diff source more than one:"
        )
    }
})
