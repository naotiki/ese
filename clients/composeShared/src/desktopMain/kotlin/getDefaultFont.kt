import androidx.compose.ui.text.font.Font
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.resource

@OptIn(ExperimentalResourceApi::class)
actual suspend fun getDefaultFont(): Font {
    return androidx.compose.ui.text.platform.Font(
        "UDEVGothic35LG", resource("UDEVGothic35LG-Regular.ttf").readBytes(),
    )
}