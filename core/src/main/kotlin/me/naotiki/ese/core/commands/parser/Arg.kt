/*
* Respect https://github.com/Kotlin/kotlinx-cli
*/
package me.naotiki.ese.core.commands.parser

import org.koin.core.Koin
import kotlin.reflect.KProperty


class VarArg<T : Any>(
    val type: ArgType<T>, val name: String, val includeOptionInArg: Boolean, val koin: Koin
) {
    var value: MutableList<T> = mutableListOf()
    operator fun getValue(thisRef: Any?, property: KProperty<*>): List<T> {
        return value
    }

    var validator: ((T) -> Boolean)? = null
    fun validation(validator: (T) -> Boolean): VarArg<T> {
        this.validator = validator
        return this
    }

    fun addValue(str: String) {
        val casted = type.converter(koin, str) ?: throw CommandIllegalArgsException("$name が無効な値です。", type)
        if (validator?.invoke(casted) != false) {
            value += casted
        } else {
            TODO("Fire!!!!!")
        }

    }
}

class Arg<T : Any>(
    val type: ArgType<T>,
    override val name: String,
    override val description: String? = null,

    ) : CommandElement<T> {
    override var value: T? = null

    var optional = false
    fun optional(): GetWrapper<T?> {
        optional = true
        return object : GetWrapper<T?> {
            override operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
                return value
            }
        }
    }

    var validator: ((T) -> Boolean)? = null
    fun validation(validator: (T) -> Boolean): Arg<T> {
        this.validator = validator
        return this
    }

    var vararg: VarArg<T>? = null
    fun vararg(includeOption: Boolean = false): VarArg<T> {
        vararg = VarArg(type, name, includeOption, getKoin())
        return vararg as VarArg<T>
    }

    override fun updateValue(str: String) {
        val casted = type.converter(getKoin(), str) ?: throw CommandIllegalArgsException("$name が無効な値です。", type)
        if (validator?.invoke(casted) != false) {
            value = casted
        } else {
            TODO("Fire!!!!!")
        }
    }

    override fun reset() {
        super.reset()
        vararg?.value?.clear()
    }

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value!!
    }

    override fun hasValue(): Boolean {
        return value != null || (!vararg?.value.isNullOrEmpty())
    }
}

@Deprecated("dame~", level = DeprecationLevel.WARNING)
class CommandIllegalArgsException(message: String?, val type: ArgType<*>) : Exception(message)

