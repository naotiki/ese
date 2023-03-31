package me.naotiki.ese.core.utils

actual class AtomicInteger {
    private var value=0
    fun getAndIncrement(): Int {
        return value++
    }
    actual fun increment() {
        value++
    }

    actual fun get(): Int {
        return value
    }
}