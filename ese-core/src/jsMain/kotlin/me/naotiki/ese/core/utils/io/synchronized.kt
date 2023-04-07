package me.naotiki.ese.core.utils.io

// synchronized doesn't support JS
actual inline fun <R> trySynchronized(lock: Any, block: () -> R): R = block()
