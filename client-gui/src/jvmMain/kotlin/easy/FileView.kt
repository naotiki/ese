package easy

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
import core.user.UserManager
import core.vfs.Directory
import core.vfs.File
import core.vfs.FileSystem
import kotlinx.coroutines.flow.onEach
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FilePanelViewModel : KoinComponent {
    val um by inject<UserManager>()
    private val fs by inject<FileSystem>()
    val flow get() = fs.currentDirectoryFlow
    fun getChildren(dir:Directory): Map<String, File>? {
        return dir.getChildren(um.user)
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