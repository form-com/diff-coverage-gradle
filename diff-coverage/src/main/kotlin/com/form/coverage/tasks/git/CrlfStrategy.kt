package com.form.coverage.tasks.git

import org.eclipse.jgit.lib.CoreConfig
import java.io.File

const val FILE_SEPARATOR: String = "\r\n"

fun getCrlf(lineSeparator: String = FILE_SEPARATOR): CoreConfig.AutoCRLF {
    return if (lineSeparator == "\r\n") {
        CoreConfig.AutoCRLF.TRUE
    } else {
        CoreConfig.AutoCRLF.INPUT
    }
}
