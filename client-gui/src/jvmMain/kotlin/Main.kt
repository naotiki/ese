import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import java.awt.MenuBar
import kotlin.system.exitProcess

val logStream = reader.lineSequence().asFlow().flowOn(Dispatchers.IO)
val handler = CoroutineExceptionHandler { _, exception ->
    println("CoroutineExceptionHandler got $exception")
    throw exception
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalSplitPaneApi::class)
@Composable
@Preview
fun App() {
    var historyIndex by remember { mutableStateOf(-1) }
    val coroutine = rememberCoroutineScope()
    val prompt by rememberPrompt("", "")
    var textLogs by remember { mutableStateOf("") }
    val stateVertical = rememberScrollState(0)
    LaunchedEffect(Unit) {
        launch(handler) {
            try {
                initialize(object : ConsoleInterface {
                    override fun prompt(promptText: String, value: String) {
                        prompt.newPrompt(promptText, value)
                    }

                    override fun exit() {
                        exitProcess(0)
                    }

                    override fun clear() {
                        textLogs = ""
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
                throw e

            }
            println("End:Init")
        }
        logStream.collect {
            textLogs += it + "\n"
            stateVertical.scrollTo(stateVertical.maxValue)
        }
    }
    val splitState= rememberSplitPaneState(0.2f)

    MaterialTheme {
        HorizontalSplitPane(splitPaneState = splitState){
            first(100.dp) {
                Box(Modifier.fillMaxSize().onSizeChanged {

                    println("->"+it.width)
                }) {
                    Text("GUIアシスト", fontSize = 20.sp)

                }

            }

            second(250.dp) {
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
                                textLogs,
                                overflow = TextOverflow.Visible,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        BasicTextField(
                            prompt.textFieldValue,
                            onValueChange = {
                                prompt.updateTextFieldValue(it) { value, _ ->
                                    println("Debug:Updated $value")
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

                                return@onPreviewKeyEvent if (it.key == Key.Enter && it.type == KeyEventType.KeyDown /*&& prompt
                        .isEnable*/) {
                                    textLogs += prompt.textFieldValue.text + "\n"
                                    consoleWriter.println(prompt.getValue())
                                    prompt.reset()
                                    historyIndex = -1
                                    true
                                } else if ((it.key == Key.DirectionUp || it.key == Key.DirectionDown) && it.type == KeyEventType.KeyDown) {
                                    historyIndex = when (it.key) {
                                        Key.DirectionUp -> minOf(historyIndex + 1, commandHistory.lastIndex)
                                        Key.DirectionDown -> maxOf(historyIndex - 1, -1)
                                        else -> -1
                                    }
                                    commandHistory.getOrNull(historyIndex).let { s ->
                                        prompt.updateValue(s ?: "")
                                    }
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
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Console", onPreviewKeyEvent = {
        return@Window if (it.key == Key.C && it.isCtrlPressed) {
            cancelCommand()
            true
        } else false
    }) {
        MenuBar {
            Menu("表示",) {
                Item("GUIアシストを折りたたむ", onClick = {  }, shortcut = KeyShortcut(Key.T, ctrl = true))
            }
        }
        App()
    }
}
