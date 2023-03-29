package me.naotiki.ese.gui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield
import kotlin.text.Regex.Companion.fromLiteral

val lineSeparator = '\n'

class TextLogState(maxLineCount: Int) {
    val maxLineCount by mutableStateOf(maxLineCount)

    var lines by mutableStateOf(listOf(""))
    private fun newLine() {
        //lastLine+= lineSeparator
        lines= if (lines.size > maxLineCount) lines.plus("").drop(1) else lines.plus("")
       /* lines.add("")
        //Overflow時 TODO ログファイル的なのに書き込む？
        if (lines.size > maxLineCount) lines.removeAt(0)*/
    }

    private var lastLine
        get() = lines[lines.lastIndex]
        set(value) {
            lines=lines.toMutableList() .apply{ set(lines.lastIndex, value) }
            //lines[lines.lastIndex] = value
        }

    val mutex= Mutex()
    fun addString(line: CharSequence) {
        arrayOfNulls<String>(50).indexOfLast { it != null }
        val separatedString = line.lines()
        lastLine += separatedString.first()
        newLine()
        separatedString.drop(1).forEach {
            lastLine += it
            newLine()
        }
    }

    suspend fun addChar(char: Char) =mutex.withLock{
        if (char == lineSeparator) {
            newLine()
            return
        }
        lastLine += char
    }

    fun clear() {
        lines= emptyList()
        //lines.clear()
        newLine()
    }
}

@Composable
fun rememberTextLogState(initialLineCount: Int) = remember {
    TextLogState(initialLineCount)
}

@Composable
fun TextLog(state: TextLogState,modifier: Modifier=Modifier, fontSize: TextUnit = TextUnit.Unspecified,) {
    val lazyListState = rememberLazyListState()
    val lastItemIndex by remember {
        derivedStateOf {
            lazyListState.layoutInfo.totalItemsCount -1
        }
    }
    LaunchedEffect(state.lines){
        lazyListState.scrollToItem(index = state.lines.lastIndex.coerceIn(0,3999))
    }
    SelectionContainer(Modifier.fillMaxWidth().then(modifier)) {
        LazyColumn(Modifier.fillMaxWidth(), state = lazyListState, /*reverseLayout = true*/) {
            itemsIndexed(state.lines, key = { index, _ -> index }) { _, it->
                Text(
                    it,
                    Modifier.fillMaxWidth(),
                    overflow = TextOverflow.Visible,
                    color = Color.LightGray,
                    fontSize = fontSize,

                    fontFamily = FontFamily.Monospace,
                   // maxLines = 1
                )
            }
        }
    }

}

private fun AnnotatedString.Builder.addStyle(style: SpanStyle, text: String, regexp: String) {
    addStyle(style, text, fromLiteral(regexp))
}

private fun AnnotatedString.Builder.addStyle(style: SpanStyle, text: String, regexp: Regex) {
    for (result in regexp.findAll(text)) {
        addStyle(style, result.range.first, result.range.last + 1)
    }
}

const val sample = "1234567890123456789012345678901234567890123456789"
private fun main()= runBlocking {
    val annotatedString = buildAnnotatedString {
        append(sample)
        addStyle(SpanStyle(Color.Red), sample, "0")
    }
    AnnotatedString(sample, SpanStyle())

    application {
        Window(onCloseRequest = {exitApplication()}) {
            MaterialTheme {
                Surface {
                    val state= rememberTextLogState(4000)
                    LaunchedEffect(Unit){
                        launch {
                            delay(1000)
                            repeat(1000){
                                state.addString("yes$it\n")
                                yield()
                            }
                        }
                    }
                    TextLog(state, fontSize = 20.sp)
                }
            }
        }
    }
}