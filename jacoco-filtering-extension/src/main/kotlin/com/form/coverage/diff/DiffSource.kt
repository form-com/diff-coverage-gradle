package com.form.coverage.diff

import com.form.coverage.config.DiffSourceConfig
import com.form.coverage.diff.git.JgitDiff
import com.form.coverage.http.executeGetRequest
import java.io.File

const val DEFAULT_PATCH_FILE_NAME: String = "diff.patch"

interface DiffSource {

    val sourceDescription: String
    fun pullDiff(): List<String>
    fun saveDiffTo(dir: File): File
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

    override fun saveDiffTo(dir: File): File {
        return File(filePath).copyTo(dir.resolve(DEFAULT_PATCH_FILE_NAME), true)
    }
}

internal class UrlDiffSource(
    private val url: String
) : DiffSource {
    override val sourceDescription = "URL: $url"

    private val diffContent: String by lazy { executeGetRequest(url) }

    override fun pullDiff(): List<String> = diffContent.lines()

    override fun saveDiffTo(dir: File): File {
        return dir.resolve(DEFAULT_PATCH_FILE_NAME).apply {
            writeText(diffContent)
        }
    }
}

internal class GitDiffSource(
    private val projectRoot: File,
    private val compareWith: String
) : DiffSource {

    private val diffContent: String by lazy {
        JgitDiff(projectRoot.resolve(".git")).obtain(compareWith)
    }

    override val sourceDescription = "Git: diff $compareWith"

    override fun pullDiff(): List<String> = diffContent.lines()

    override fun saveDiffTo(dir: File): File {
        return dir.resolve(DEFAULT_PATCH_FILE_NAME).apply {
            writeText(diffContent)
        }
    }
}

internal fun diffSourceFactory(
    projectRoot: File,
    diffSourceConfig: DiffSourceConfig
): DiffSource = when {

    diffSourceConfig.file.isNotBlank() && diffSourceConfig.url.isNotBlank() -> throw IllegalStateException(
        "Expected only Git configuration or file or URL diff source more than one: " +
                "git.diffBase=${diffSourceConfig.diffBase} file=${diffSourceConfig.file}, url=${diffSourceConfig.url}"
    )

    diffSourceConfig.file.isNotBlank() -> FileDiffSource(diffSourceConfig.file)
    diffSourceConfig.url.isNotBlank() -> UrlDiffSource(diffSourceConfig.url)
    diffSourceConfig.diffBase.isNotBlank() -> GitDiffSource(projectRoot, diffSourceConfig.diffBase)

    else -> throw IllegalStateException("Expected Git configuration or file or URL diff source but all are blank")
}
