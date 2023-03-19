package me.naotiki.ese.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintStream

class IO {

    /*private*/ val inputStream = PipedInputStream()

    val reader = inputStream.reader()
    val outputStream = PrintStream(PipedOutputStream(inputStream), true)

    private val consoleInput = PipedInputStream()
    val consoleReader = consoleInput.bufferedReader()
    val consoleWriter = PrintStream(PipedOutputStream(consoleInput), true)
    suspend fun newPrompt(clientImpl: ClientImpl, promptText: String, value: String = ""): String =
        withContext(
            Dispatchers.IO
        ) {
            clientImpl.prompt(promptText, value)
            return@withContext consoleReader.readLine()
        }
}