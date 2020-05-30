package com.gaumala.qalog

import com.gaumala.qalog.printer.*
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

fun ByteArrayOutputStream.getStreamedText(): String {
    val array = toByteArray()
    return array.toString(Charset.defaultCharset())
}

fun LogPrinter.getPrintedText(): String {
    val baos = ByteArrayOutputStream()
    copy(baos)
    return baos.getStreamedText()
}

fun createTestLogger(): Logger {
    return Logger()
}

val testFormatter = object: LogFormatter {
    override fun format(timestamp: Long, text: String): String {
        return "[$timestamp] $text"
    }
}

fun Logger.runQueue(scope: CoroutineScope,
                    printer: LogPrinter,
                    monitor: PrinterMonitor = mockk(relaxed = true)): PrinterQueue {
    val receiver = PrinterMsgReceiver(1000L, channel)
    val queue = PrinterQueue(
        scope = scope,
        receiver = receiver,
        formatter = testFormatter,
        monitor = monitor,
        bufferMaxCapacity = 10,
        printer = printer)
    queue.run()
    return queue
}

