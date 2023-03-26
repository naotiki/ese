package me.naotiki.ese.gui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.*
import me.naotiki.ese.core.*
import me.naotiki.ese.core.commands.Expression
import me.naotiki.ese.core.utils.splitArgs
import me.naotiki.ese.gui.component.assistant.CommandPanel
import me.naotiki.ese.gui.component.assistant.EasyFileView
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.SplitPaneState
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.system.exitProcess

val handler = CoroutineExceptionHandler { _, exception ->
    println("CoroutineExceptionHandler got $exception")
    throw exception
}


class TerminalViewModel(prompt: Prompt) : KoinComponent {
    val io by inject<IO>()
    val expression by inject<Expression>()

    val channnel = io.readChannel
    val commandHistory get() = expression.commandHistory

    var textLogs by mutableStateOf(dataDir.absolutePath + "\n")
    private val clientImpl = object : ClientImpl {

        override fun prompt(promptText: String, value: String) {
            prompt.newPrompt(promptText, value)
        }


        override fun exit() {
            exitProcess(0)
        }

        override fun clear() {
            textLogs = ""
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
        } ?: value).also { println(it) }
    }

    fun getSuggestList(value: String) =
        expression.suggest(value).also {
            println(it)
        }


    suspend fun initialize() {
        initialize(getKoin(), clientImpl)
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
fun rememberTerminalViewModel(prompt: Prompt) = remember { TerminalViewModel(prompt) }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Terminal() {
    val prompt by rememberPrompt("", "")
    val viewModel = rememberTerminalViewModel(prompt)
    var historyIndex by remember { mutableStateOf(-1) }
    val coroutine = rememberCoroutineScope()
    val stateVertical = rememberScrollState(0)
    LaunchedEffect(Unit) {
        launch(handler) {
            try {
                viewModel.initialize()
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
            println("End:Init")
        }

        viewModel.channnel.consumeEach {
            viewModel.textLogs += it
            // Scroll to bottom
            stateVertical.scrollTo(stateVertical.maxValue)
            //yield()
        }


        /*viewModel.logFlow.collect {
            viewModel.textLogs += it + ""
            stateVertical.scrollTo(stateVertical.maxValue)
        }*/
    }
    Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray)) {
        Column(
            modifier = Modifier.fillMaxSize().onSizeChanged {
                coroutine.launch {
                    stateVertical.scrollTo(stateVertical.maxValue)
                }
            }.verticalScroll(stateVertical).padding(5.dp),
            verticalArrangement = Arrangement.spacedBy((-5).dp, Alignment.Top)
        ) {
            SelectionContainer {
                Text(
                    viewModel.textLogs,
                    overflow = TextOverflow.Visible,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
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
                    fontFamily = FontFamily.Monospace
                ),
                cursorBrush = SolidColor(Color.White),
                modifier = Modifier.fillMaxWidth().onPreviewKeyEvent {
                    if (!prompt.isEnable) return@onPreviewKeyEvent false
                    return@onPreviewKeyEvent if ((it.key == Key.Enter || it.key == Key.NumPadEnter) && it.type == KeyEventType
                            .KeyDown
                    ) {
                        println(prompt.textFieldValue.text)
                        viewModel.textLogs += prompt.textFieldValue.text + "\n"
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
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd)
                .fillMaxHeight(),
            adapter = rememberScrollbarAdapter(stateVertical), style = LocalScrollbarStyle.current.copy
                (hoverColor = Color.LightGray, unhoverColor = Color.Gray, thickness = 5.dp)
        )
    }
}


@OptIn(ExperimentalSplitPaneApi::class)
@Composable
@Preview
fun App(isAssistExtended: Boolean) {
    val splitState = remember(isAssistExtended) {
        SplitPaneState(
            if (isAssistExtended) 0.35f else 0f,
            isAssistExtended
        )
    }
    MaterialTheme {
        HorizontalSplitPane(splitPaneState = splitState) {
            first(0.dp) {
                val stateVertical = rememberScrollState(0)
                Box(Modifier.fillMaxSize()) {
                    Column(Modifier.fillMaxSize().padding(2.dp).verticalScroll(stateVertical)) {
                        Text("GUIアシスタント", fontSize = 20.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                        var selectedTabIndex by remember { mutableStateOf(0) }

                        TabRow(
                            selectedTabIndex = selectedTabIndex,
                            backgroundColor = MaterialTheme.colors.surface,
                        ) {
                            Tab(
                                selected = selectedTabIndex == 0,
                                onClick = {
                                    selectedTabIndex = 0
                                },
                                text = {
                                    Text(
                                        text = "コマンド",
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedTabIndex == 0) MaterialTheme.colors.primary else
                                            Color.Unspecified
                                    )
                                }
                            )
                            Tab(
                                selected = selectedTabIndex == 1,
                                onClick = {
                                    selectedTabIndex = 1
                                },
                                text = {
                                    Text(
                                        text = "ファイル",
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedTabIndex == 1) MaterialTheme.colors.primary else
                                            Color.Unspecified
                                    )
                                }
                            )
                        }
                        when (selectedTabIndex) {
                            0 -> CommandPanel()
                            1 -> EasyFileView()
                        }


                    }
                    VerticalScrollbar(
                        modifier = Modifier.align(Alignment.CenterEnd)
                            .fillMaxHeight(),
                        adapter = rememberScrollbarAdapter(stateVertical), style = LocalScrollbarStyle.current.copy
                            (hoverColor = Color.LightGray, unhoverColor = Color.Gray, thickness = 5.dp)
                    )
                }


            }

            second(250.dp) {
                Terminal()
            }
        }


    }
}

class ApplicationViewModel : KoinComponent {
    val expression by inject<Expression>()
    fun cancelCommand() {
        expression.cancelJob()
    }
}


@Composable
fun rememberAppViewModel() = remember { ApplicationViewModel() }

@OptIn(ExperimentalComposeUiApi::class)
fun main(vararg args: String) {
    programArg(args.toList())
    //DIよーい！！！！！！
    prepareKoinInjection()

    //以下からCompose(UI部分)
    application {
        val appViewModel = rememberAppViewModel()

        Window(onCloseRequest = ::exitApplication, title = "EseLinux", onPreviewKeyEvent = {
            return@Window if (it.key == Key.C && it.isCtrlPressed) {
                appViewModel.cancelCommand()
                true
            } else false
        }) {
            var isAssistExtended by remember { mutableStateOf(true) }
            MenuBar {
                Menu("表示") {
                    Item(
                        "GUIアシスタントを" + if (isAssistExtended) "折りたたむ" else "表示する", onClick = {
                            isAssistExtended =
                                !isAssistExtended
                        },
                        shortcut = KeyShortcut(
                            Key.T,
                            ctrl = true
                        )
                    )
                }
            }
            App(isAssistExtended)
        }
    }
}
