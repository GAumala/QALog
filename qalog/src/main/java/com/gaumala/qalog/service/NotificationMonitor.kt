package com.gaumala.qalog.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.gaumala.qalog.QAClearLogService
import com.gaumala.qalog.R
import com.gaumala.qalog.printer.PrinterMonitor
import com.gaumala.qalog.utils.*

class NotificationMonitor(private val ctx: Context): PrinterMonitor {
    companion object {
        private const val notificationId = 416897
        private const val channelId = "QALogChannel"
        private val priority = getMediumPriorityConst()
    }

    init {
        val name = ctx.getString(R.string.qa_log)
        val descriptionText =
            ctx.getString(R.string.qa_log_notif_channel_desc)
        ctx.registerNotificationChannelCompat(
            channelId = channelId,
            priority = priority,
            description = descriptionText,
            name = name,
            sound = null,
            audioAttributes = null)
    }

    private fun createClearAction(): NotificationCompat.Action {
        val icon = R.drawable.ic_qa_log_delete_forever_black_24dp
        val name = ctx.getString(R.string.qa_log_clear_log)
        val intent = Intent(ctx, QAClearLogService::class.java)
        val pendingIntent = PendingIntent.getService(
            ctx, 0, intent, 0)
        return NotificationCompat.Action(icon, name, pendingIntent)
    }

    private fun createNotification(count: Int): Notification {
        val appName = ctx.getString(R.string.app_name)
        val contentText = ctx.resources.getQuantityString(
            R.plurals.qa_log_lines_logged, count, appName, count)

        return notificationBuilderCompat(ctx, channelId)
            .setSmallIcon(R.drawable.ic_qa_log_adb_black_24dp)
            .setContentTitle(ctx.getText(R.string.app_name))
            .setContentText(contentText)
            .setOnlyAlertOnce(true)
            .addAction(createClearAction())
            .setOngoing(true)
            .setLocalOnly(true)
            .build()
    }

    private fun push(count: Int) {
        val manager = NotificationManagerCompat.from(ctx)
        manager.notify(notificationId, createNotification(count))
    }

    private fun remove() {
        val manager = NotificationManagerCompat.from(ctx)
        manager.cancel(notificationId)
    }

    override fun showCount(count: Int) {
        if (count > 0)
            push(count)
        else
            remove()
    }

    override fun shutdown() {
        remove()
    }

}