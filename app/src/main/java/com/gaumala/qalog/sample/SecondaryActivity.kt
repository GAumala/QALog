package com.gaumala.qalog.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gaumala.qalog.QA
import com.gaumala.qalog.service.QALogServiceConnection

class SecondaryActivity: AppCompatActivity() {
    private val serviceConnection = QALogServiceConnection()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.secondary_activity)
        QA.log("Create SecondaryActivity")
    }

    override fun onDestroy() {
        super.onDestroy()
        QA.log("Destroy SecondaryActivity")
    }

    override fun onStart() {
        super.onStart()
        serviceConnection.bind(this)
    }

    override fun onStop() {
        super.onStop()
        serviceConnection.unbind(this)
    }
}