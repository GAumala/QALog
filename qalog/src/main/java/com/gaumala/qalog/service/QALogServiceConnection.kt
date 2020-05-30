package com.gaumala.qalog.service

import android.app.Activity
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import com.gaumala.qalog.R

class QALogServiceConnection(canLaunchSettings: Boolean = true) : ServiceConnection {
    var connected = false
    private set

    var canLaunchSettings = canLaunchSettings
    private set

    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        connected = true
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private class PermissionsDialogListener(val appCtx: Context): DialogInterface.OnClickListener {
        override fun onClick(dialog: DialogInterface, p1: Int) {
            dialog.dismiss()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${appCtx.packageName}"))
            startActivity(appCtx, intent, null)
        }

    }

    fun bind(activity: Activity) {
        if (Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(activity)) {
            if (connected)
                return

            val intent = Intent(activity, QALogService::class.java)
            activity.bindService(intent, this, Context.BIND_AUTO_CREATE)

        } else if (canLaunchSettings) {
            val appCtx = activity.applicationContext
            val listener = PermissionsDialogListener(appCtx)
            launchSettingsWithDialog(activity, listener)

            // avoid getting stuck in loop between activities
            // if permissions were not granted
            canLaunchSettings = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun launchSettingsWithDialog(ctx: Context,
                                         listener: PermissionsDialogListener) {
        AlertDialog.Builder(ctx)
            .setMessage(R.string.qa_log_permissions_required_msg)
            .setCancelable(true)
            .setPositiveButton(R.string.qa_log_goto_settings, listener)
            .show()
    }

    fun unbind(activity: Activity) {
        if (! connected)
            return

        activity.unbindService(this)
        connected = false
    }
}