package me.naotiki.ese.core.utils.io

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield

class PrintChannel(
    val lineSeparator: String=System.lineSeparator()?:"\n"
):ReadChannel {
    private val channel = Channel<Char>(Channel.RENDEZVOUS)
    val receiveChannel: ReceiveChannel<Char> get() = channel

    private val mutex = Mutex()
    suspend fun print(a: Any?) {

        mutex.withLock {
            a.toString().forEach {
                channel.send(it)
            }
        }
    }

    suspend inline fun println(a: Any?) {
        print(a.toString() + lineSeparator)
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
        tryPrint(a.toString() + lineSeparator)
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