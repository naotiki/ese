package easy
import CustomKoinComponent
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import component.Accordion
import core.commands.Expression
import core.commands.parser.Executable
import core.vfs.ExecutableFile
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.java.KoinJavaComponent.inject

class CommandPanelViewModel  : CustomKoinComponent() {
    private val ex by inject<Expression>()
    fun getExecutablesList(): List<Executable<*>> {
        println("exe")
        return ex.getExecutables().map { it.executable }
    }
}

@Composable
fun rememberCommandPanelViewModel() = remember { CommandPanelViewModel() }

@Composable
fun CommandPanel(){
    val viewModel= rememberCommandPanelViewModel()
    var executables by remember { mutableStateOf(emptyList<Executable<*>>()) }
    LaunchedEffect(Unit) {
        executables=viewModel.getExecutablesList()
    }
    executables.forEach {
        Accordion({ Text(it.name) }) {
            var isHelpOpen by remember { mutableStateOf(false) }
            Text(it.description?:"(説明文無し)", modifier = Modifier.clickable {
                isHelpOpen=!isHelpOpen
            })
            if (isHelpOpen){
                Window(onCloseRequest = { isHelpOpen = false }, alwaysOnTop = true,title = "${it.name} Help") {
                    val stateVertical= rememberScrollState()
                    Box(Modifier.fillMaxSize()){
                        Box(Modifier.fillMaxSize().verticalScroll(stateVertical)) {
                            Text(it.generateHelpText(), Modifier.padding(5.dp))
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