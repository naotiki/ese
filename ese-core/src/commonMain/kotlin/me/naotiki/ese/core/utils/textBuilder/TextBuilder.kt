package me.naotiki.ese.core.utils.textBuilder

class TextBuilder {
    var text: String = ""
    inline fun block(content:()->Unit){

    }
    operator fun String.unaryPlus(){
        text
    }
}

inline fun textBuilder(crossinline builder: TextBuilder.() -> Unit): String {
    return TextBuilder().apply { builder() }.text
}