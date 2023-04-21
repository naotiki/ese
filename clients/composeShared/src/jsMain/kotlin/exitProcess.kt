import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.NativeKeyEvent
import kotlinx.coroutines.*
import org.jetbrains.skiko.SkikoInputModifiers
import org.jetbrains.skiko.SkikoKey
import org.jetbrains.skiko.SkikoKeyboardEventKind
import kotlin.coroutines.CoroutineContext

actual fun exitApp(code: Int) {
    throw NotImplementedError("Not Supported")
}



@OptIn(DelicateCoroutinesApi::class)
actual fun <T> tryRunBlocking(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T
) {
    GlobalScope.launch {
        block()
    }
}

@Composable
actual fun VirtualKeyboard(modifier: Modifier) {
    Row(Modifier.fillMaxWidth().background(Color.White).then(modifier),) {
        TextButton({
            VirtualKeyboardManager.press(KeyEvent(NativeKeyEvent(SkikoKey.KEY_TAB, kind = SkikoKeyboardEventKind.DOWN, platform = null)))
        },Modifier){
            Text("Tab")
        }
        IconButton({
            VirtualKeyboardManager.press(KeyEvent(NativeKeyEvent(SkikoKey.KEY_UP, kind = SkikoKeyboardEventKind.DOWN, platform = null)))
        }){
            Icon(Icons.Default.KeyboardArrowUp,null)
        }
        IconButton({
            VirtualKeyboardManager.press(KeyEvent(NativeKeyEvent(SkikoKey.KEY_DOWN, kind = SkikoKeyboardEventKind.DOWN, platform = null)))
        }){
            Icon(Icons.Default.KeyboardArrowDown,null)
        }
        TextButton({
            VirtualKeyboardManager.press(KeyEvent(NativeKeyEvent(SkikoKey.KEY_C, kind = SkikoKeyboardEventKind.DOWN, modifiers = SkikoInputModifiers.CONTROL, platform = null)))
        },Modifier){
            Text("Ctrl+C")
        }
    }
}