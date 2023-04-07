import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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