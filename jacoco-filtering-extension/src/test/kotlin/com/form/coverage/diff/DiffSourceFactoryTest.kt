package com.form.coverage.diff

import com.form.coverage.config.DiffSourceConfig
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.startWith
import io.kotest.matchers.types.shouldBeTypeOf
import java.io.File

class DiffSourceFactoryTest : StringSpec() {
    init {

        "diffSourceFactory should return file diff source" {
            // setup
            val filePath = "someFile"
            val diffConfig = buildDiffSourceOption(file = filePath)

            // run
            val diffSource = diffSourceFactory(File("."), diffConfig)

            // assert
            diffSource.shouldBeTypeOf<FileDiffSource>()
            diffSource.sourceDescription shouldBe "File: $filePath"
        }

        "diffSourceFactory should return url diff source" {
            // setup
            val url = "someUrl"
            val diffConfig = buildDiffSourceOption(url = url)

            // run
            val diffSource = diffSourceFactory(File("."), diffConfig)

            // assert
            diffSource.shouldBeTypeOf<UrlDiffSource>()
            diffSource.sourceDescription shouldBe "URL: $url"
        }

        "diffSourceFactory should return git diff source" {
            // setup
            val compareWith = "develop"
            val diffConfig = buildDiffSourceOption(git = compareWith)

            // run
            val diffSource = diffSourceFactory(File("."), diffConfig)

            // assert
            diffSource.shouldBeTypeOf<GitDiffSource>()
            diffSource.sourceDescription shouldBe "Git: diff $compareWith"
        }

        "diffSourceFactory should throw when no source specified" {
            // setup
            val diffConfig = buildDiffSourceOption()

            // run
            val exception = shouldThrow<IllegalStateException> {
                diffSourceFactory(File("."), diffConfig)
            }

            // assert
            exception.message should startWith("Expected Git configuration or file or URL diff source but all are blank")
        }

        "diffSourceFactory should throw when both sources specified" {
            // setup
            val diffConfig = buildDiffSourceOption(file = "file", url = "url", git = "master")

            // run
            val exception = shouldThrow<IllegalStateException> {
                diffSourceFactory(File("."), diffConfig)
            }

            // assert
            exception.message should startWith(
                "Expected only Git configuration or file or URL diff source more than one:"
            )
        }
    }

    private fun buildDiffSourceOption(file: String = "", url: String = "", git: String = ""): DiffSourceConfig {
        return DiffSourceConfig(file, url, git)
    }
}
