package core.utils

fun String.splitArgs()= replace(" {2,}".toRegex()," ").split(' ')