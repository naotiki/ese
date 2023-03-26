package me.naotiki.ese.core.utils

actual fun String.format(vararg args: Any?): String = java.lang.String.format(this, *args)