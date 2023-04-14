package me.naotiki.ese.core.utils

private val regex=Regex("%-?\\d*\\.?\\d*[bhscdoxeftBHSCXET][YmdHOMSLDT]?")

/**
 * JSにはformatが無いので置換するだけの関数です、
 */
actual fun String.format(vararg args: Any?): String {
    var index=0
    return regex.replace(this){
        args[index++].toString()
    }
}

