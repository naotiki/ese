import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import me.naotiki.ese.core.Shell
import me.naotiki.ese.core.Shell.Expression
import me.naotiki.ese.core.commands.Expression
import me.naotiki.ese.core.programArg
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.load
import org.jetbrains.compose.resources.resource


/*@OptIn(ExperimentalResourceApi::class)
suspend fun loadFont(): Font {
    return Font("SourceHanCode", resource("SourceHanCodeJP.ttc").readBytes())
}*/

class ApplicationViewModel  {
    fun cancelCommand() {
        Expression.cancelJob()
    }
}

val LocalDefaultFont = compositionLocalOf {
    FontFamily.Monospace as FontFamily
}
var japaneseFont: FontFamily? = null
val DefaultFont: FontFamily get() = japaneseFont ?: FontFamily.Monospace
fun initializeComposeCommon(args: Array<out String> = emptyArray()) {
    programArg(args.toList())
}

@Composable
fun AppContainer(content: @Composable () -> Unit) {
    load {
        japaneseFont = FontFamily(getDefaultFont())
    }
    CompositionLocalProvider(
        LocalDefaultFont provides (japaneseFont ?: FontFamily.Monospace),
        content = content
    )
}

@OptIn(ExperimentalResourceApi::class)
suspend fun getDefaultFont(): Font {
    return androidx.compose.ui.text.platform.Font(
        "UDEVGothic35LG", resource("UDEVGothic35LG-Regular.ttf").readBytes(),
    )
}

@Composable
fun rememberAppViewModel() = remember { ApplicationViewModel() }


