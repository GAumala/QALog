package com.gaumala.qalog.service

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings

class QALogServiceConnection: ServiceConnection {
    var connected = false
    private set

    private var canLaunchSettings = true

    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        connected = true
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
    }

    fun bind(activity: Activity) {
        if (Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(activity)) {
            if (connected)
                return

            val intent = Intent(activity, QALogService::class.java)
            activity.bindService(intent, this, Context.BIND_AUTO_CREATE)

        } else if (canLaunchSettings) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${activity.packageName}"))
            activity.startActivity(intent)

            // avoid getting stuck in loop between activities
            // if permissions were not granted
            canLaunchSettings = false
        } else {
            val msg = "QALogger needs System Overlay permissions. " +
                    "Please go to settings and check the option \"Allow display over other apps\"."
            throw SecurityException(msg)
        }
    }

    fun unbind(activity: Activity) {
        if (! connected)
            return

        activity.unbindService(this)
        connected = false
    }
}