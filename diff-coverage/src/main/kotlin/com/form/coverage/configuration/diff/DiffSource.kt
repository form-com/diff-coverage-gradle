package com.form.coverage.configuration.diff

import com.form.coverage.configuration.DiffSourceConfiguration
import com.form.coverage.http.requestGet
import java.io.File


interface DiffSource {
    val sourceType: String
    val sourceLocation: String
    fun pullDiff(): List<String>
}

internal class FileDiffSource(
        override val sourceLocation: String,
        override val sourceType: String = "File"
) : DiffSource {
    override fun pullDiff(): List<String> {
        val file = File(sourceLocation)
        return if(file.exists() && file.isFile) {
            file.readLines()
        } else {
            throw RuntimeException("'$sourceLocation' not a file or doesn't exist")
        }
    }
}

internal class UrlDiffSource(
        override val sourceLocation: String,
        override val sourceType: String = "URL"
) : DiffSource {
    override fun pullDiff(): List<String> = requestGet(sourceLocation).lines()
}

fun getDiffSource(diffConfig: DiffSourceConfiguration): DiffSource {

    return when {
        diffConfig.file.isNotBlank() && diffConfig.url.isNotBlank() -> throw IllegalStateException(
                "Expected only file or URL diff source but found both: file=${diffConfig.file}, url=${diffConfig.url}"
        )

        diffConfig.file.isNotBlank() -> FileDiffSource(diffConfig.file)
        diffConfig.url.isNotBlank() -> UrlDiffSource(diffConfig.url)

        else -> throw IllegalStateException("Expected file or URL diff source but both are blank")
    }
}
