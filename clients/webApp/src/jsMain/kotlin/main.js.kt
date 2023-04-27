import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import me.naotiki.ese.core.Shell
import org.jetbrains.skiko.wasm.onWasmReady

class JSClientViewModel  {
    fun cancelJob() {
        Shell.Expression.cancelJob()
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

                }
            }
        }


    }
}