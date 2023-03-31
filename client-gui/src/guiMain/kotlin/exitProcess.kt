import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext

actual fun exitProcess(code: Int): Nothing = kotlin.system.exitProcess(code)
actual fun <T> runBlocking(
    context: CoroutineContext,
     block: suspend CoroutineScope.() -> T
) {

     runBlocking(context,block)
}