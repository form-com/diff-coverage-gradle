package com.form.coverage.configuration.diff

import com.form.coverage.configuration.DiffSourceConfiguration
import io.kotlintest.matchers.startWith
import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec

class DiffSourceKtTest : StringSpec({

    "getDiffSource should return file diff source" {
        // setup
        val filePath = "someFile"
        val diffConfig = DiffSourceConfiguration(file = filePath)

        // run
        val diffSource = getDiffSource(diffConfig)

        // assert
        diffSource.shouldBeTypeOf<FileDiffSource>()
        diffSource.sourceType shouldBe "File"
        diffSource.sourceLocation shouldBe filePath
    }

    "getDiffSource should return url diff source" {
        // setup
        val url = "someUrl"
        val diffConfig = DiffSourceConfiguration(url = url)

        // run
        val diffSource = getDiffSource(diffConfig)

        // assert
        diffSource.shouldBeTypeOf<UrlDiffSource>()
        diffSource.sourceType shouldBe "URL"
        diffSource.sourceLocation shouldBe url
    }

    "getDiffSource should throw when no source specified" {
        // setup
        val diffConfig = DiffSourceConfiguration()

        // run
        val exception = shouldThrow<IllegalStateException> {
            getDiffSource(diffConfig)
        }

        // assert
        exception.message should startWith("Expected file or URL diff source but both are blank")
    }

    "getDiffSource should throw when both source specified" {
        // setup
        val diffConfig = DiffSourceConfiguration(file = "file", url = "url")

        // run
        val exception = shouldThrow<IllegalStateException> {
            getDiffSource(diffConfig)
        }

        // assert
        exception.message should startWith("Expected only file or URL diff source but found both:")
    }
})
