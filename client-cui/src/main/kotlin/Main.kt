import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.naotiki.ese.core.ClientImpl
import me.naotiki.ese.core.IO
import me.naotiki.ese.core.commands.Expression
import me.naotiki.ese.core.initialize
import me.naotiki.ese.core.prepareKoinInjection
import me.naotiki.ese.core.utils.log
import me.naotiki.ese.core.utils.loop
import org.jline.reader.*
import org.jline.terminal.TerminalBuilder
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.logger.Level
import kotlin.system.exitProcess


suspend fun main(args: Array<String>) {
    val koin = prepareKoinInjection(Level.NONE).koin
    val term = TerminalBuilder.builder().build()
    val readerBuilder =
        LineReaderBuilder.builder()
            .terminal(term)
            .completer(EseCompleter())
    val reader = readerBuilder.build()
    reader.setVariable(LineReader.SECONDARY_PROMPT_PATTERN, "")
    reader.option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)

    var prompt: String? = null
    val impl = object : ClientImpl {
        override fun prompt(promptText: String, value: String) {
            prompt = promptText
        }

        override fun exit() {
            exitProcess(0)
        }

        override fun clear() {
            reader.history.save()
        }

    }
    val io = koin.get<IO>()
    withContext(Dispatchers.IO) {
        launch {
            initialize(koin, impl)
        }.start()

        launch {
            io.readChannel.consumeEach { char ->
                reader.terminal.output().run {
                    write(char.toString().encodeToByteArray())
                }
            }
        }.start()

        println("Init:")
        launch {
            loop {
                if (prompt != null) {
                    val str = reader.readLine(prompt)
                    io.clientChannel.println(str)
                    prompt = null
                }
            }
        }
    }

}

class EseCompleter : Completer, KoinComponent {
    val expression by inject<Expression>()
    override fun complete(reader: LineReader?, line: ParsedLine?, candidates: MutableList<Candidate>?) {
        val a = line?.line() ?: return
        a.log("[AA]")
        expression.suggest(a).also {
            //println(it)
            candidates?.addAll(it.map {
                println(it)
                Candidate(it)
            })
        }
    }

}