package com.gaumala.qalog.printer

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeoutOrNull

class PrinterMsgReceiver(private val bufferTimeout: Long,
                         private val channel: Channel<PrinterMsg>) {

    suspend fun awaitNewMsg(): PrinterMsg = channel.receive()

    suspend fun awaitFollowupMsg(): PrinterMsg? {
        return withTimeoutOrNull(bufferTimeout) { channel.receive() }
    }

}