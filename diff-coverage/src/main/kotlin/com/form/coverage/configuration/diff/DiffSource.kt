package com.form.coverage.configuration.diff

import com.form.coverage.Git
import com.form.coverage.configuration.DiffSourceConfiguration
import com.form.coverage.http.requestGet
import java.io.File


interface DiffSource {
    val sourceType: String
    val sourceLocation: String
    fun pullDiff(): List<String>
}

internal class FileDiffSource(
        override val sourceLocation: String
) : DiffSource {

    override val sourceType: String = "File"

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
        override val sourceLocation: String
) : DiffSource {
    override val sourceType: String = "URL"

    override fun pullDiff(): List<String> = requestGet(sourceLocation).lines()
}

internal class GitDiffSource(
        private val projectRoot: File,
        override val sourceLocation: String
) : DiffSource {
    override val sourceType: String = "Git"

    override fun pullDiff(): List<String> {
        val result = Git(projectRoot).exec("diff", "--no-color", "--minimal", sourceLocation)
        if (result.first != 0) {
            throw Exception("Git command 'git diff' exited with code: '${result.first}'. Output: ${result.second}")
        }
        return result.second.lines()
    }
}

fun getDiffSource(projectRoot: File, diffConfig: DiffSourceConfiguration): DiffSource {

    return when {
        diffConfig.file.isNotBlank() && diffConfig.url.isNotBlank() -> throw IllegalStateException(
                "Expected only Git configuration or file or URL diff source more than one: " +
                        "git.diffBase=${diffConfig.git.diffBase} file=${diffConfig.file}, url=${diffConfig.url}"
        )

        diffConfig.file.isNotBlank() -> FileDiffSource(diffConfig.file)
        diffConfig.url.isNotBlank() -> UrlDiffSource(diffConfig.url)
        diffConfig.git.diffBase.isNotBlank() -> GitDiffSource(projectRoot, diffConfig.git.diffBase)

        else -> throw IllegalStateException("Expected Git configuration or file or URL diff source but all are blank")
    }
}
