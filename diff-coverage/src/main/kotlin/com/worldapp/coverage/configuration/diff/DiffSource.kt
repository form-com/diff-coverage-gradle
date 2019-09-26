package com.worldapp.coverage.configuration.diff

import com.worldapp.coverage.configuration.DiffSourceConfiguration
import com.worldapp.coverage.http.requestGet
import org.slf4j.LoggerFactory
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
        return File(sourceLocation)
                .takeIf(File::exists)
                ?.takeIf(File::isFile)
                ?.readLines()
                ?: throw RuntimeException("'$sourceLocation' not a file or doesn't exist")
    }
}

internal class UrlDiffSource(
        override val sourceLocation: String,
        override val sourceType: String = "URL"
) : DiffSource {
    override fun pullDiff(): List<String> = requestGet(sourceLocation).lines().apply {
        LoggerFactory.getLogger(UrlDiffSource::class.java).warn("Lines: $this")
    }
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
