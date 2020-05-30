package com.gaumala.qalog.sample

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.gaumala.qalog.QA
import com.gaumala.qalog.service.QALogServiceConnection


class MainActivity: AppCompatActivity() {
    private val serviceConnection = QALogServiceConnection()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val editText = findViewById<EditText>(R.id.edit_text)

        val logButton = findViewById<Button>(R.id.log_button)
        logButton.setOnClickListener {
            val text = editText.text.toString()
            if (text.isNotEmpty()) {
                QA.log(text)
                editText.text.clear()
            }
        }

        val launchButton = findViewById<Button>(R.id.launch_button)
        launchButton.setOnClickListener {
            val intent = Intent(this, SecondaryActivity::class.java)
            startActivity(intent)
        }
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