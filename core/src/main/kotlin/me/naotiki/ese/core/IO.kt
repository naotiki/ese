package me.naotiki.ese.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.naotiki.ese.core.utils.io.PrintChannel
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintStream

class IO {

    val printChannel = PrintChannel()
    val readChannel = printChannel.receiveChannel


    @Deprecated("migrate channel")
    private val inputStream = PipedInputStream()

    @Deprecated("migrate channel")
    val reader = inputStream.reader()

    @Deprecated("migrate channel", replaceWith = ReplaceWith("this.printChannel"))
    val outputStream = PrintStream(PipedOutputStream(inputStream), true)

    private val consoleInput = PipedInputStream()
    val consoleReader = consoleInput.bufferedReader()
    val consoleWriter = PrintStream(PipedOutputStream(consoleInput), true)
    suspend fun newPrompt(clientImpl: ClientImpl, promptText: String, value: String = ""): String =
        withContext(Dispatchers.IO) {
            clientImpl.prompt(promptText, value)
            return@withContext consoleReader.readLine()
        }
}