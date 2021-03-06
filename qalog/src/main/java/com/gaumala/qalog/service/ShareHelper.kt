package com.gaumala.qalog.service

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider.getUriForFile
import java.io.File
import android.os.Build
import com.gaumala.qalog.R

object ShareHelper {

    private fun createIntentBody(): String {
        return ("Device Info:"
                + "\nVERSION.RELEASE : " + Build.VERSION.RELEASE
                + "\nVERSION.INCREMENTAL : " + Build.VERSION.INCREMENTAL
                + "\nVERSION.SDK.NUMBER : " + Build.VERSION.SDK_INT
                + "\nBRAND : " + Build.BRAND
                + "\nMANUFACTURER : " + Build.MANUFACTURER
                + "\nMODEL : " + Build.MODEL
                + "\nPRODUCT : " + Build.MODEL
                + "\nBOARD : " + Build.BOARD
                + "\nTAGS : " + Build.TAGS
                + "\nBOOTLOADER : " + Build.BOOTLOADER
                + "\nDISPLAY : " + Build.DISPLAY
                + "\nHARDWARE : " + Build.HARDWARE
                + "\nDEVICE : " + Build.DEVICE
                + "\nFINGERPRINT : " + Build.FINGERPRINT
                + "\nUNKNOWN : " + Build.UNKNOWN)
    }

    fun sendShareIntent(ctx: Context, logFile: File) {
        val authority = "${ctx.packageName}.fileprovider"
        val uri = if (logFile.length() == 0L) null
                else getUriForFile(ctx, authority, logFile)
        val appName = ctx.getString(R.string.app_name)
        val title = ctx.getString(R.string.qa_log_share_with_title)
        val intent = Intent(ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, "$appName logs")
        intent.putExtra(Intent.EXTRA_TEXT, createIntentBody())
        intent.putExtra(Intent.EXTRA_STREAM, uri)

        val chooserIntent = Intent.createChooser(intent, title)
        chooserIntent.flags = FLAG_ACTIVITY_NEW_TASK
        startActivity(ctx, chooserIntent, null)
    }
}