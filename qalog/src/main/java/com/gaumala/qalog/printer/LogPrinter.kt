package com.gaumala.qalog.printer

import java.io.OutputStream

interface LogPrinter {
    fun countPrintedLines(): Int
    fun println(newLine: String)
    fun copy(outputStream: OutputStream)
    fun shutdown()
    fun reset()
}