package component

import androidx.compose.runtime.Composable

@Composable
actual fun Suggester(
    isExtended: Boolean,
    onDismiss: () -> Unit,
    candidacies: List<String>,
    selected: (String) -> Unit
) {
    Unit
}