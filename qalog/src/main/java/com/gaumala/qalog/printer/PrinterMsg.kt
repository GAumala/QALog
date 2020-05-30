package com.gaumala.qalog.printer

import kotlinx.coroutines.CompletableDeferred
import java.io.OutputStream

sealed class PrinterMsg {
    data class Append(val timestamp: Long, val text: String): PrinterMsg()
    data class Copy(val outputStream: OutputStream,
                    val copyDeferred: CompletableDeferred<Unit>): PrinterMsg()
    data class Reset(val resetDeferred: CompletableDeferred<Unit>): PrinterMsg()
}