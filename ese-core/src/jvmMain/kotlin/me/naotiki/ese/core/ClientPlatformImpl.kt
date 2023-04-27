package me.naotiki.ese.core

import me.naotiki.ese.core.commands.parser.ArgType
import java.io.File

actual interface PlatformImpl{
    fun getEseHomeDir(): File?
}

const val eseHomePropName="ese.homedir"
private const val developmentEseHome="EseHome"
val eseHomeDirPath = System.getProperty(eseHomePropName, developmentEseHome)
//For Desktop
fun getEseHomeDirByProp():File{
    return File(eseHomeDirPath)
}