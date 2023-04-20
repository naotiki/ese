import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontLoadingStrategy
import me.naotiki.ese.shared.R

actual suspend fun getDefaultFont(): Font {
    return Font(R.font.udev_regular, loadingStrategy = FontLoadingStrategy.Async)
}