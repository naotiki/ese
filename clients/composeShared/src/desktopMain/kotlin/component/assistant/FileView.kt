package component.assistant

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import component.Accordion
import me.naotiki.ese.core.EseSystem
import me.naotiki.ese.core.Shell
import me.naotiki.ese.core.vfs.Directory
import me.naotiki.ese.core.vfs.File

class FilePanelViewModel  {
    val flow get() = Shell.FileSystem.currentDirectoryFlow
    fun getChildren(dir:Directory): Map<String, File>? {
        return dir.getChildren(EseSystem.UserManager.user)
    }
}

@Composable
fun rememberFilePanelViewModel() = remember { FilePanelViewModel() }

@Composable
fun EasyFileView() {
    val viewModel = rememberFilePanelViewModel()
    val dir by viewModel.flow.collectAsState()
    Column {
        Text(dir.getFullPath().value)
        Accordion({
            Icon(Icons.Default.Folder,null)
            Text(dir.name)
        },true) {
            Column {
                viewModel.getChildren(dir)?.forEach { (t, u) ->
                    Row{
                        Spacer(Modifier.width(24.dp))
                        if (u is Directory) {
                            Icon(Icons.Default.Folder, null)
                        }
                        Text(t)
                    }
                }
            }

        }


    }
}