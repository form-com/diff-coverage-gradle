package com.form.coverage.diff.git

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.eclipse.jgit.lib.CoreConfig

class CrlfStrategyTest : StringSpec({

    "crlf should be `Auto` when line separator is \\r\\n" {
        val crlf = getCrlf("\r\n")
        crlf shouldBe CoreConfig.AutoCRLF.TRUE
    }

    "crlf should be `Input` when line separator is \\n" {
        val crlf = getCrlf("\n")
        crlf shouldBe CoreConfig.AutoCRLF.INPUT
    }
})
