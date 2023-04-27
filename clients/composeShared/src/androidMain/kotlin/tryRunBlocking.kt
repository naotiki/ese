import android.app.Activity
import android.content.Context
import android.view.KeyEvent.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.NativeKeyEvent
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext

actual fun <T> tryRunBlocking(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T
) {
    runBlocking(context, block)
}
var targetActivity:Activity?=null
actual fun exitApp(code: Int) {

    targetActivity?.finishAndRemoveTask()
}

@Composable
actual fun VirtualKeyboard(modifier: Modifier,) {
    Row(Modifier.fillMaxWidth().background(Color.White).then(modifier),) {
        TextButton({
            VirtualKeyboardManager.press(KeyEvent(NativeKeyEvent(ACTION_DOWN, KEYCODE_TAB)))
        },Modifier){
            Text("Tab")
        }
        IconButton({
            VirtualKeyboardManager.press(KeyEvent(NativeKeyEvent(ACTION_DOWN, KEYCODE_DPAD_UP)))
        }){
            Icon(Icons.Default.KeyboardArrowUp,null)
        }
        IconButton({
            VirtualKeyboardManager.press(KeyEvent(NativeKeyEvent(ACTION_DOWN, KEYCODE_DPAD_DOWN)))
        }){
            Icon(Icons.Default.KeyboardArrowDown,null)
        }
        TextButton({
            val mil=System.currentTimeMillis()
            VirtualKeyboardManager.press(KeyEvent(NativeKeyEvent(mil,mil,ACTION_DOWN, KEYCODE_C ,1, META_CTRL_MASK)))
        },Modifier){
            Text("Ctrl+C")
        }
    }
}