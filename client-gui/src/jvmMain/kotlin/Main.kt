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
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/*val logStream: Flow<String> = flow {
    while (true) {
        println("In Loop")
        val result = withContext(Dispatchers.IO) { reader.readText() }
        emit(result)
        println(result)
    }
}*/
val logStream= reader.lineSequence().asFlow().flowOn(Dispatchers.IO)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {
    val coroutine = rememberCoroutineScope()
    val prompt by rememberPrompt("", "")
    var textLogs by remember { mutableStateOf("") }
    val stateVertical = rememberScrollState(0)
    //ここでStreamの読み取りは危険かもしれぬ
    LaunchedEffect(Unit) {
        launch {
            initialize(object : ConsoleInterface {
                override fun prompt(promptText: String, value: String) {
                    prompt.newPrompt(promptText, value)
                }
            })
            println("End:Init")
        }

        logStream.collect {
            textLogs += it + "\n"
            stateVertical.scrollTo(stateVertical.maxValue)
        }
    }
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray)) {
            Column(
                modifier = Modifier.fillMaxSize().onSizeChanged {
                   coroutine.launch {
                       stateVertical.scrollTo(stateVertical.maxValue)
                   }
                }.verticalScroll(stateVertical).padding(end = 5.dp),
                verticalArrangement = Arrangement.spacedBy((-5).dp, Alignment.Top)
            ) {
                SelectionContainer {
                    Text(
                        textLogs,
                        overflow = TextOverflow.Visible,
                        color = Color.White,
                        fontSize = 25.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                BasicTextField(
                    prompt.textFieldValue,
                    onValueChange = {
                        prompt.updateValue(it) { value, _ ->
                            println("Debug:Updated $value")
                        }
                    },
                    textStyle =
                    TextStyle(
                        color = Color.White,
                        fontSize =
                        25.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    cursorBrush = SolidColor(Color.White),
                    modifier = Modifier.fillMaxWidth().onPreviewKeyEvent {
                        return@onPreviewKeyEvent if (it.key == Key.Enter && it.type == KeyEventType.KeyUp && prompt.isEnable) {
                            textLogs += prompt.textFieldValue.text + "\n"
                            consoleWriter.println(prompt.getValue())
                            prompt.reset()
                            true
                        } else it.key == Key.Enter

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

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Console") {
        App()
    }
}
