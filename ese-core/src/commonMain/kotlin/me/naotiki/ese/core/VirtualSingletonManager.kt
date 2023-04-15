package me.naotiki.ese.core

import kotlinx.coroutines.sync.Mutex
import me.naotiki.ese.core.EseSystem.UserManager
import me.naotiki.ese.core.VirtualSingletonKey.Companion.key
import me.naotiki.ese.core.user.Group
import me.naotiki.ese.core.user.User
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@JvmInline
value class VirtualSingletonKey(val key: Any) {

    internal constructor(key: Unit) : this(key as Any)

    companion object {
        val Any.key get() = VirtualSingletonKey(this)
    }
}



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
            instanceMap[vst.key] = value
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

    fun switch(key: VirtualSingletonKey) {
        val existsKey = keys.find { it == key }
        if (existsKey != null) {
            this.currentKey = existsKey
        } else TODO("IllegalState")
    }

    fun create(key: VirtualSingletonKey, switch: Boolean = true) {
        val isCreated = keys.add(key)
        check(isCreated)
        if (switch) {
            switch(key)
        }
    }

    companion object {
        @JvmStatic
        val DefaultKey = Unit.key
    }
}


private fun main(args: Array<out String>) {
    println(UserManager.userList.map { it.name })
    UserManager.addUser(User(UserManager, "a", Group(UserManager, "fyguyh")))
    println(UserManager.userList.map { it.name })
    EseSystem.create(1.key)
    println(UserManager.userList.map { it.name })
}




