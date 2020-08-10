package com.form.diff

import java.nio.file.Path
import java.nio.file.Paths

class ClassFile(
        private val sourceFileName: String,
        private val className: String
) {
    val path: String
        get() = Paths.get(className).let {
            if (it.parent == null) {
                sourceFileName
            } else {
                it.parent.resolveWithNormalize(sourceFileName)
            }
        }

    private fun Path.resolveWithNormalize(fileName: String): String {
        return resolve(fileName)
                .toString()
                .replace("\\", "/")
    }
}
