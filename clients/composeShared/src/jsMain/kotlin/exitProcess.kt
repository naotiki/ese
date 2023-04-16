import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.js.Promise

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