package com.form.coverage.diff

import com.form.coverage.diff.parse.ModifiedLinesDiffParser
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.startWith
import java.io.File

class ModifiedLinesDiffParserTest : StringSpec({

    "collectModifiedLines should return empty map on empty list" {
        // setup
        val modifiedLinesDiffParser = ModifiedLinesDiffParser()

        // run
        val collectModifiedLines = modifiedLinesDiffParser.collectModifiedLines(emptyList())

        // assert
        collectModifiedLines shouldBe emptyMap()
    }

    "collectModifiedLines should throw when file path cannot be parsed" {
        // setup
        val diffContent = """
            +++ 
        """.trimIndent().lines()

        // run
        val exception = shouldThrow<IllegalArgumentException> {
            ModifiedLinesDiffParser().collectModifiedLines(diffContent)
        }

        // assert
        exception.message should startWith("Couldn't parse file relative path: ")
    }

    "collectModifiedLines should return empty map when diff file has few line breaks" {
        // setup
        val lines = listOf("", "")

        // run
        val collectModifiedLines = ModifiedLinesDiffParser().collectModifiedLines(lines)

        // assert
        collectModifiedLines shouldBe emptyMap()
    }

    "collectModifiedLines should throw when offset cannot be parsed" {
        // setup
        val diffContent = """
            --- path/file
            +++ path/file
            @@ invalid,offset @@
        """.trimIndent().lines()

        // run
        val exception = shouldThrow<IllegalArgumentException> {
            ModifiedLinesDiffParser().collectModifiedLines(diffContent)
        }

        // assert
        exception.message should startWith("Couldn't parse file's range information: ")
    }

    "collectModifiedLines should return modified and added lines used 'git diff' format" {
        // setup
        val modifiedFilePath = "some/file/path.ext"
        val toFileGitDiffFormat = "b/$modifiedFilePath"
        val diffContent = """
            diff --git a/$modifiedFilePath $toFileGitDiffFormat
            index 8a1218a..41da4de 100644
            --- a/$modifiedFilePath
            +++ b/$modifiedFilePath
            @@ -1,5 +1,8 @@
             a          #1
             b          #2
            +add row    #3 expected
             c          #4
            -d
            +d modify   #5 expected
             e          #6
            +f add      #7 expected
            +g add      #8 expected
        """.trimIndent().lines()
        val expected = mapOf(
            toFileGitDiffFormat to setOf(3, 5, 7, 8)
        )

        // run
        val collectModifiedLines = ModifiedLinesDiffParser().collectModifiedLines(diffContent)

        // assert
        collectModifiedLines shouldContainExactly expected
    }

    "collectModifiedLines should return modified and added lines when multiple modified files" {
        // setup
        val modifiedFile1 = "some/file/path1.ext"
        val modifiedFile3 = "some/file/path3.ext"
        val diffContent = """
            --- $modifiedFile1
            +++ $modifiedFile1
            @@ -1,2 +1,4 @@
             #1
            -2
            +#2 expected
            +#3 expected
            +#4 expected
            --- some/file/path2.ext
            +++ some/file/path22.ext
            @@ -1,2 +1 @@
             1
            -2
            --- $modifiedFile3
            +++ $modifiedFile3
            @@ -1 +1,2 @@
            +0
             1

            some service row
        """.trimIndent().lines()
        val expected = mapOf(
            modifiedFile1 to setOf(2, 3, 4),
            modifiedFile3 to setOf(1)
        )

        // run
        val collectModifiedLines = ModifiedLinesDiffParser().collectModifiedLines(diffContent)

        // assert
        collectModifiedLines shouldContainExactly expected
    }

    "collectModifiedLines should not skip modified lines when patch file contains empty lines"{
        // setup
        val diffFileContent = ModifiedLinesDiffParserTest::class.java.classLoader
            .getResource("testintPatch1.patch")!!.file
            .let(::File)
        val modifiedFileName =
            "jacoco-filtering-extension/src/main/kotlin/com/form/coverage/filters/ModifiedLinesFilter.kt"
        val modifiedLines: Set<Int> = (7..8)
            .union(18..32)
            .union(40..40)
            .union(43..44)
            .union(46..46)
            .union(48..48)
            .union(70..73)

        // run
        val collectModifiedLines = ModifiedLinesDiffParser().collectModifiedLines(diffFileContent.readLines())

        // assert
        collectModifiedLines shouldContainExactly mapOf(modifiedFileName to modifiedLines)
    }

    "collectModifiedLines should successfully parse diff of a patch file" {
        // setup
        val filePath = "jacoco-filtering-extension/src/test/resources/testintPatch1.patch"

        val diffContent = """
            --- jacoco-filtering-extension/src/test/resources/testintPatch1.patch
            +++ jacoco-filtering-extension/src/test/resources/testintPatch1.patch
            @@ -1,10 +1,10 @@
            -Index: jacoco-filtering-extension/src/main/kotlin/com/prev/coverage/filters/ModifiedLinesFilter.kt
            +Index: jacoco-filtering-extension/src/main/kotlin/com/form/coverage/filters/ModifiedLinesFilter.kt
             IDEA additional info:
             Subsystem: com.intellij.openapi.diff.impl.patch.CharsetEP
             <+>UTF-8
             ===================================================================
            ---- jacoco-filtering-extension/src/main/kotlin/com/prev/coverage/filters/ModifiedLinesFilter.kt
            -+++ jacoco-filtering-extension/src/main/kotlin/com/prev/coverage/filters/ModifiedLinesFilter.kt
            +--- jacoco-filtering-extension/src/main/kotlin/com/form/coverage/filters/ModifiedLinesFilter.kt
            ++++ jacoco-filtering-extension/src/main/kotlin/com/form/coverage/filters/ModifiedLinesFilter.kt
             @@ -4,10 +4,8 @@
              import org.jacoco.core.internal.analysis.filter.IFilter
              import org.jacoco.core.internal.analysis.filter.IFilterContext
              
        """.trimIndent().lines()

        // run
        val collectModifiedLines = ModifiedLinesDiffParser().collectModifiedLines(diffContent)

        // assert
        collectModifiedLines shouldContainExactly mapOf(filePath to setOf(1, 6, 7))
    }

    "collectModifiedLines should parse quoted paths" {
        // setup
        val filePath = "b/#1{ }.txt"

        val diffContent = """
            diff --git "a/1\173 \175.txt" "b/1\173 \175.txt"
            index 150c4fd..1ddd327 100644
            --- "a/1\173 \175.txt"
            +++ "b/\0431\173 \175.txt"
            @@ -1 +1,2 @@
             prev
            +new
        """.trimIndent().lines()

        // run
        val collectModifiedLines = ModifiedLinesDiffParser().collectModifiedLines(diffContent)

        // assert
        collectModifiedLines shouldContainExactly mapOf(filePath to setOf(2))
    }

})
