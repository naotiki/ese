import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import me.naotiki.ese.core.commands.Expression
import me.naotiki.ese.core.prepareKoinInjection
import me.naotiki.ese.core.programArg
import org.jetbrains.skiko.wasm.onWasmReady
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class JSClientViewModel:KoinComponent{
    val ex by inject<Expression>()
    fun cancelJob(){
        ex.cancelJob()
    }
}
@Composable
fun rememberJSViewModel()= remember { JSClientViewModel() }

suspend fun main(){
    initializeComposeCommon()
    onWasmReady {

        Window("Ese Linux"){
            val vm=rememberJSViewModel()
            Box(modifier = Modifier.fillMaxSize()){
                Terminal()
                //App(false)*/
                Button({vm.cancelJob()},Modifier.align(Alignment.BottomEnd)) {
                    Text("コマンド中止", fontFamily = DefaultFont)
                }
            }
        }
    }
}