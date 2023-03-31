import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import me.naotiki.ese.core.ClientImpl
import me.naotiki.ese.core.IO
import me.naotiki.ese.core.commands.Expression
import me.naotiki.ese.core.utils.splitArgs
import component.TextLog
import component.TextLogState
import component.rememberTextLogState
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

expect fun <T> runBlocking(context: CoroutineContext = EmptyCoroutineContext,  block: suspend CoroutineScope.()
->T)
expect fun exitProcess(code:Int):Nothing
class TerminalViewModel(prompt: Prompt, logState: TextLogState) : KoinComponent {
    val io by inject<IO>()
    val expression by inject<Expression>()

    val channnel = io.readChannel
    val commandHistory get() = expression.commandHistory

    private val clientImpl = object : ClientImpl {
        override fun getClientName(): String = clientName
        override suspend fun prompt(promptText: String, value: String) {
            prompt.newPrompt(promptText, value)
        }


        override fun exit() {
            exitProcess(0)
        }

        override fun clear() {
            //textLogs = ""
            logState.clear()

        }
    }

    var suggestion by mutableStateOf<String?>(null)
    var suggestList by mutableStateOf(emptyList<String>())
    var count by mutableStateOf(0)
    var previousArgsCount by mutableStateOf(0)
    fun nextSuggest(value: String): String {
        val target = value.splitArgs()
        val arg = target.last()
        if (suggestion != arg || previousArgsCount != target.size) {
            suggestion = arg
            suggestList = getSuggestList(value)
            previousArgsCount = target.size
        }
        return (suggestList.ifEmpty { null }?.let {
            val a = (it.getOrNull(count) ?: run {
                count = 0
                suggestList.getOrNull(count)
            }) ?: return@let null
            count++
            (target.dropLast(1) + a).joinToString(" ")
        } ?: value)
    }

    fun getSuggestList(value: String) =
        expression.suggest(value)


    suspend fun initialize() {
        me.naotiki.ese.core.initialize(getKoin(), clientImpl)
    }

    fun CoroutineScope.outln(value: String) {
        suggestion = null
        suggestList = emptyList()

        runBlocking {
            this@outln.launch { io.clientChannel.println(value) }
        }

    }
}

@Composable
fun rememberTerminalViewModel(prompt: Prompt, logState: TextLogState) =
    remember { TerminalViewModel(prompt, logState) }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Terminal() {
    val prompt by rememberPrompt("", "")
    val textLogState = rememberTextLogState(4000)
    val viewModel = rememberTerminalViewModel(prompt, textLogState)
    var historyIndex by remember { mutableStateOf(-1) }
    val coroutine = rememberCoroutineScope()
    //  val stateVertical = rememberScrollState(0)
    LaunchedEffect(Unit) {
        launch {
            try {
                viewModel.initialize()
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }
        withContext(Dispatchers.Default) {
            viewModel.channnel.consumeEach {
                textLogState.addChar(it)
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(5.dp),
        ) {
            TextLog(textLogState, Modifier.weight(0.1f, false), fontSize = 20.sp) {
                var lastInput by remember { mutableStateOf("") }
                BasicTextField(
                    prompt.textFieldValue,
                    onValueChange = {
                        prompt.updateTextFieldValue(it) { value, _ ->
                            lastInput = value
                        }
                    },
                    textStyle =
                    TextStyle(
                        color = Color.White,
                        fontSize =
                        20.sp,
                        fontFamily =DefaultFont
                    ),
                    cursorBrush = SolidColor(Color.White),
                    modifier = Modifier.fillMaxWidth().weight(1f).onPreviewKeyEvent {
                        if (!prompt.isEnable) return@onPreviewKeyEvent false
                        return@onPreviewKeyEvent if ((it.key == Key.Enter || it.key == Key.NumPadEnter) && it.type == KeyEventType
                                .KeyDown
                        ) {

                            textLogState.addString(prompt.textFieldValue.text)
                            with(viewModel) { coroutine.outln(prompt.getValue()) }
                            prompt.reset()
                            lastInput = ""
                            historyIndex = -1
                            true
                        } else if ((it.key == Key.DirectionUp || it.key == Key.DirectionDown) && it.type == KeyEventType.KeyDown) {
                            historyIndex = when (it.key) {
                                Key.DirectionUp -> minOf(historyIndex + 1, viewModel.commandHistory.lastIndex)
                                Key.DirectionDown -> maxOf(historyIndex - 1, -1)
                                else -> -1
                            }
                            viewModel.commandHistory.getOrNull(historyIndex).let { s ->
                                prompt.updateValue((s ?: "").also { lastInput = it })
                            }
                            true
                        } else if (it.key == Key.Tab && it.type == KeyEventType.KeyDown) {
                            prompt.updateValue(viewModel.nextSuggest(lastInput))
                            true
                        } else false
                    },
                )
            }
        }
    }
}