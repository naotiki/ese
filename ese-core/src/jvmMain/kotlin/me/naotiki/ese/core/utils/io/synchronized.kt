package me.naotiki.ese.core.utils.io

actual inline fun <R> trySynchronized(lock: Any, block: () -> R):R = kotlin.synchronized(lock, block)