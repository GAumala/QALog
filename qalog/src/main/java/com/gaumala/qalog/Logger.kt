package com.gaumala.qalog

import com.gaumala.qalog.printer.PrinterMsg
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import java.io.OutputStream

class Logger {
    internal val channel = Channel<PrinterMsg>(Channel.UNLIMITED)

    private fun sendMsg(msg: PrinterMsg) {
        channel.sendBlocking(msg)
    }

    fun log(timestamp: Long, text: String) {
        sendMsg(PrinterMsg.Append(timestamp, text))
    }

    fun copy(dst: OutputStream): CompletableDeferred<Unit> {
        val deferred = CompletableDeferred<Unit>()
        sendMsg(PrinterMsg.Copy(dst, deferred))
        return deferred
    }

    fun reset(): CompletableDeferred<Unit> {
        val deferred = CompletableDeferred<Unit>()
        sendMsg(PrinterMsg.Reset(deferred))
        return deferred
    }
}