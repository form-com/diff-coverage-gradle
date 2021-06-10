package com.form.coverage.tasks.git

import com.form.coverage.configuration.DiffSourceConfiguration
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.eclipse.jgit.lib.CoreConfig
import org.junit.Test

import org.junit.Assert.*
import java.io.File

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
