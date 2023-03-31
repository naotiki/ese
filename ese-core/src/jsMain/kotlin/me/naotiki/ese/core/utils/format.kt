package me.naotiki.ese.core.utils

import kotlin.reflect.KClass

val magicalRegex=Regex("%-?\\d*\\.?\\d*[bhscdoxeftBHSCXET][YmdHOMSLDT]?")
actual fun String.format(vararg args: Any?): String {
    var index=0
    return magicalRegex.replace(this){
        args[index++].toString()
    }
}

