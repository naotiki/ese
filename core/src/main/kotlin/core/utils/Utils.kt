package core.utils

fun String.splitArgs()= replace(" {2,}".toRegex()," ").split(' ')

fun <E : Enum<E>> Enum<E>.getFlag(): Int = 1 shl ordinal

fun normalizeYesNoAnswer(input:String): Boolean? {
    return when (input.lowercase()) {
        "y" -> true
        "yes"->true
        "n"->false
        "no"->false
        else -> null
    }
}

fun <T:Any?> T.log()= also {
    println(it)
}