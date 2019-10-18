package com.form.coverage.configuration.diff

import com.form.coverage.http.requestGet
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockkStatic
import java.net.MalformedURLException
import java.net.UnknownHostException

class UrlDiffSourceTest : StringSpec() {

    init {
        "pullDiff should throw when url is invalid" {
            // setup
            val urlDiffSource = UrlDiffSource("invalid url format")

            // run
            // assert
            shouldThrow<MalformedURLException> {
                urlDiffSource.pullDiff()
            }
        }

        "pullDiff should throw when url doesn't exist" {
            // setup
            val urlDiffSource = UrlDiffSource("http://1.html")

            // run
            // assert
            shouldThrow<UnknownHostException> {
                urlDiffSource.pullDiff()
            }
        }

        "pullDiff should return content as lines" {
            // setup
            mockkStatic("com.form.coverage.http.HttpRequestUtilsKt")
            val expectedLines = listOf("1", "3", "0")
            every { requestGet(any()) } returns expectedLines.joinToString("\n")

            val urlDiffSource = UrlDiffSource("http://ok-url.com")

            // run
            val diffLines = urlDiffSource.pullDiff()

            // assert
            diffLines shouldContainExactly expectedLines
        }
    }
}
