import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.js.Promise

actual fun exitProcess(code: Int): Nothing {
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