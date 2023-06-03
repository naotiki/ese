package me.naotiki.ese.core

import kotlinx.coroutines.sync.Mutex
import me.naotiki.ese.core.EseSystem.UserManager
import me.naotiki.ese.core.VirtualSingletonKey.Companion.vsk
import me.naotiki.ese.core.user.Group
import me.naotiki.ese.core.user.User
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * It's used for switching [VirtualSingletonManager] context.
 * @sample [VirtualSingletonManager.DefaultKey]
 */
@JvmInline
value class VirtualSingletonKey(val key: Any) {

    companion object {
        internal val Any.vsk get() = VirtualSingletonKey(this)
    }
}


/**
 * The VSM make singleton able to switch.
 * */
abstract class VirtualSingletonManager {
    private var currentKey: VirtualSingletonKey = DefaultKey
    private val keys = mutableSetOf(DefaultKey)
    val virtualSingletonsMap = mutableMapOf<KType, VirtualSingleton<*>>()

    inline fun <reified T> getSingleton(): VirtualSingleton<T> {
        @Suppress("UNCHECKED_CAST")
        return (virtualSingletonsMap[typeOf<T>()] ?: TODO("Instance Not Found")) as VirtualSingleton<T>
    }
    private val factoryContext = FactoryContext()
    inner class FactoryContext {
        inline fun <reified T> inject(): T {
            return getSingleton<T>().getInstance(this@VirtualSingletonManager)
        }
    }
    inner class VirtualSingleton<T>(val factory: FactoryContext.() -> T) {
        private val mutex= Mutex()
        private val instanceMap = mutableMapOf<VirtualSingletonKey, T>()
        fun getInstance(vst: VirtualSingletonManager): T {
            @Suppress("ControlFlowWithEmptyBody")
            while (!mutex.tryLock());
            return instanceMap.getOrPut(vst.currentKey) {
                factoryContext.factory()
            }.also {
                mutex.unlock()
            }
        }

        operator fun getValue(vst: VirtualSingletonManager, property: KProperty<*>): T {
            return getInstance(vst)
        }

    }

    inner class LazyVirtualSingleton<T> {
        private val instanceMap = mutableMapOf<VirtualSingletonKey, T>()
        operator fun setValue(vst: VirtualSingletonManager, property: KProperty<*>, value: T) {
            instanceMap[vst.vsk] = value
        }

        private fun getInstance(vst: VirtualSingletonManager): T {
            return instanceMap[vst.currentKey] ?: TODO("Not Initialized")
        }

        operator fun getValue(vst: VirtualSingletonManager, property: KProperty<*>): T {
            return getInstance(vst)
        }
    }

    fun <T> lazyVirtualSingle(): LazyVirtualSingleton<T> {
        return LazyVirtualSingleton()
    }

    inline fun <reified T> virtualSingle(noinline factory: FactoryContext.() -> T): VirtualSingleton<T> {
        return VirtualSingleton(factory).also {
            virtualSingletonsMap[typeOf<T>()] = it
        }
    }
    /**
     * Switch to the existing context.
     * @param key Existing key
     */
    fun switch(key: VirtualSingletonKey) {
        val existsKey = keys.find { it == key }
        if (existsKey != null) {
            this.currentKey = existsKey
        } else TODO("IllegalState")
    }

    /**
     * Create new context.
     * @param key new context
     * @param switch Switch context after creating. Default value is true.
     */
    fun create(key: VirtualSingletonKey, switch: Boolean = true) {
        val isCreated = keys.add(key)
        check(isCreated)
        if (switch) {
            switch(key)
        }
    }

    companion object {
        @JvmStatic
        val DefaultKey = Unit.vsk
    }
}





