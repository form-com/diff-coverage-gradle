package com.worldapp.diff

import org.junit.Assert.assertTrue
import org.junit.Test

class ModifiedLinesDiffParserTest {

    @Test
    fun collectModifiedLines() {
        // setup
        val modifiedLinesDiffParser = ModifiedLinesDiffParser()

        // run
        val collectModifiedLines = modifiedLinesDiffParser.collectModifiedLines(emptyList())

        // assert
        assertTrue(collectModifiedLines.isEmpty())
    }
}
