package com.form.coverage

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class Git(private val projectRoot: File) {
    fun exec(vararg commands: String): Pair<Int, String> {
        val command = mutableListOf("git").apply { this += commands }

        val process = ProcessBuilder(command).apply {
            directory(projectRoot)
            redirectErrorStream(true)
        }.start()

        val processOutput = BufferedReader(InputStreamReader(
                process.inputStream,
                StandardCharsets.UTF_8
        )).useLines {
            it.joinToString("\n")
        }
        val exitCode = process.waitFor()

        return exitCode to processOutput
    }
}
