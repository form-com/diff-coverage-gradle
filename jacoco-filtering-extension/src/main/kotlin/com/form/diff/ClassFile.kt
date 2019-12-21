package com.form.diff

import java.nio.file.Paths

class ClassFile(
        private val sourceFileName: String,
        private val className: String
) {
    val path: String
        get() = Paths.get(className).parent
                .resolve(sourceFileName).toString()
                .replace("\\", "/")
}
