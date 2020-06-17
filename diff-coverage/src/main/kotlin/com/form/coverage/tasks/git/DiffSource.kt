package com.form.coverage.tasks.git

import com.form.coverage.configuration.DiffSourceConfiguration
import com.form.coverage.http.requestGet
import java.io.File


interface DiffSource {
    val sourceDescription: String
    fun pullDiff(): List<String>
}

internal class FileDiffSource(
        private val filePath: String
) : DiffSource {

    override val sourceDescription = "File: $filePath"

    override fun pullDiff(): List<String> {
        val file = File(filePath)
        return if (file.exists() && file.isFile) {
            file.readLines()
        } else {
            throw RuntimeException("'$filePath' not a file or doesn't exist")
        }
    }
}

internal class UrlDiffSource(
        private val url: String
) : DiffSource {
    override val sourceDescription = "URL: $url"

    override fun pullDiff(): List<String> = requestGet(url).lines()
}

internal class GitDiffSource(
        private val projectRoot: File,
        private val compareWith: String
) : DiffSource {
    override val sourceDescription = "Git: diff $compareWith"

    override fun pullDiff(): List<String> {
        return JgitDiff(projectRoot.resolve(".git"))
                .obtain(compareWith)
                .lines()
    }
}

fun getDiffSource(
        projectRoot: File,
        diffConfig: DiffSourceConfiguration
): DiffSource = when {

    diffConfig.file.isNotBlank() && diffConfig.url.isNotBlank() -> throw IllegalStateException(
            "Expected only Git configuration or file or URL diff source more than one: " +
                    "git.diffBase=${diffConfig.git.diffBase} file=${diffConfig.file}, url=${diffConfig.url}"
    )

    diffConfig.file.isNotBlank() -> FileDiffSource(diffConfig.file)
    diffConfig.url.isNotBlank() -> UrlDiffSource(diffConfig.url)
    diffConfig.git.diffBase.isNotBlank() -> GitDiffSource(projectRoot, diffConfig.git.diffBase)

    else -> throw IllegalStateException("Expected Git configuration or file or URL diff source but all are blank")
}
