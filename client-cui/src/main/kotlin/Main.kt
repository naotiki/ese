import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import me.naotiki.ese.core.ClientImpl
import me.naotiki.ese.core.IO
import me.naotiki.ese.core.commands.Expression
import me.naotiki.ese.core.initialize
import me.naotiki.ese.core.prepareKoinInjection
import me.naotiki.ese.core.utils.log
import me.naotiki.ese.core.utils.loop
import org.jline.reader.*
import org.jline.reader.impl.DefaultParser
import org.jline.reader.impl.LineReaderImpl.BRACKETED_PASTE_ON
import org.jline.terminal.Attributes
import org.jline.terminal.TerminalBuilder
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.logger.Level
import java.util.*
import java.util.logging.Logger
import kotlin.system.exitProcess

fun mains() {
    object : System.Logger {
        override fun getName(): String {
            TODO("Not yet implemented")
        }

        override fun isLoggable(level: System.Logger.Level?): Boolean {
            TODO("Not yet implemented")
        }

        override fun log(level: System.Logger.Level?, bundle: ResourceBundle?, msg: String?, thrown: Throwable?) {
            TODO("Not yet implemented")
        }

        override fun log(level: System.Logger.Level?, bundle: ResourceBundle?, format: String?, vararg params: Any?) {
            TODO("Not yet implemented")
        }

    }
    val terminal = TerminalBuilder.builder().build()
    terminal.enterRawMode()

    var ch = 0

    while (terminal.input().read().also { ch = it } != 0x09) {
        // TAB(0x09)で抜ける
        val c = ch.toChar()
        println(String.format("%d, %c", ch, ch))
    }
}

// IMPORTANT : DO NOT EXECUTE FROM GRADLE RUN TASKS
// JLine3 will not be working
suspend fun main(args: Array<String>) {
    val koin = prepareKoinInjection(Level.NONE).koin
    val term = TerminalBuilder.builder().build()

    val reader = LineReaderBuilder.builder()
        .terminal(term)
        .completer(EseCompleter())
        .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true).build()


    val io = koin.get<IO>()
    val impl = object : ClientImpl {
        override suspend fun prompt(promptText: String, value: String) {
            withContext(Dispatchers.Default) {
                val str = reader.readLine(promptText)
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
                val w=term.writer()
                io.readChannel.consumeEach {
                    w.write(it.code)
                    if (it=='\n')w.flush()
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