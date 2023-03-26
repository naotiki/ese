package me.naotiki.ese.core.utils.io

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield
import me.naotiki.ese.core.utils.log




class PrintChannel(
    val lineSeparator: String="\n"
):ReadChannel {
    private val channel = Channel<Char>(Channel.BUFFERED)
    val receiveChannel: ReceiveChannel<Char> get() = channel
    @OptIn(ExperimentalCoroutinesApi::class)
    val isClosed get() = channel.isClosedForReceive
    private val mutex = Mutex()
    suspend fun print(a: Any?) {
        mutex.withLock {
            a.toString().forEach {
                channel.send(it)
            }
        }
    }

    suspend inline fun println(a: Any?) {
        print(a.toString() + '\n')
    }

    suspend fun println() {
        newLine()
    }

    fun tryPrint(a: Any?) {
        synchronized(this){
            a.toString().forEach {
                channel.trySendBlocking(it)
            }
        }
    }

    fun tryPrintln(a: Any?) {
        tryPrint(a.toString() + '\n')
    }

    fun tryPrintln() = newLineBlocking()

    private fun newLineBlocking() = lineSeparator.toCharArray().forEach { channel.trySendBlocking(it)}
    private suspend inline fun newLine() =  lineSeparator.toCharArray().forEach{ channel.send(it) }

    override suspend fun readln(): String {
        var result=""

        do {
            val c=receiveChannel.receive()
            result+=c
            yield()
        }while (c != '\n')

        return result.dropLast(1)
    }
}

interface ReadChannel{
    suspend fun readln():String

}

private suspend fun main() {
    val pc = PrintChannel()
    pc.receiveChannel.consumeEach { }
}