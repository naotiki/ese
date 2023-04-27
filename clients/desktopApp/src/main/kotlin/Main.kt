import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.awaitApplication
import component.assistant.CommandPanel
import component.assistant.EasyFileView
import me.naotiki.ese.core.PlatformImpl
import me.naotiki.ese.core.appName
import me.naotiki.ese.core.getEseHomeDirByProp
import me.naotiki.ese.core.initializePlatformImpl
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.SplitPaneState
import java.io.File

@OptIn(ExperimentalComposeUiApi::class)
suspend fun main(vararg args: String) {
    println("Starting... $appName")
    println(System.getProperties().map {(k,v)->
        "$k : $v"
    }.joinToString("\n"))
    initializeComposeCommon(args)
    val clientPlatformImpl= object : PlatformImpl {
        override fun getEseHomeDir(): File = getEseHomeDirByProp()
    }
    initializePlatformImpl(clientPlatformImpl)
    awaitApplication {
        val appViewModel = rememberAppViewModel()
        // ... Content goes here ...
        // This part of Composition will see the `elevations` instance
        // when accessing LocalElevations.current
        Window(onCloseRequest = ::exitApplication, title = appName, onPreviewKeyEvent = {
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
            AppContainer {
                App(isAssistExtended)
            }


        }


    }
}

@OptIn(ExperimentalSplitPaneApi::class)
@Composable
fun App(isAssistExtended:Boolean){
    MaterialTheme {
        val splitState = remember(isAssistExtended) {
            SplitPaneState(
                if (isAssistExtended) 0.35f else 0f,
                isAssistExtended
            )
        }
        HorizontalSplitPane(splitPaneState = splitState) {
            first(0.dp) {
                val stateVertical = rememberScrollState(0)
                Box(Modifier.fillMaxSize()) {
                    Column(Modifier.fillMaxSize().padding(2.dp).verticalScroll(stateVertical)) {
                        Text(
                            "GUIアシスタント",
                            fontSize = 20.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
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
                    /*VerticalScrollbar(
                        modifier = Modifier.align(Alignment.CenterEnd)
                            .fillMaxHeight(),
                        adapter = rememberScrollbarAdapter(stateVertical), style = LocalScrollbarStyle.current.copy
                            (hoverColor = Color.LightGray, unhoverColor = Color.Gray, thickness = 5.dp)
                    )*/
                }
            }
            second(250.dp) {
                Terminal()
            }
        }
        //App(isAssistExtended)
    }
}
