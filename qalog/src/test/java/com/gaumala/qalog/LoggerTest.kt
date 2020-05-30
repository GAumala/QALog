package com.gaumala.qalog

import com.gaumala.qalog.printer.LogPrinter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.amshove.kluent.`should be empty`
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not be empty`
import org.junit.Rule
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.PrintStream


@ExperimentalCoroutinesApi
class LoggerTest {

    @Rule
    @JvmField
    val rule = TestCoroutineRule()

    private val testScope = rule.testScope

    private fun createTestPrinter(): LogPrinter {
        return object: LogPrinter {
            private var builder = StringBuilder()

            override fun println(newLine: String) {
                builder.append(newLine).append('\n')
            }

            override fun copy(outputStream: OutputStream) {
                val printStream = PrintStream(outputStream)
                if (builder.isNotEmpty())
                    printStream.print(builder.toString())
            }

            override fun reset() {
                builder = StringBuilder()
            }

            override fun countPrintedLines(): Int {
                // This method should only be called when
                // the queue starts up. We can assume that
                // they all begin without pre-existing
                // lines for these tests
                return 0
            }

            override fun shutdown() {
                // No resources to close
            }

        }
    }

    @Test
    fun `should be available if printer is not yet set`() {
        val logger = createTestLogger()

        logger.log(1, "hello world!")
    }

    @Test
    fun `should not print until setting a printer`() {
        val logger = createTestLogger()
        val printer = createTestPrinter()

        logger.log(1, "hello world!")
        testScope.advanceTimeBy(1000)

        logger.runQueue(testScope, printer)
        testScope.advanceTimeBy(1000)

        val printedText = printer.getPrintedText()
        printedText `should be equal to` "[1] hello world!\n"
    }

    @Test
    fun `should print when buffer times out`() {
        val logger = createTestLogger()
        val printer = createTestPrinter()
        logger.runQueue(testScope, printer)

        logger.log(1, "hello world!")

        // short delay
        testScope.advanceTimeBy(500)
        printer.getPrintedText().`should be empty`()

        // add extra msg to buffer
        logger.log(2,"extra msg")

        // wait for timeout
        testScope.advanceTimeBy(1000)

        val printedText = printer.getPrintedText()
        val expectedText = "[1] hello world!\n[2] extra msg\n"
        printedText `should be equal to` expectedText
    }


    @Test
    fun `should print if buffer max capacity is exceeded`() {
        val logger = createTestLogger()
        val printer = createTestPrinter()
        logger.runQueue(testScope, printer)

        logger.log(0,"hello world!")

        // short delay
        testScope.advanceTimeBy(500)
        printer.getPrintedText().`should be empty`()

        logger.log(1, "extra 1")
        logger.log(2, "extra 2")
        logger.log(3, "extra 3")
        logger.log(4, "extra 4")
        logger.log(5, "extra 5")
        logger.log(6, "extra 6")
        logger.log(7, "extra 7")
        logger.log(8, "extra 8")
        logger.log(9, "extra 9")

        val expectedText = "[0] hello world!\n" +
                "[1] extra 1\n" +
                "[2] extra 2\n" +
                "[3] extra 3\n" +
                "[4] extra 4\n" +
                "[5] extra 5\n" +
                "[6] extra 6\n" +
                "[7] extra 7\n" +
                "[8] extra 8\n" +
                "[9] extra 9\n"
        val printedText = printer.getPrintedText()
        printedText `should be equal to` expectedText

        testScope.advanceUntilIdle()
    }

    @Test
    fun `should copy printed messages if copy is requested`() {
        val logger = createTestLogger()
        val printer = createTestPrinter()
        logger.runQueue(testScope, printer)

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
        val logger = createTestLogger()
        val printer = createTestPrinter()
        logger.runQueue(testScope, printer)

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
        val logger = createTestLogger()
        val printer = createTestPrinter()
        logger.runQueue(testScope, printer)

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
        val logger = createTestLogger()
        val printer = createTestPrinter()
        logger.runQueue(testScope, printer)

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
}