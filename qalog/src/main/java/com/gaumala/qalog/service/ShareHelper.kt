package com.gaumala.qalog.service

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider.getUriForFile
import com.gaumala.qalog.QA
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import android.os.Build
import com.gaumala.qalog.R


object ShareHelper {
    private const val authority = "com.gaumala.qalog.fileprovider"

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

    private fun sendShareIntent(ctx: Context, logFile: File) {
        val uri = if (logFile.length() == 0L) null
                else getUriForFile(ctx, authority, logFile)
        val appName = ctx.getString(R.string.app_name)
        val intent = Intent(ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, "Logs from $appName")
        intent.putExtra(Intent.EXTRA_TEXT, createIntentBody())
        intent.putExtra(Intent.EXTRA_STREAM, uri)

        val chooserIntent = Intent.createChooser(intent, "Share logs with...")
        chooserIntent.flags = FLAG_ACTIVITY_NEW_TASK
        startActivity(ctx, chooserIntent, null)
    }

    private fun createCopyDstFile(ctx: Context): File {
        val logPath = File(ctx.cacheDir, "qa_log")
        logPath.mkdir()
        return File(logPath, "exported_log.txt")
    }

    fun shareLogs(ctx: Context, scope: CoroutineScope)  {
        scope.launch {
            val file = createCopyDstFile(ctx)
            val outputStream = file.outputStream()
            QA.logger.copy(outputStream).await()
            outputStream.close()

            scope.launch(Dispatchers.Main) {
                sendShareIntent(ctx, file)
            }
        }
    }
}