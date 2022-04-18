package com.form.coverage.diff

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import java.net.MalformedURLException
import java.net.UnknownHostException

class UrlDiffSourceTest : StringSpec() {

    init {
        "pullDiff should throw when url is invalid" {
            // setup
            val urlDiffSource = UrlDiffSource("invalid url format")

            // run
            // assert
            shouldThrow<IllegalArgumentException> {
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
    }
}
