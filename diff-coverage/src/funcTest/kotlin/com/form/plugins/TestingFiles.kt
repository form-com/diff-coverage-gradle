package com.form.plugins

import org.junit.rules.TemporaryFolder
import java.io.File

inline fun <reified T> getResourceFile(filePath: String): File {
    return T::class.java.classLoader
            .getResource(filePath)!!.file
            .let(::File)
}

inline fun <reified T> TemporaryFolder.copyResourceFile(fileFrom: String, fileTo: String): File {
    return getResourceFile<T>(fileFrom).copyTo(newFile(fileTo), true)
}

inline fun <reified T> TemporaryFolder.copyResourceDir(dirToCopy: String, destDir: String): String {
    getResourceFile<T>(dirToCopy).copyRecursively(
        newFolder(destDir),
        true
    )
    return root.resolve(destDir).toUnixAbsolutePath()
}

fun File.toUnixAbsolutePath() : String = absolutePath.replace("\\", "/")
