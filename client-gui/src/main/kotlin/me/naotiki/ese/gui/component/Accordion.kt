package me.naotiki.ese.gui.component

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun Accordion(title: @Composable () -> Unit,defaultOpen:Boolean=false, body: @Composable () -> Unit) {
    var isOpen by remember {
        mutableStateOf(defaultOpen)
    }
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(2.dp).clickable {
                isOpen = !isOpen
            }, verticalAlignment = CenterVertically
        ) {
            Icon(
                if (isOpen) Icons.Default.KeyboardArrowDown
                else Icons.Default.KeyboardArrowRight, null
            )
            title()
        }
        if (isOpen) {
            Row {
                Spacer(
                    Modifier.width(26.dp)
                )
                body()
            }
        }

    }
}

@Preview
@Composable
fun Preview() {
    Accordion({ Text("Title") }) {
        Text("くぁwせdrftgyhjkl；：")
    }
}