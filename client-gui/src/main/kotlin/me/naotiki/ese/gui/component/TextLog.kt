package me.naotiki.ese.gui.component

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
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
import kotlin.text.Regex.Companion.fromLiteral

val lineSeparator = '\n'

class TextLogState(maxLineCount: Int) {
    val maxLineCount by mutableStateOf(maxLineCount)
    var lines =mutableStateListOf("")//駄目ならmutableStateOf(listOf(""))
    private fun newLine() {
        //lastLine+= lineSeparator
        //lines = if (lines.size > maxLineCount) listOf("") + lines.dropLast(1) else listOf("") + lines
          lines.add(0,"")
           //Overflow時 TODO ログファイル的なのに書き込む？
           if (lines.size > maxLineCount) lines.removeAt(lines.lastIndex)
    }

    private var firstLine
        get() = lines[0]
        set(value) {
            //lines = lines.toMutableList().apply { set(0, value) }
            lines[0] = value
        }
    private var lastLine
        get() = lines[lines.lastIndex]
        set(value) {
            //lines = lines.toMutableList().apply { set(lines.lastIndex, value) }
             lines[lines.lastIndex] = value
        }

    val mutex = Mutex()
    fun addString(line: CharSequence) {
        arrayOfNulls<String>(50).indexOfLast { it != null }
        val separatedString = line.lines()
        firstLine += separatedString.first()
        newLine()
        separatedString.drop(1).forEach {
            firstLine += it
            newLine()
        }
    }

    suspend fun addChar(char: Char) = mutex.withLock {
        if (char == lineSeparator) {
            newLine()
            return
        }
        firstLine += char
    }

    fun clear() {
       // lines = listOf("")
        firstLine = ""
        lines.removeRange(1,lines.lastIndex)

    }
}

@Composable
fun rememberTextLogState(initialLineCount: Int) = remember {
    TextLogState(initialLineCount)
}


@OptIn(ExperimentalTextApi::class)
@Composable
fun TextLog(
    state: TextLogState, modifier: Modifier = Modifier, fontSize: TextUnit = TextUnit.Unspecified,
    letterSpacing: TextUnit = TextUnit.Unspecified, footer: @Composable (() -> Unit)? =null
) {
    val lazyListState = rememberLazyListState()

    SelectionContainer(Modifier.fillMaxWidth().then(modifier)) {

        LazyColumn(Modifier.fillMaxWidth().then(modifier), state = lazyListState, reverseLayout = true) {
            footer?.let {
                item {
                    it()
                }
            }
            items(state.lines.count()) {

                BoxWithConstraints {
                    Column(modifier=Modifier.fillParentMaxWidth()) {
                        val textMeasurer = rememberTextMeasurer(0)
                        val text = AnnotatedString(state.lines.getOrNull(it) ?: run {
                            println("LazyList has got null")
                            ""
                        })

                        val result = textMeasurer.measure(
                            text,
                            overflow = TextOverflow.Visible,
                            constraints = this@BoxWithConstraints.constraints,
                            style = TextStyle(
                                color = Color.LightGray,
                                fontSize = fontSize,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = letterSpacing,
                            )
                        )
                        repeat(result.lineCount) { lineIndex ->
                            val start=result.getLineStart(lineIndex)
                            val end=result.getLineEnd(lineIndex)
                            Text(
                                text.subSequence(start,end).plus(AnnotatedString("\n")),
                                Modifier.fillMaxWidth(),
                                style = TextStyle(
                                    color = Color.LightGray,
                                    fontSize = fontSize,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = letterSpacing,
                                ),
                                overflow = TextOverflow.Visible,
                                maxLines = 1
                            )

                        }
                    }
                }


                /* Text(
                     state.lines.getOrNull(it) ?: run {
                         println("LazyList has got null")
                         ""
                     },

                     Modifier.fillMaxWidth(),
                     overflow = TextOverflow.Visible,
                     color = Color.LightGray,
                     fontSize = fontSize,
                     fontFamily = FontFamily.Monospace,
                     letterSpacing = letterSpacing,

                     //   maxLines = 1,
                     onTextLayout = {
                         println(it.getLineStart(1.coerceIn(0, it.lineCount - 1)))
                     }
                 )*/

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

@OptIn(ExperimentalTextApi::class)
private fun main() = runBlocking {
    val annotatedString = buildAnnotatedString {
        append(sample)
        addStyle(SpanStyle(Color.Red), sample, "0")
    }



    application {
        Window(onCloseRequest = { exitApplication() }) {
            MaterialTheme {
                Surface {
                    val state = rememberTextLogState(4000)
                    LaunchedEffect(Unit) {
                        launch {
                            delay(1000)
                            state.addString(sample)
                            /*repeat(1000) {
                                state.addString("yes$it\n")
                                yield()
                            }*/
                        }
                    }
                    TextLog(state, fontSize = 20.sp)
                }
            }
        }
    }
}