package me.naotiki.ese.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.naotiki.ese.core.utils.io.PrintChannel
import me.naotiki.ese.core.utils.io.ReadChannel
class IO {

    val printChannel = PrintChannel(buffer = 64)
    val readChannel = printChannel.receiveChannel


   /* @Deprecated("migrate channel")
    private val inputStream = PipedInputStream()

    @Deprecated("migrate channel")
    val reader = inputStream.reader()

    @Deprecated("migrate channel", replaceWith = ReplaceWith("this.printChannel"))
    val outputStream = PrintStream(PipedOutputStream(inputStream), true)*/


    val clientChannel = PrintChannel(buffer = 64)
    internal val clientReadChannel = clientChannel.receiveChannel
   /* @Deprecated("migrate channel")
    private val consoleInput = PipedInputStream()
    @Deprecated("migrate channel")
    val consoleReader = consoleInput.bufferedReader()
    @Deprecated("migrate channel")
    val consoleWriter = PrintStream(PipedOutputStream(consoleInput), true)*/
    //For Client-GUI
    suspend fun newPrompt(clientImpl: ClientImpl, promptText: String, value: String = ""):String{
        clientImpl.prompt(promptText, value)

        return (clientChannel as ReadChannel).readln()
    }
    suspend fun newPromptAsync(clientImpl: ClientImpl, promptText: String, value: String = ""):String= withContext(Dispatchers.Default){
        launch{ clientImpl.prompt(promptText, value) }

        (clientChannel as ReadChannel).readln()
    }
 /*   @Deprecated("だめー", ReplaceWith("this.newPrompt(clientImpl,promptText,value)"))
    suspend fun newPromptDep(clientImpl: ClientImpl, promptText: String, value: String = ""): String =
        withContext(Dispatchers.IO) {
            clientImpl.prompt(promptText, value)

            return@withContext consoleReader.readLine()
        }*/
}