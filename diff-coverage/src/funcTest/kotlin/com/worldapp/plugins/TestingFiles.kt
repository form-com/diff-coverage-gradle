package com.worldapp.plugins

import java.io.File

inline fun <reified T> getResourceFile(filePath: String): File {
    return T::class.java.classLoader
            .getResource(filePath)!!.file
            .let(::File)
}