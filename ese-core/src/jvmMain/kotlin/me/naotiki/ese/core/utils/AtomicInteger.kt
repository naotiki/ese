package me.naotiki.ese.core.utils

import java.util.concurrent.atomic.AtomicInteger

actual class AtomicInteger {
    private val atomicInteger=AtomicInteger()
    actual fun increment() {
        atomicInteger.incrementAndGet()
    }

    actual fun get(): Int {
        return atomicInteger.get()
    }
}