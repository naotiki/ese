
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import me.naotiki.ese.core.ClientImpl
import me.naotiki.ese.core.IO
import me.naotiki.ese.core.commands.Expression
import me.naotiki.ese.core.initialize
import me.naotiki.ese.core.prepareKoinInjection
import org.jline.reader.*
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.logger.Level
import kotlin.system.exitProcess


// IMPORTANT : DO NOT EXECUTE FROM GRADLE RUN TASKS
// JLine3 will not be working
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun main(args: Array<String>) {
    val koin = prepareKoinInjection(Level.NONE).koin
    val expression = koin.get<Expression>()
    val term = TerminalBuilder.builder().system(true).nativeSignals(true).signalHandler {
        if (it == Terminal.Signal.INT) {
            expression.cancelJob()
        }
    }.build()
    val reader = LineReaderBuilder.builder()
        .terminal(term)
        .completer(EseCompleter())
        .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true).build()


    val io = koin.get<IO>()
    val impl = object : ClientImpl {
        override fun getClientName(): String = "EseLinux Client-CUI"
        override suspend fun prompt(promptText: String, value: String) {
            withContext(Dispatchers.Default) {
                while (!io.readChannel.isEmpty){
                    yield()
                }
                val str = reader.readLine(promptText, null, value)
                io.clientChannel.println(str)
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
                initialize(koin, impl)
            },
            launch {
                val w = term.writer()
                io.readChannel.consumeEach {
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

class EseCompleter : Completer, KoinComponent {
    val expression by inject<Expression>()
    override fun complete(reader: LineReader?, line: ParsedLine?, candidates: MutableList<Candidate>?) {
        val a = line?.line() ?: return
        expression.suggest(a).also {
            //println(it)
            candidates?.addAll(it.map {
                Candidate(it)
            })
        }
    }

}