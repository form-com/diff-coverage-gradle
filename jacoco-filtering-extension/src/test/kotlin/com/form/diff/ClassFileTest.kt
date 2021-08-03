package com.form.diff

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ClassFileTest: StringSpec({

    "ClassFile.path should return relative path when class has no package" {
        // setup
        val classFile = ClassFile("Class1.java", "Class1")

        // run // assert
        classFile.path shouldBe "Class1.java"
    }

    "ClassFile.path should return relative path when class has package" {
        // setup
        val classFile = ClassFile("Class1.java", "com/java/test/Class1")

        // run // assert
        classFile.path shouldBe "com/java/test/Class1.java"
    }
})
