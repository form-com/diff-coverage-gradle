package com.worldapp.diff

import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.util.*
import java.util.regex.Pattern

class ModifiedLinesDiffParser {

    fun collectModifiedLines(filePath: File): Map<String, Set<Int>> {
        return collectModifiedLines(Files.readAllLines(filePath.toPath()))
    }

    fun collectModifiedLines(lines: List<String>): Map<String, Set<Int>> {
        val iterator = lines.listIterator()

        val fileNameToChangedLines = HashMap<String, Set<Int>>()
        while (iterator.hasNext()) {
            val patchedFileRow = moveToNextFile(iterator) ?: return fileNameToChangedLines
            val patchedFileRelativePath = parseFileRelativePath(patchedFileRow)
            log.info("Found modified file: $patchedFileRelativePath")

            val fileChangedLines = collectFilesChangedLines(iterator)

            if (fileChangedLines.isNotEmpty()) {
                fileNameToChangedLines[patchedFileRelativePath] = fileChangedLines
            }
        }
        return fileNameToChangedLines
    }

    private fun moveToNextFile(iterator: ListIterator<String>): String? {
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next.startsWith(FILE_NAME_SIGNS)) {
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
            if(nextLine.startsWith(HUNK_RANGE_INFO_SIGNS)) {
                val fileOffset = parseFileDiffBlockOffset(nextLine)
                fileChangedLines += obtainFilesAddedOrUpdatedLines(iterator, fileOffset)
            } else {
                break
            }
        }

        return fileChangedLines
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
                line.startsWith(ADDED_LINE_SIGN) -> modifiedOrAddedLinesNumbers += lineNumber
                line.startsWith(DELETED_LINE_SIGN) -> lineNumber -= 1
                !line.startsWith(NO_MOD_LINE_SIGN) -> {
                    iterator.previous()
                    return modifiedOrAddedLinesNumbers
                }
            }

            lineNumber++
        }
        return modifiedOrAddedLinesNumbers
    }

    private fun parseFileDiffBlockOffset(line: String): Int {
        val matcher = FILE_OFFSET_PATTERN.matcher(line)
        return if(matcher.find()) {
            matcher.group(1).toInt()
        } else {
            throw RuntimeException("Couldn't parse file's range information: $line")
        }
    }

    private fun parseFileRelativePath(diffFilePath: String): String {
        val matcher = FILE_RELATIVE_PATH_PATTERN.matcher(diffFilePath)
        return if(matcher.find()) {
            matcher.group(1)
        } else {
            throw RuntimeException("Couldn't parse file relative path: $diffFilePath")
        }
    }

    private companion object {
        val FILE_OFFSET_PATTERN = Pattern.compile("^@@.*\\+(\\d+)(,\\d+)? @@")!!
        val FILE_RELATIVE_PATH_PATTERN = Pattern.compile("^\\+\\+\\+\\s([\\w-_.<>$/]+)\\s*\\t*.*\$")!!

        val log = LoggerFactory.getLogger(ModifiedLinesDiffParser::class.java)

        const val ADDED_LINE_SIGN = '+'
        const val DELETED_LINE_SIGN = '-'
        const val NO_MOD_LINE_SIGN = ' '

        const val FILE_NAME_SIGNS = "+++"
        const val HUNK_RANGE_INFO_SIGNS = "@@"

        @JvmStatic
        fun main(args: Array<String>) {
            val matcher = FILE_OFFSET_PATTERN.matcher("@@ -1 +1 @@")
            println(matcher.find())
        }
    }
}


