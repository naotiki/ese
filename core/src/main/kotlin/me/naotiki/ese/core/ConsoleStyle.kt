package me.naotiki.ese.core

const val esc: Char = '\u001b'

interface ANSI<T : ANSIString> {
    val postfix: String
    val value: String
    fun ANSIBuilder<T>.wrap(): T
    fun wrap(): T
}

fun a() {
}

interface ANSIString {
    val string: String
    fun pr() = print(string)
}

fun List<ANSIString>.out() = forEach { it.pr() }

class ANSIBuilder<T : ANSIString> {
    private val values = mutableListOf<ANSI<T>>()
    operator fun ANSI<T>.unaryPlus() {
        values.add(this)
    }
}

fun <T : ANSI<T>> builder(vararg values: T): String =
    esc + "[" + values.joinToString(";") + values.first().postfix

@JvmInline
value class Style(override val string: String) : ANSIString {
    companion object{
        val reset = "$esc[0m"
    }
}

/*operator fun core.ANSI<core.Style>.plus(b: core.ANSI<core.Style>): core.Style {
    return core.Style("")
}*/
abstract class ANSIStyle : ANSI<Style> {
    override val postfix: String = "m"
}

sealed class Color(override val value: String) : ANSIStyle() {
    override fun ANSIBuilder<Style>.wrap(): Style =
        Style(value)
    override fun wrap(): Style =
        Style(value)

    object Black : Color("30")
    object Red : Color("31")
    object Green : Color("32")
    object Yellow : Color("33")
    object Blue : Color("34")
    object Magenta : Color("35")
    object Cyan : Color("36")
    object White : Color("37")
    object BGBlack : Color("40")
    object BGRed : Color("41")
    object BGGreen : Color("42")
    object BGYellow : Color("43")
    object BGBlue : Color("44")
    object BGMagenta : Color("45")
    object BGCyan : Color("46")
    object BGWhite : Color("47")
    data class HEXColor(val hex: UByte) : Color("38;5;$hex")
    data class BGHEXColor(val hex: UByte) : Color("48;5;$hex")


}

sealed class CharType(override val value: String) : ANSIStyle() {
    override fun ANSIBuilder<Style>.wrap(): Style {
        return Style("")
    }

    override fun wrap() = Style(value)

    object Bold : CharType("1")
    object Thin : CharType("2")
    object Italic : CharType("3")
    object UnderLine : CharType("4")

    @Deprecated("一部環境非対応")
    object Blink : CharType("5")

    @Deprecated("一部環境非対応")
    object FastBlink : CharType("6")
    object ReplacementColor : CharType("7")
    object Hide : CharType("8")

    /**
     * What's this???
     */
    object Cancel : CharType("9")


}


