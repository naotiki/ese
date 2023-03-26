import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import me.naotiki.ese.core.commands.Expression
import me.naotiki.ese.core.prepareKoinInjection
import me.naotiki.ese.core.programArg
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.load
import org.jetbrains.compose.resources.resource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


/*@OptIn(ExperimentalResourceApi::class)
suspend fun loadFont(): Font {
    return Font("SourceHanCode", resource("SourceHanCodeJP.ttc").readBytes())
}*/

class ApplicationViewModel : KoinComponent {
    val expression by inject<Expression>()
    fun cancelCommand() {
        expression.cancelJob()
    }
}

val LocalDefaultFont = compositionLocalOf {
    FontFamily.Monospace as FontFamily
}
var japaneseFont: FontFamily? = null
val DefaultFont: FontFamily get() = japaneseFont ?: FontFamily.Monospace
fun initializeComposeCommon(args: Array<out String> = emptyArray()) {
    programArg(args.toList())
    //DIよーい！！！！！！
    prepareKoinInjection()

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


