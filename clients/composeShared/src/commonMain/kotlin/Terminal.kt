import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import component.TextLog
import component.TextLogState
import component.rememberTextLogState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import me.naotiki.ese.core.*
import me.naotiki.ese.core.EseSystem.IO
import me.naotiki.ese.core.Shell.Expression
import me.naotiki.ese.core.utils.splitArgs
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

expect fun <T> tryRunBlocking(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> T
)

expect fun exitApp(code: Int): Unit
class TerminalViewModel(val prompt: Prompt,val  logState: TextLogState,) {

    val channnel get() = IO.readChannel
    val commandHistory get() = Expression.commandHistory

    private val clientImpl = object : ClientImpl {
        override fun getClientName(): String = clientName
        override suspend fun prompt(promptText: String, value: String) {
            prompt.newPrompt(promptText, value)
        }
        override fun exit() {
            exitApp(0)
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
        Expression.suggest(value)


    suspend fun initialize() {
        EseSystem.ClientImpl=clientImpl
        if (eseInitialized) return
        me.naotiki.ese.core.initialize(clientImpl, platformInitMessage)
    }

    fun CoroutineScope.outln(value: String) {
        suggestion = null
        suggestList = emptyList()

        tryRunBlocking {
            this@outln.launch { IO.clientChannel.println(value) }
        }

    }
}

@Composable
fun rememberTerminalViewModel(prompt: Prompt, logState: TextLogState) =
    remember { TerminalViewModel(prompt, logState) }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Terminal() {
    val focusRequester = remember { FocusRequester() }
    val prompt by rememberPrompt("", "")
    val textLogState = rememberTextLogState(4000)
    val viewModel = rememberTerminalViewModel(prompt, textLogState)
    var historyIndex by remember { mutableStateOf(-1) }
    val coroutine = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        launch {
            try {
                viewModel.initialize()
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }
        viewModel.channnel.consumeEach {
            textLogState.addChar(it)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray)) {

        Column(
            modifier = Modifier.fillMaxSize().padding(5.dp),
        ) {
            val lazyListState = rememberLazyListState()
            TextLog(textLogState, lazyListState, modifier = Modifier.weight(0.1f, false), fontSize = 20.sp) {

                var lastInput by remember { mutableStateOf("") }
                val onSend by rememberUpdatedState {
                    textLogState.addString(prompt.textFieldValue.text)
                    with(viewModel) { coroutine.outln(prompt.getValue()) }
                    prompt.reset()
                    lastInput = ""
                    historyIndex = -1
                }

                BasicTextField(
                    prompt.textFieldValue,

                    onValueChange = {
                        prompt.updateTextFieldValue(it) { value, _ ->
                            lastInput = value
                        }
                    },
                    keyboardActions = KeyboardActions {
                        onSend()
                    },
                    textStyle =
                    TextStyle(
                        color = Color.White,
                        fontSize =
                        20.sp,
                        fontFamily = LocalDefaultFont.current
                    ),
                    cursorBrush = SolidColor(Color.White),
                    modifier = Modifier.fillMaxWidth().weight(1f).onPreviewKeyEvent {
                        if (!prompt.isEnable) return@onPreviewKeyEvent false
                        return@onPreviewKeyEvent if ((it.key == Key.Enter || it.key == Key.NumPadEnter) && it.type == KeyEventType
                                .KeyDown
                        ) {
                            onSend()
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
                    }.focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Send
                    ),
                )
                LaunchedEffect(Unit) {
                    //Focusをロック
                    focusRequester.requestFocus()
                    focusRequester.captureFocus()
                    //VK登録
                    VirtualKeyboardManager.addListener {
                        if (it.isCtrlPressed && it.key == Key.C) {
                            Shell.Expression.cancelJob()
                            true
                        } else
                            if ((it.key == Key.DirectionUp || it.key == Key.DirectionDown) && it.type == KeyEventType.KeyDown) {
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
                    }
                }


            }
            VirtualKeyboard()
        }

    }
}

object VirtualKeyboardManager {
    private val listeners = mutableListOf<(KeyEvent) -> Boolean>()
    fun addListener(onPressed: (KeyEvent) -> Boolean) {
        listeners.add(onPressed)
    }

    fun press(keyEvent: KeyEvent) {
        listeners.forEach {
            if (it(keyEvent)) {
                return
            }
        }
    }
}

@Composable
expect fun VirtualKeyboard(modifier: Modifier = Modifier)