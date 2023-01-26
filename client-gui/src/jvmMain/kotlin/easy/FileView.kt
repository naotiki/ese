package easy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import core.Vfs

@Composable
fun EasyFileView(){
    val dir by Vfs.currentDirectoryFlow.collectAsState()
}