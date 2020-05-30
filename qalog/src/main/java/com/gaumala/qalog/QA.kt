package com.gaumala.qalog

import java.io.File

object QA {
    internal val logger = Logger()

    fun log(entry: String) {
        logger.log(System.currentTimeMillis(), entry)
    }

    fun copyLog(dstFile: File) {
        logger.copy(dstFile.outputStream())
    }
}