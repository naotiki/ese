package me.naotiki.ese.core.utils
/**
 * 次の要素もしくはnullを返します。
 *
 * */
fun <T> ListIterator<T>.nextOrNull(): T? {
    return if (hasNext()) next() else null
}
fun <T> ListIterator<T>.previousOrNull(): T? {
    return if (hasPrevious()) previous() else null
}