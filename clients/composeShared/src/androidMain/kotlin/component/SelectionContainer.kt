package component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun SelectionContainer(content: @Composable () -> Unit){
    return content()
    androidx.compose.foundation.text.selection
        .SelectionContainer(content=content)
}


@Composable
actual fun VerticalScrollbar(
    modifier: Modifier,
    adapter: LazyListState,
    reverseLayout: Boolean
) {

}

actual fun getSystemLineSeparator(): String = System.getProperty("line.separator")?:"\n"