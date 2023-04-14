package component

import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList

@Composable
actual fun Suggester(isExtended:Boolean,onDismiss:()->Unit,candidacies:List<String>,selected:(String)->Unit){
    val currentCandidacies = remember { candidacies.toMutableStateList() }
    DropdownMenu(isExtended,onDismiss,focusable = false){
        currentCandidacies.forEach {
            DropdownMenuItem({selected(it)}){
                Text(it)
            }
        }
    }
}