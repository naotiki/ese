package component

import PlatformBackend
import LocalDefaultFont
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import platform
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.text.Regex.Companion.fromLiteral

expect fun getSystemLineSeparator():String
val lineSeparator = getSystemLineSeparator()
val annotatedLineSeparator=AnnotatedString(lineSeparator)
class TextLogState(maxLineCount: Int) {
    val maxLineCount by mutableStateOf(maxLineCount)
    var lines = mutableStateListOf("")//駄目ならmutableStateOf(listOf(""))
    private fun newLine() {
        //lastLine+= lineSeparator
        //lines = if (lines.size > maxLineCount) listOf("") + lines.dropLast(1) else listOf("") + lines
        lines.add(0, "")
        //Overflow時 TODO ログファイル的なのに書き込む？
        if (lines.size > maxLineCount) lines.removeAt(lines.lastIndex)
    }

    //正常に表示できない文字列
    private val replaceMap = mapOf("\t" to "    ", "\r" to "")
    private fun escape(value: String): String {
        return replaceMap.toList().fold(value) { acc, (old, new) ->
            acc.replace(old, new)
        }
    }

    private var firstLine
        get() = lines[0]
        set(value) {
            //lines = lines.toMutableList().apply { set(0, value) }
            lines[0] = escape(value)
        }
    private var lastLine
        get() = lines[lines.lastIndex]
        set(value) {
            //lines = lines.toMutableList().apply { set(lines.lastIndex, value) }
            lines[lines.lastIndex] = escape(value)
        }

    private val mutex = Mutex()
    fun addString(line: CharSequence) {
        val separatedString = line.lines()
        firstLine += separatedString.first()
        newLine()
        separatedString.drop(1).forEach {
            firstLine += it
            newLine()
        }
    }

    suspend fun addChar(char: Char) = mutex.withLock {
        if (char in lineSeparator) {
            if (char == lineSeparator.last()) newLine()
            return
        }
        firstLine += char
    }

    fun clear() {
        // lines = listOf("")
        firstLine = ""
        lines.removeRange(1, lines.lastIndex)

    }
}

@Composable
fun rememberTextLogState(initialLineCount: Int) = remember {
    TextLogState(initialLineCount)
}

//SelectionContainer has the bug on JS Platform
@Composable
expect fun SelectionContainer(content: @Composable () -> Unit)

@OptIn(ExperimentalTextApi::class)
@Composable
fun TextLog(
    state: TextLogState,
    lazyListState: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    footer: @Composable (() -> Unit)? = null
) {


    Box(modifier = Modifier.fillMaxSize().then(modifier)) {
        SelectionContainer {
            LazyColumn(Modifier.fillMaxWidth(), state = lazyListState, reverseLayout = true) {
                footer?.let {

                    item {
                        DisableSelection {
                            it()
                        }
                    }
                }
                items(state.lines.count()) {
                    BoxWithConstraints {
                        Column(modifier = Modifier.fillParentMaxWidth()) {
                            val textMeasurer = rememberTextMeasurer()
                            val text = AnnotatedString(state.lines.getOrNull(it) ?: run {
                                println("LazyList has got null")
                                ""
                            })

                            //in JS, Not Working
                            val result = textMeasurer.measure(
                                text,
                                overflow = TextOverflow.Visible,
                                constraints = this@BoxWithConstraints.constraints,
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = fontSize,
                                    fontFamily = LocalDefaultFont.current,
                                    letterSpacing = letterSpacing,
                                    lineBreak = LineBreak.Simple
                                )
                            ).takeIf { platform.backend != PlatformBackend.JS }
                            repeat(result?.lineCount ?: 1) { lineIndex ->
                                val start = result?.getLineStart(lineIndex) ?: 0
                                val end = result?.getLineEnd(lineIndex) ?: text.length
                                //JS Bug avoidance
                                val shown =
                                    //本当の改行にしか改行コードを付与しない
                                    if ((result?.lineCount?.minus(1) ?: 0) == lineIndex && platform!=Platform.Android) {
                                        text.subSequence(start, end).plus(annotatedLineSeparator)
                                    } else {
                                        text.subSequence(start, end)
                                    }
                                println("L$it : $shown")
                                BasicText(
                                    shown,
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = fontSize,
                                        fontFamily = LocalDefaultFont.current,
                                       //letterSpacing = letterSpacing,
                                    ),
                                    softWrap = true,
                                    overflow = TextOverflow.Visible,
                                    //コピーのときのみ改行される
                                    minLines = 1,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                }

            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = lazyListState,
            reverseLayout = true

        )
    }

}

@Composable
expect fun VerticalScrollbar(modifier: Modifier, adapter: LazyListState, reverseLayout: Boolean)

private fun AnnotatedString.Builder.addStyle(style: SpanStyle, text: String, regexp: String) {
    addStyle(style, text, fromLiteral(regexp))
}

private fun AnnotatedString.Builder.addStyle(style: SpanStyle, text: String, regexp: Regex) {
    for (result in regexp.findAll(text)) {
        addStyle(style, result.range.first, result.range.last + 1)
    }
}

const val sample = "1234567890123456789012345678901234567890123456789"

/*
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
                            */
/*repeat(1000) {
                                state.addString("yes$it\n")
                                yield()
                            }*//*

                        }
                    }
                    TextLog(state, fontSize = 20.sp)
                }
            }
        }
    }
}*/
