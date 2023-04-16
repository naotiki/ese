package component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import me.naotiki.ese.R
import org.jetbrains.compose.resources.resource

@Composable
actual fun SelectionContainer(content: @Composable () -> Unit)=
    androidx.compose.foundation.text.selection
        .SelectionContainer(content=content)

@Composable
actual fun VerticalScrollbar(
    modifier: Modifier,
    adapter: LazyListState,
    reverseLayout: Boolean
) {

}

