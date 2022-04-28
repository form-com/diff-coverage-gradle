package com.form.coverage.diff.parse

import org.eclipse.jgit.util.QuotedString
import org.slf4j.LoggerFactory
import java.util.regex.Pattern

internal class ModifiedLinesDiffParser {

    fun collectModifiedLines(lines: List<String>): Map<String, Set<Int>> {
        val iterator = lines.listIterator()

        val fileNameToChangedLines = HashMap<String, Set<Int>>()
        while (iterator.hasNext()) {
            val patchedFileRow = moveToNextFile(iterator) ?: return fileNameToChangedLines
            val patchedFileRelativePath = parseFileRelativePath(patchedFileRow)
            log.debug("Found modified file: $patchedFileRelativePath")

            val fileChangedLines = collectFilesChangedLines(iterator)

            if (fileChangedLines.isNotEmpty()) {
                fileNameToChangedLines[patchedFileRelativePath] = fileChangedLines
            }
        }
        return fileNameToChangedLines
    }

    private fun parseFileRelativePath(diffFilePath: String): String {
        val parsedPath = parseFilePath(FILE_RELATIVE_PATH_PATTERN, diffFilePath)
            ?: parseFilePath(FILE_RELATIVE_PATH_QUOTED_PATTERN, diffFilePath, QuotedString.GIT_PATH::dequote)

        return parsedPath ?: throw IllegalArgumentException("Couldn't parse file relative path: $diffFilePath")
    }

    private fun parseFilePath(
        pattern: Pattern,
        diffFilePath: String,
        pathMapper: (String) -> String = { it }
    ): String? {
        val matcher = pattern.matcher(diffFilePath)
        return if (matcher.find()) {
            pathMapper(matcher.group(1))
        } else {
            null
        }
    }

    private fun moveToNextFile(iterator: ListIterator<String>): String? {
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next.startsWith(FILE_NAME_TO_SIGNS)) {
                return next
            }
        }
        return null
    }

    private fun collectFilesChangedLines(
        iterator: ListIterator<String>
    ): Set<Int> {
        val fileChangedLines = HashSet<Int>()
        while (iterator.hasNext()) {
            val nextLine = iterator.next()
            if (nextLine.startsWith(HUNK_RANGE_INFO_SIGNS)) {
                val fileOffset = parseFileDiffBlockOffset(nextLine)
                fileChangedLines += obtainFilesAddedOrUpdatedLines(iterator, fileOffset)
            } else {
                break
            }
        }

        return fileChangedLines
    }

    private fun parseFileDiffBlockOffset(line: String): Int {
        val matcher = FILE_OFFSET_PATTERN.matcher(line)
        return if (matcher.find()) {
            matcher.group(1).toInt()
        } else {
            throw IllegalArgumentException("Couldn't parse file's range information: $line")
        }
    }

    private fun obtainFilesAddedOrUpdatedLines(
        iterator: ListIterator<String>,
        fileOffset: Int
    ): Set<Int> {
        val modifiedOrAddedLinesNumbers = HashSet<Int>()
        var lineNumber = fileOffset
        while (iterator.hasNext()) {
            val line = iterator.next()
            when {
                line.isLineAdded() -> modifiedOrAddedLinesNumbers += lineNumber
                line.isLineDeleted() -> lineNumber -= 1
                line.isNoNewLineAtEndOfFile() -> Unit // do nothing
                line.isNotEmpty() && !line.isNoModLine() -> {
                    iterator.previous()
                    return modifiedOrAddedLinesNumbers
                }
            }

            lineNumber++
        }
        return modifiedOrAddedLinesNumbers
    }

    private fun String.isLineAdded() = startsWith(ADDED_LINE_SIGN)
    private fun String.isLineDeleted() = startsWith(DELETED_LINE_SIGN) && !startsWith(FILE_NAME_FROM_SIGNS)
    private fun String.isNoModLine() = startsWith(NO_MOD_LINE_SIGN)
    private fun String.isNoNewLineAtEndOfFile() = startsWith(NO_NEW_LINE_AT_END_OF_FILE)

    private companion object {
        val FILE_OFFSET_PATTERN = Pattern.compile("^@@.*\\+(\\d+)(,\\d+)? @@")!!
        val FILE_RELATIVE_PATH_PATTERN = Pattern.compile("^\\+\\+\\+\\s([^\"][^\\t]*)([\\t]+.*)?\$")!!
        val FILE_RELATIVE_PATH_QUOTED_PATTERN = Pattern.compile("^\\+\\+\\+\\s(\".+?\")\\s*\\t*.*\$")!!

        val log = LoggerFactory.getLogger(ModifiedLinesDiffParser::class.java)

        const val ADDED_LINE_SIGN = '+'
        const val DELETED_LINE_SIGN = '-'
        const val NO_MOD_LINE_SIGN = ' '
        const val NO_NEW_LINE_AT_END_OF_FILE = "\\ No newline at end of file"

        const val FILE_NAME_FROM_SIGNS = "--- "
        const val FILE_NAME_TO_SIGNS = "+++ "
        const val HUNK_RANGE_INFO_SIGNS = "@@"
    }
}


