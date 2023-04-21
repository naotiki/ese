package cui

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import me.naotiki.ese.core.ClientImpl
import me.naotiki.ese.core.EseSystem.IO
import me.naotiki.ese.core.Shell
import me.naotiki.ese.core.Shell.Expression
import me.naotiki.ese.core.appName
import me.naotiki.ese.core.initialize
import org.jline.reader.*
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import kotlin.system.exitProcess


// IMPORTANT : DO NOT EXECUTE FROM GRADLE RUN TASKS
// JLine3 will not be working
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun main(args: Array<String>) {
    println("Starting... $appName")
    val term = TerminalBuilder.builder().system(true).nativeSignals(true).signalHandler {
        if (it == Terminal.Signal.INT) {
            if (!Shell.Expression.cancelJob()) {
                exitProcess(0)
            }
        }
    }.build()
    val reader = LineReaderBuilder.builder()
        .terminal(term)
        .completer(EseCompleter())
        .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true).build()


    val impl = object : ClientImpl {
        override fun getClientName(): String = "Ese CUI"
        override suspend fun prompt(promptText: String, value: String) {
            withContext(Dispatchers.Default) {
                while (!IO.readChannel.isEmpty) {
                    yield()
                }
                val str = reader.readLine(promptText, null, value)
                IO.clientChannel.println(str)
            }
        }

        override fun exit() {
            exitProcess(0)
        }

        override fun clear() {
            // reader.history.save()
        }

    }

    withContext(Dispatchers.Default) {
        listOf(
            launch {
                println("Start Initialize")
                initialize(impl)
            },
            launch {
                println("Start ConsoleReader")
                val w = term.writer()
                IO.readChannel.consumeEach {
                    w.write(it.code)
                    if (it == '\n') w.flush()
                }

                /* while (!io.printChannel.isClosed) {
                     val str = io.printChannel.readln()
                     term.writer().println(str)
                 }*/


                /*{ char ->
                    reader.terminal.output().run {
                        write(char.toString().encodeToByteArray())
                    }
                }*/
            }
        ).joinAll()
    }

}

class EseCompleter : Completer {
    override fun complete(reader: LineReader?, line: ParsedLine?, candidates: MutableList<Candidate>?) {
        val a = line?.line() ?: return
        Expression.suggest(a).also {
            //println(it)
            candidates?.addAll(it.map {
                Candidate(it)
            })
        }
    }

}