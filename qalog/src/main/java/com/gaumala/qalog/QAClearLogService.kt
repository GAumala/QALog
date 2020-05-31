package com.gaumala.qalog

import android.app.IntentService
import android.content.Intent
import com.gaumala.qalog.QA


class QAClearLogService: IntentService("ClearLogService") {
    override fun onHandleIntent(p0: Intent?) {
        QA.logger.reset()
    }
}