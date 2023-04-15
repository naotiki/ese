package me.naotiki.ese.core.utils.io

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield

expect inline fun <R> trySynchronized(lock:Any, block:()->R):R
class PrintChannel(
    val lineSeparator: String="\n",
    val buffer:Int=Channel.BUFFERED
):ReadChannel {
    private val channel = Channel<Char>(buffer)
    val receiveChannel: ReceiveChannel<Char> get() = channel
    @OptIn(ExperimentalCoroutinesApi::class)
    val isClosed get() = channel.isClosedForReceive
    private val mutex = Mutex()
    suspend fun print(a: Any?) = mutex.withLock {
            a.toString().forEach {
                channel.send(it)
            }
        }


    suspend inline fun println(a: Any?) {
        print(a.toString() + '\n')
    }

    suspend fun println() {
        newLine()
    }
    @Deprecated("JS Not Working",level = DeprecationLevel.WARNING)
    fun tryPrint(a: Any?) {
        trySynchronized(this){
            a.toString().forEach {
                while(!channel.trySend(it).isSuccess);

            }
        }
    }
    @Deprecated("JS Not Working",level = DeprecationLevel.WARNING)
    fun tryPrintln(a: Any?) {
        tryPrint(a.toString() + '\n')
    }

    fun tryPrintln() = tryNewLine()

    private fun tryNewLine() = lineSeparator.toCharArray().forEach { channel.trySend(it)}
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