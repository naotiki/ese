package component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun VerticalScrollbar(
    modifier: Modifier,
    adapter: LazyListState,
    reverseLayout: Boolean
) {
    Unit
}

@Composable
actual fun SelectionContainer(content: @Composable () -> Unit) {
    content()
}

actual fun getSystemLineSeparator(): String ="\n"