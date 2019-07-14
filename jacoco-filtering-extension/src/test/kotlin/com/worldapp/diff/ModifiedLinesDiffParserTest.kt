package com.worldapp.diff

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModifiedLinesDiffParserTest {

    @Test
    fun `collectModifiedLines should return empty map on empty list`() {
        // setup
        val modifiedLinesDiffParser = ModifiedLinesDiffParser()

        // run
        val collectModifiedLines = modifiedLinesDiffParser.collectModifiedLines(emptyList())

        // assert
        assertTrue(collectModifiedLines.isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `collectModifiedLines should throw when file path cannot be parsed`() {
        // setup
        val diffContent = """
            +++path/file
        """.trimIndent().lines()

        // run
        ModifiedLinesDiffParser().collectModifiedLines(diffContent)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `collectModifiedLines should throw when offset cannot be parsed`() {
        // setup
        val diffContent = """
            --- path/file
            +++ path/file
            @@ invalid,offset @@
        """.trimIndent().lines()

        // run
        ModifiedLinesDiffParser().collectModifiedLines(diffContent)
    }

    @Test
    fun `collectModifiedLines should return modified and added lines used 'git diff' format`() {
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
                toFileGitDiffFormat to setOf(3, 5, 7, 8))

        // run
        val collectModifiedLines = ModifiedLinesDiffParser().collectModifiedLines(diffContent)

        // assert
        assertEquals(expected, collectModifiedLines)
    }

    @Test
    fun `collectModifiedLines should return modified and added lines when multiple modified files`() {
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
        assertEquals(expected, collectModifiedLines)
    }
}
