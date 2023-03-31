import androidx.compose.ui.window.Window
import kotlinx.atomicfu.atomic
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Promise

actual fun exitProcess(code: Int): Nothing {
    throw NotImplementedError("Not Supported")
}



actual fun <T> runBlocking(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T
) {

    GlobalScope.launch {
        block()
    }
}