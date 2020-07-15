package com.form.coverage.diff

import com.form.coverage.diff.git.JgitDiff
import java.io.File
import java.net.URL

const val DEFAULT_PATCH_FILE_NAME: String = "diff.patch"

interface DiffSource {

    val sourceDescription: String
    fun pullDiff(): List<String>
    fun saveDiffTo(dir: File): File
}

class FileDiffSource(
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

class UrlDiffSource(
        private val url: String
) : DiffSource {
    override val sourceDescription = "URL: $url"

    private val diffContent: String by lazy { URL(url).readText() }

    override fun pullDiff(): List<String> = diffContent.lines()

    override fun saveDiffTo(dir: File): File {
        return dir.resolve(DEFAULT_PATCH_FILE_NAME).apply {
            writeText(diffContent)
        }
    }
}

class GitDiffSource(
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
