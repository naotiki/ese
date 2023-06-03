package me.naotiki.ese.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.naotiki.ese.core.utils.io.PrintChannel
import me.naotiki.ese.core.utils.io.ReadChannel

/**
 * IO System of Ese
 */
class IO {
    /**
     * Send texts to client app from Ese core or Ese plugins
     */
    val printChannel = PrintChannel(buffer = 64)
    val readChannel = printChannel.receiveChannel
    /**
     * Send texts to Ese core or Ese plugins from client app
     */
    val clientChannel = PrintChannel(buffer = 64)
    internal val clientReadChannel = clientChannel.receiveChannel
    //For Client-GUI
    suspend fun newPrompt(clientImpl: ClientImpl, promptText: String, value: String = ""):String{
        clientImpl.prompt(promptText, value)

        return (clientChannel as ReadChannel).readln()
    }
    suspend fun newPromptAsync(clientImpl: ClientImpl, promptText: String, value: String = ""):String= withContext(Dispatchers.Default){
        launch{ clientImpl.prompt(promptText, value) }

        (clientChannel as ReadChannel).readln()
    }
}

