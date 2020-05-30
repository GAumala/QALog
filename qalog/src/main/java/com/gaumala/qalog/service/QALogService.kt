package com.gaumala.qalog.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.gaumala.qalog.QA
import com.gaumala.qalog.printer.*
import com.gaumala.qalog.printer.DefaultFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import java.io.File

open class QALogService: Service() {

    companion object {
        const val DEFAULT_BUFFER_MAX_CAPACITY = 10
        const val DEFAULT_BUFFER_TIMEOUT = 1000L
    }

    private val ui by lazy {
        UI(this)
    }

    private val coroutineScope by lazy {
        CoroutineScope(Job())
    }

    private val printerQueue by lazy {
        val receiver =
            PrinterMsgReceiver(bufferTimeout, QA.logger.channel)
        PrinterQueue(
            scope = coroutineScope,
            formatter = createFormatter(),
            printer = createPrinter(),
            monitor = createMonitor(),
            receiver = receiver,
            bufferMaxCapacity = bufferMaxCapacity)
    }

    /**
     * Amount of time in milliseconds that queue should
     * wait for any followup log writes before calling
     * {@link LogPrinter#print(String)} with the buffered
     * text
     */
    open val bufferTimeout: Long = DEFAULT_BUFFER_TIMEOUT

    /**
     * Maximum amount of log writes that the queue should
     * buffer before {@link LogPrinter#print(String)} with
     * the buffered text. This has precedence over timeout.
     */
    open val bufferMaxCapacity: Int = DEFAULT_BUFFER_MAX_CAPACITY

    /**
     * Override this method to use a custom formatter
     * @return The formatter to use for logging
     */
    open fun createFormatter(): LogFormatter = DefaultFormatter()

    /**
     * Override this method to use a custom printer
     * @return The printer to use for logging
     */
    open fun createPrinter(): LogPrinter {
        val file = File(cacheDir, "QALog.txt")
        return LogFilePrinter(file)
    }

    /**
     * Override this method to use a custom monitor
     * @return The monitor to use with the printer
     */
    open fun createMonitor(): PrinterMonitor {
        return NotificationMonitor(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.coroutineContext.cancel()
        ui.clear()
    }

    override fun onBind(p0: Intent?): IBinder? {
        printerQueue.run()
        ui.setOnClickListener {
            ShareHelper.shareLogs(this@QALogService, coroutineScope)
        }
        return object: Binder() { }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return false
    }

}