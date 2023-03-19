package me.naotiki.ese.core.utils.io

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PrintChannel {
    //capacityは
    private val channel = Channel<Char>(Channel.RENDEZVOUS)

    val receiveChannel: ReceiveChannel<Char> get() = channel
    val m = Mutex()
    suspend fun print(a: Any?) {
        m.withLock {
            a.toString().forEach {
                channel.send(it)
            }
        }
    }

    suspend fun println(a: Any?) {
        print(a)
        newLine()
    }

    suspend fun println() {
        newLine()
    }

    fun tryPrint(a: Any?) {
        a.toString().forEach {
            channel.trySendBlocking(it)
        }
    }

    fun tryPrintln(a: Any?) {
        tryPrint(a)
        newLineBlocking()
    }

    private fun newLineBlocking() = channel.trySendBlocking('\n')
    private suspend inline fun newLine() = channel.send('\n')
}

private suspend fun main() {
    val pc = PrintChannel()
    pc.receiveChannel.consumeEach { }
}