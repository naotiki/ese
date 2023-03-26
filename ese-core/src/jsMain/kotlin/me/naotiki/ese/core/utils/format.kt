package me.naotiki.ese.core.utils

private val magicalRegex=Regex("%-?\\d*\\.?\\d*[bhscdoxeftBHSCXET][YmdHOMSLDT]?")
actual fun String.format(vararg args: Any?): String {
    var index=0
    return magicalRegex.replace(this){
        args[index++].toString()
    }
}

