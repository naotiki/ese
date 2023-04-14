package component

import androidx.compose.runtime.Composable

@Composable
expect fun Suggester(
    isExtended: Boolean,
    onDismiss: () -> Unit,
    candidacies: List<String>,
    selected: (String) -> Unit
)