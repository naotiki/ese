package me.naotiki.ese.core

class Variable {
    val nameRule = Regex("[A-z]+")
    val map = mutableMapOf<String, String>()

    fun expandVariable(string: String): String {
        println(string)
        return Regex("\\$$nameRule").replace(string) {
            map.getOrDefault(it.value.trimStart('$'), "")
        }
    }
}