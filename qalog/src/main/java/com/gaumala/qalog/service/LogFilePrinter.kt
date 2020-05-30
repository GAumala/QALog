package com.gaumala.qalog.service

import com.gaumala.qalog.printer.LogPrinter
import java.io.*

class LogFilePrinter(private val file: File): LogPrinter {
    private var outputStream =
        PrintStream(FileOutputStream(file, true))

    override fun println(newLine: String) {
        try {
            outputStream.println(newLine)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    override fun copy(outputStream: OutputStream) {
        try {
            outputStream.flush()
            file.inputStream().use { it.copyTo(outputStream) }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    override fun reset() {
        try {
            outputStream.close()
            file.delete()
            outputStream = PrintStream(file.outputStream())
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    override fun countPrintedLines(): Int {
        var count = 0
        file.forEachLine { count += 1 }
        return count
    }

    override fun shutdown() {
        outputStream.close()
    }

}