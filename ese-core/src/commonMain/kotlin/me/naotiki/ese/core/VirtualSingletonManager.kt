package me.naotiki.ese.core

import me.naotiki.ese.core.EseSystem.UserManager
import me.naotiki.ese.core.VirtualSingletonKey.Companion.key
import me.naotiki.ese.core.user.Group
import me.naotiki.ese.core.user.User
import me.naotiki.ese.core.vfs.FileSystem
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic
import kotlin.reflect.KProperty

@JvmInline
value class VirtualSingletonKey(val key: Any) {
    companion object {
        val Any.key get() =  VirtualSingletonKey(this)
    }
}
abstract class VirtualSingletonManager {
    private var currentKey: VirtualSingletonKey = DefaultKey
    private val keys = mutableSetOf(DefaultKey)
    val singletons = mutableMapOf<String, VirtualSingleton<*>>()
    inline fun <reified T> getSingleton(): VirtualSingleton<T> {
        return singletons.filter { (className, _) ->
            className == T::class.qualifiedName
        }.values.filterIsInstance<VirtualSingleton<T>>().single()
    }

    private val factoryContext = FactoryContext()

    inner class FactoryContext {
        inline fun <reified T> inject(): T {
            return getSingleton<T>().getInstance(this@VirtualSingletonManager)
        }
    }

    inner class VirtualSingleton<T>(private val factory: FactoryContext.() -> T) {
        private val instanceMap = mutableMapOf<VirtualSingletonKey, T>()
        fun getInstance(vst: VirtualSingletonManager): T {
            return instanceMap.getOrPut(vst.currentKey) {
                factoryContext.factory()
            }
        }

        operator fun getValue(vst: VirtualSingletonManager, property: KProperty<*>): T {
            return getInstance(vst)
        }

        fun getInstanceFromKey(virtualSingletonKey: VirtualSingletonKey): T {
            return instanceMap.getOrPut(virtualSingletonKey) {
                factoryContext.factory()
            }
        }
    }
    inner class LazyVirtualSingleton<T>{
        private val instanceMap = mutableMapOf<VirtualSingletonKey, T>()
        operator fun setValue(vst: VirtualSingletonManager,property: KProperty<*>, value:T) {
            instanceMap[vst.key] = value
        }

        fun getInstance(vst: VirtualSingletonManager): T {
            return instanceMap[vst.currentKey]?:TODO("Not Initialized")
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
            singletons[T::class.qualifiedName!!] = it
        }
    }

    internal fun switch(key: VirtualSingletonKey) {
        val existsKey = keys.find { it == key }
        if (existsKey != null) {
            this.currentKey = existsKey
        } else TODO("IllegalState")
    }

    internal fun create(key: VirtualSingletonKey, switch: Boolean = true) {
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




