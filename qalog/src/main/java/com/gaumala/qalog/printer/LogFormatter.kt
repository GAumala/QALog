package com.gaumala.qalog.printer

interface LogFormatter {
    fun format(timestamp: Long, text: String): String
}