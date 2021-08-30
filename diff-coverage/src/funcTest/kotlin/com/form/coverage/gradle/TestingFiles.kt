package com.form.coverage.gradle

import org.junit.rules.TemporaryFolder
import java.io.File

inline fun <reified T> getResourceFile(filePath: String): File {
    return T::class.java.classLoader
        .getResource(filePath)!!.file
        .let(::File)
}

inline fun <reified T> TemporaryFolder.copyFileFromResources(fileFrom: String, fileTo: String): File {
    return getResourceFile<T>(fileFrom).copyTo(newFile(fileTo), true)
}

inline fun <reified T> TemporaryFolder.copyDirFromResources(
    dirToCopy: String,
    destDir: String = dirToCopy
): File {
    getResourceFile<T>(dirToCopy).copyRecursively(
        newFolder(destDir),
        true
    )
    return root.resolve(destDir)
}

fun File.toUnixAbsolutePath(): String = absolutePath.replace("\\", "/")
