package com.gaumala.qalog.service

import com.gaumala.qalog.printer.LogFormatter
import com.gaumala.qalog.printer.LogPrinter

data class LogConfig(val formatter: LogFormatter,
                     val bufferTimeLimit: Long,
                     val bufferMaxCapacity: Int,
                     val printer: LogPrinter
)