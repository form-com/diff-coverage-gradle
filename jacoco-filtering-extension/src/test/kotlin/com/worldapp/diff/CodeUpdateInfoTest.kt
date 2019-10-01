package com.worldapp.diff

import io.kotlintest.data.forall
import io.kotlintest.properties.Gen.Companion.positiveIntegers
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.row

class ClassModificationsTest : StringSpec({

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
})

class CodeUpdateInfoTest : StringSpec({

    "getClassModifications should return empty ClassModifications when no such info" {
        assertAll(positiveIntegers()) { line ->
            // setup
            val codeUpdateInfo = CodeUpdateInfo(
                    mapOf("com/package/Class.java" to setOf(12))
            )

            // run
            val classModifications = codeUpdateInfo.getClassModifications(
                    ClassFile("UnknownClass.java", "com/package/UnknownClass")
            )

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
                    mapOf("module/src/main/java/com/package/Class.java" to set)
            )

            // run
            val infoExists = codeUpdateInfo.isInfoExists(
                    ClassFile("Class.java", "com/package/Class")
            )

            // assert
            infoExists shouldBe true
        }
    }

    "getClassModifications should return modifications for class when there is similar class name exists" {
        // setup
        val expectedLineNumber = 1
        val requestedLineNumber1 = 2
        val requestedLineNumber2 = 3
        val codeUpdateInfo = CodeUpdateInfo(
                mapOf(
                        "src/com/package/ClassSuffix.java" to setOf(requestedLineNumber1),
                        "src/com/package/Class.java" to setOf(expectedLineNumber),
                        "src/com/package/PrefixClass.java" to setOf(requestedLineNumber2)
                )
        )

        // run
        val modifications = codeUpdateInfo.getClassModifications(
                ClassFile("Class.java", "com/package/Class")
        )

        // assert
        modifications.isLineModified(2) shouldBe false
        modifications.isLineModified(3) shouldBe false
        modifications.isLineModified(1) shouldBe true
    }

    "isInfoExists should return false when modifications info doesn't exist for class" {
        forall(
                row(
                        "OtherClass.java",
                        "com/package/OtherClass",
                        mapOf("src/java/com/package/Class.java" to setOf(1, 2, 3))
                ),
                row(
                        "Class.java",
                        "com/package/Class",
                        mapOf("src/java/com/package/Class.java" to setOf())
                ),
                row(
                        "Class.java",
                        "com/package/Class",
                        mapOf()
                )
        ) { classSourceFile, classNameToCheck, mapOfModifiedLines ->
            // setup
            val codeUpdateInfo = CodeUpdateInfo(mapOfModifiedLines)

            // run
            val infoExists = codeUpdateInfo.isInfoExists(
                    ClassFile(classSourceFile, classNameToCheck)
            )

            // assert
            infoExists shouldBe false
        }
    }

})