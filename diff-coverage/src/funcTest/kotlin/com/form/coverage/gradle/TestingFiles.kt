package com.form.coverage.gradle

import java.io.File

inline fun <reified T> getResourceFile(filePath: String): File {
    return T::class.java.classLoader
        .getResource(filePath)!!.file
        .let(::File)
}

inline fun <reified T> File.copyFileFromResources(fileFrom: String, fileTo: String): File {
    val target: File = resolve(fileTo).apply {
        parentFile.mkdirs()
    }
    return getResourceFile<T>(fileFrom).copyTo(target, true)
}

inline fun <reified T> File.copyDirFromResources(
    dirToCopy: String,
    destDir: String = dirToCopy
): File {
    val target = resolve(destDir)
    getResourceFile<T>(dirToCopy).copyRecursively(target, true)
    return target
}

fun File.toUnixAbsolutePath(): String = absolutePath.replace("\\", "/")
