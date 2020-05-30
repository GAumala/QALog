package com.gaumala.qalog.service

import android.app.IntentService
import android.app.Service
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.IBinder
import com.gaumala.qalog.QA
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import androidx.core.content.FileProvider.getUriForFile
import kotlinx.coroutines.Dispatchers


class QAClearLogService: IntentService("ClearLogService") {
    override fun onHandleIntent(p0: Intent?) {
        QA.logger.reset()
    }
}