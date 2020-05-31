package com.gaumala.qalog.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat

@Suppress("DEPRECATION")
fun notificationBuilderCompat(ctx: Context,
                              channelId: String): NotificationCompat.Builder {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        NotificationCompat.Builder(ctx, channelId)
    else
        NotificationCompat.Builder(ctx)

}


@Suppress("DEPRECATION")
fun getMediumPriorityConst(): Int =
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
        Notification.PRIORITY_DEFAULT
    else
        NotificationManager.IMPORTANCE_DEFAULT


fun Context.registerNotificationChannelCompat(
    channelId: String,
    sound: Uri?,
    audioAttributes: AudioAttributes?,
    priority: Int,
    name: String,
    description: String) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val mChannel = NotificationChannel(channelId, name, priority)
        mChannel.description = description
        mChannel.setSound(sound, audioAttributes)

        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }
}