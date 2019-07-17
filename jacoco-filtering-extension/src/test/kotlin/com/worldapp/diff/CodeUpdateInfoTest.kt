package com.worldapp.diff

import io.kotlintest.data.forall
import io.kotlintest.properties.Gen.Companion.positiveIntegers
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.row

class ClassModificationsTest: StringSpec( {

    "isLineModified should return true or false depends on line is modified or not" {
        forall(
                row(true, 1, setOf(1, 2, 3)),
                row(false, 1, setOf()),
                row(false, 0, setOf(1, 2)),
                row(false, -1, setOf(1, 2)),
                row(false, 3, setOf(1, 2))
        ) { isModified, line, lines ->
            // setup
            val classModifications = ClassModifications(lines)

            // assert
            classModifications.isLineModified(line) shouldBe isModified
        }
    }
} )

class CodeUpdateInfoTest: StringSpec( {

    val className = "Class"
    "getClassModifications should return empty ClassModifications when no such info" {
        assertAll(positiveIntegers()) { line  ->
            // setup
            val codeUpdateInfo = CodeUpdateInfo(
                    mapOf(className to setOf(12))
            )

            // run
            val classModifications = codeUpdateInfo.getClassModifications("UnknownClass")

            // assert
            classModifications.isLineModified(line) shouldBe false
        }
    }


    "isInfoExists should return true when modifications info exists for class" {
        forall(
                row(setOf(1, 2, 3)),
                row(setOf(1, 2))
        ) { set ->
            // setup
            val codeUpdateInfo = CodeUpdateInfo(
                    mapOf(className to set)
            )

            // run
            val infoExists = codeUpdateInfo.isInfoExists(className)

            // assert
            infoExists shouldBe true
        }
    }

    "isInfoExists should return false when modifications info doesn't exist for class" {
        forall(
                row("UnknownClass", mapOf(className to setOf(1, 2, 3))),
                row(className, mapOf(className to setOf())),
                row(className, mapOf())
        ) { classNameToCheck, mapOfModifiedLines ->
            // setup
            val codeUpdateInfo = CodeUpdateInfo(mapOfModifiedLines)

            // run
            val infoExists = codeUpdateInfo.isInfoExists(classNameToCheck)

            // assert
            infoExists shouldBe false
        }
    }

} )