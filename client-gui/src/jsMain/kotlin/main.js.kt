import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import me.naotiki.ese.core.commands.Expression
import org.jetbrains.skiko.wasm.onWasmReady
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class JSClientViewModel : KoinComponent {
    val ex by inject<Expression>()
    fun cancelJob() {
        ex.cancelJob()
    }
}

@Composable
fun rememberJSViewModel() = remember { JSClientViewModel() }

fun main() {
    println("Starting... Ese Linux")
    initializeComposeCommon()
    onWasmReady {

        Window("Ese Linux") {
            AppContainer {
                val vm = rememberJSViewModel()
                Box(modifier = Modifier.fillMaxSize()) {

                    Terminal()
                    //App(false)*/
                    Button({ vm.cancelJob() }, Modifier.align(Alignment.BottomEnd)) {
                        Text("コマンド中止", fontFamily = LocalDefaultFont.current)
                    }
                }
            }


        }


    }
}