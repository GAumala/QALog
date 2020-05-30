package com.gaumala.qalog.printer

import java.text.SimpleDateFormat
import java.util.*

internal class DefaultFormatter: LogFormatter {
    val simpleFormat = SimpleDateFormat("HH:mm:ss:SSS")
    override fun format(timestamp: Long, text: String): String {
        val date = Date(timestamp)
        val formattedTime = simpleFormat.format(date)
        return "[$formattedTime] $text"
    }

    override fun equals(other: Any?): Boolean {
        return other is DefaultFormatter
    }

    override fun hashCode(): Int {
        return 1
    }
}