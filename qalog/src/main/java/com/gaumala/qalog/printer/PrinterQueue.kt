package com.gaumala.qalog.printer

import kotlinx.coroutines.*
import java.io.OutputStream
import java.lang.StringBuilder

class PrinterQueue(private val scope: CoroutineScope,
                   private val receiver: PrinterMsgReceiver,
                   private val printer: LogPrinter,
                   private val monitor: PrinterMonitor,
                   private val formatter: LogFormatter,
                   private val bufferMaxCapacity: Int) {

    private var isRunning = false
    private var lineCount = 0

    fun run() {
        if (isRunning)
            return
        isRunning = true

        scope.launch {
            lineCount = printer.countPrintedLines()
            if (lineCount != 0)
                showCount(this)

            while(isActive) {
                try {
                    val msg = receiver.awaitNewMsg()
                    process(this, msg)
                } catch (ex: CancellationException) {
                    // Job got cancelled
                }
            }

            printer.shutdown()
            monitor.shutdown()
        }
    }

    private suspend fun process(scope: CoroutineScope,
                                msg: PrinterMsg) {
        when (msg) {
            is PrinterMsg.Append ->
                processAppend(scope, msg.timestamp, msg.text)
            is PrinterMsg.Copy ->
                processCopy(msg.outputStream, msg.copyDeferred)
            is PrinterMsg.Reset ->
                processReset(scope, msg.resetDeferred)
        }
    }

    private fun processReset(scope: CoroutineScope,
                             resetDeferred: CompletableDeferred<Unit>) {
        printer.reset()
        resetMonitor(scope)
        resetDeferred.complete(Unit)
    }

    private fun processCopy(
        outputStream: OutputStream,
        copyDeferred: CompletableDeferred<Unit>) {
        printer.copy(outputStream)
        copyDeferred.complete(Unit)
    }

    private suspend fun processAppend(scope: CoroutineScope,
                                      firstTimestamp: Long,
                                      firstText: String) {
        val line = formatter.format(firstTimestamp, firstText)
        val builder = StringBuilder(line)

        // Try to get all buffered messages
        var extraMsg = receiver.awaitFollowupMsg()
        var totalBuffered = 2

        while (extraMsg != null) {
            // If extra buffered msg is not type append then it
            // should be processed after printing current batch
            val extraAppendMsg = extraMsg as? PrinterMsg.Append ?: break
            extraMsg = null

            val (timestamp, text) = extraAppendMsg
            builder.append('\n')
                .append(formatter.format(timestamp, text))

            // Limit the number of extras to avoid getting
            // caught in an infinite loop
            if (totalBuffered >= bufferMaxCapacity) break

            extraMsg = receiver.awaitFollowupMsg()
            totalBuffered += 1
        }

        val output = builder.toString()
        printer.println(output)
        updateMonitor(scope, output)

        if (extraMsg != null) {
            process(scope, extraMsg)
        }
    }

    private fun showCount(scope: CoroutineScope) {
        scope.launch(Dispatchers.Main) {
            monitor.showCount(lineCount)
        }
    }

    private fun resetMonitor(scope: CoroutineScope) {
        lineCount = 0
        showCount(scope)
    }

    private fun updateMonitor(scope: CoroutineScope, newLines: String) {
        val lineBreaks = newLines.count { it == '\n' }
        lineCount += when {
            lineBreaks > 0 -> lineBreaks + 1
            newLines.isNotEmpty() -> 1
            else -> 0
        }
        showCount(scope)
    }
}
