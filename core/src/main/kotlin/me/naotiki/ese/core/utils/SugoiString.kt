package me.naotiki.ese.core.utils

object SugoiString {
    fun CharSequence.splitPair(vararg filter:Char): Pair<CharSequence, CharSequence> {
        var one=""
        var two=""
        var splited=false
        forEach {
            if (splited||it !in filter){
                splited=true
                two+=it
            }else one+=it

        }
        return one to two
    }
}