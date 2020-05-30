package com.gaumala.qalog

import com.gaumala.qalog.service.LogFilePrinter
import com.gaumala.qalog.printer.LogPrinter
import com.gaumala.qalog.printer.PrinterMonitor
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.*
import org.amshove.kluent.`should be empty`
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not be empty`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File

@ExperimentalCoroutinesApi
class LoggerFileTest {

    @Rule @JvmField
    val rule = TestCoroutineRule()

    private val testScope = rule.testScope
    private lateinit var logFile: File

    @Before
    fun setup() {
        logFile = File.createTempFile("TestLog_", ".txt")
        logFile.deleteOnExit()
    }

    private fun createTestLogger(): Triple<Logger, LogPrinter, PrinterMonitor> {
        val logger = Logger()
        val printer = LogFilePrinter(logFile)
        val monitor = mockk<PrinterMonitor>(relaxed = true)
        logger.runQueue(testScope, printer, monitor)
        return Triple(logger, printer, monitor)
    }

    @Test
    fun `should copy printed messages if copy is requested`() {
        val (logger, printer, _) = createTestLogger()

        logger.log(0,"hello world!")

        // wait for timeout
        testScope.advanceTimeBy(1000)
        printer.getPrintedText().`should not be empty`()

        val copyOutput = ByteArrayOutputStream()
        logger.copy(copyOutput)

        val expectedText = "[0] hello world!\n"
        val copiedText = copyOutput.getStreamedText()
        copiedText `should be equal to` expectedText

        testScope.advanceUntilIdle()
    }

    @Test
    fun `should copy everything received prior to copy msg`() {
        val (logger, printer, _) = createTestLogger()

        logger.log(0,"hello world!")

        // wait for timeout
        testScope.advanceTimeBy(1000)
        printer.getPrintedText().`should not be empty`()

        logger.log(1, "extra 1")
        logger.log(2, "extra 2")
        val copyOutput = ByteArrayOutputStream()
        logger.copy(copyOutput)


        val expectedText = "[0] hello world!\n" +
                "[1] extra 1\n" +
                "[2] extra 2\n"
        val printedText = printer.getPrintedText()
        printedText `should be equal to` expectedText

        val copiedText = copyOutput.getStreamedText()
        copiedText `should be equal to` expectedText

        testScope.advanceUntilIdle()
    }

    @Test
    fun `should clear printed messages reset is requested`() {
        val (logger, printer, _) = createTestLogger()

        logger.log(0,"hello world!")

        // wait for timeout
        testScope.advanceTimeBy(1000)
        printer.getPrintedText().`should not be empty`()

        logger.reset()

        val printedText = printer.getPrintedText()
        printedText.`should be empty`()

        testScope.advanceUntilIdle()
    }

    @Test
    fun `should delete everything received prior to the reset request`() {
        val (logger, printer, _) = createTestLogger()

        logger.log(0,"hello world!")

        // wait for timeout
        testScope.advanceTimeBy(1000)
        printer.getPrintedText().`should not be empty`()

        logger.log(1,"extra msg")
        logger.reset()

        val printedText = printer.getPrintedText()
        printedText.`should be empty`()

        testScope.advanceUntilIdle()
    }

    @Test
    fun `should update monitor after every print`() {
        val (logger, _, monitor) = createTestLogger()

        // should not invoke monitor before printing anything
        verify(exactly = 0) { monitor.showCount(any()) }

        // wait until buffer timeout triggers print
        logger.log(1, "hello world!")
        testScope.advanceTimeBy(1000)

        verify(exactly = 1) { monitor.showCount(1) }

        // wait until buffer timeout triggers another print
        logger.log(2, "hello again world!")
        testScope.advanceTimeBy(1000)

        verify(exactly = 1) { monitor.showCount(2) }

        // wait until capacity limit triggers another print
        logger.log(3, "log #3")
        logger.log(4, "log #4")
        logger.log(5, "log #5")
        logger.log(6, "log #6")
        logger.log(7, "log #7")
        logger.log(8, "log #8")
        logger.log(9, "log #9")
        logger.log(10, "log #10")
        logger.log(11, "log #11")
        logger.log(12, "log #12")

        verify(exactly = 1) { monitor.showCount(12) }
    }

    @Test
    fun `should update monitor after clearing the log`() {
        val (logger, _, monitor) = createTestLogger()

        // wait until buffer timeout triggers print
        logger.log(1, "hello world!")
        testScope.advanceTimeBy(1000)

        verify(exactly = 1) { monitor.showCount(1) }

        logger.reset()

        verify(exactly = 1) { monitor.showCount(0) }
    }

    @Test
    fun `should shut down monitor when scope gets cancelled`() {
        val (_, _, monitor) = createTestLogger()

        verify(exactly = 0) { monitor.shutdown() }

        testScope.cancel(null)

        verify(exactly = 1) { monitor.shutdown() }
    }

    @Test
    fun `should update monitor when queue starts if printer already had text`() {
        val logger = Logger()
        val printer = LogFilePrinter(logFile)

        printer.println("Here is some pre existing line")

        val monitor = mockk<PrinterMonitor>(relaxed = true)
        logger.runQueue(testScope, printer, monitor)


        verify(exactly = 1) { monitor.showCount(1) }
    }

    @Test
    fun `logs should be persisted across different printer instances`() {
        val logger = Logger()
        val firstPrinter = LogFilePrinter(logFile)

        firstPrinter.println("Here is some pre existing line")

        val monitor = mockk<PrinterMonitor>(relaxed = true)
        logger.runQueue(testScope, firstPrinter, monitor)
        // cancel the scope to simulate process death
        testScope.cancel()

        val secondPrinter = LogFilePrinter(logFile)
        secondPrinter.println("Here is a second line")

        val expectedText = "Here is some pre existing line\nHere is a second line\n"
        val printedText = secondPrinter.getPrintedText()
        printedText `should be equal to` expectedText
    }
}

