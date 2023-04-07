package component.assistant
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key.Companion.Window
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import component.Accordion
import component.VerticalScrollbar
import me.naotiki.ese.core.commands.Expression
import me.naotiki.ese.core.vfs.ExecutableFile
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CommandPanelViewModel  : KoinComponent {
    private val ex by inject<Expression>()
    fun getExecutablesList(): List<ExecutableFile<*>> {
        return ex.getExecutables(includeHidden = false)
    }
}

@Composable
fun rememberCommandPanelViewModel() = remember { CommandPanelViewModel() }

@Composable
fun CommandPanel(){
    val viewModel= rememberCommandPanelViewModel()
    var executables by remember { mutableStateOf(emptyList<ExecutableFile<*>>()) }
    LaunchedEffect(Unit) {
        executables=viewModel.getExecutablesList()
    }
    executables.forEach {
        Accordion({ Text(it.name) }) {
            var isHelpOpen by remember { mutableStateOf(false) }
            Text(it.description?:"(説明文無し)", modifier = Modifier.fillMaxWidth().clickable {
                isHelpOpen=!isHelpOpen
            })
            if (isHelpOpen){
                Window(onCloseRequest = { isHelpOpen = false }, alwaysOnTop = true,title = "${it.name} Help") {
                    val stateVertical= rememberScrollState()
                    Box(Modifier.fillMaxSize()){
                        Box(Modifier.fillMaxSize().verticalScroll(stateVertical)) {
                            SelectionContainer {
                                Text(it.generateHelpText(), Modifier.padding(5.dp))
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
            }

        }
    }
}