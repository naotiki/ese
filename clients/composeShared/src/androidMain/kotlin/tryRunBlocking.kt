import android.app.Activity
import android.os.Process
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