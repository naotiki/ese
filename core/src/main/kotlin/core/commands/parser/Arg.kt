/*
* Respect https://github.com/Kotlin/kotlinx-cli
*/
package core.commands.parser

import kotlin.reflect.KProperty


class VarArg<T : Any>(
    val type: ArgType<T>,val name: String
) {
    var value: MutableList<T> = mutableListOf()
    operator fun getValue(thisRef: Any?, property: KProperty<*>): List<T> {
        return value
    }

    fun addValue(str: String) {
        type.cast(str)?.let { value.add(it) }?:throw CommandIllegalArgsException("$name が無効な数値です。",type)
    }
}

class Arg<T : Any>
    (
    val type: ArgType<T>, override val name: String, override val description: String? = null
) : SafetyString<T> {
    override var value: T? = null

    var optional = false
    fun optional(): GetWrapper<T?> {
        optional = true
        return  object :GetWrapper<T?>{
            override operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
                return value
            }
        }
    }

    var vararg: VarArg<T>? = null
    fun vararg(): VarArg<T> {
        vararg = VarArg(type,name)
        return vararg as VarArg<T>
    }

    override fun updateValue(str: String) {
        value = type.cast(str)?:throw CommandIllegalArgsException("$name が無効な数値です。",type)
    }

    override fun reset() {
        super.reset()
        vararg?.value?.clear()
    }

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value!!
    }

    override fun hasValue(): Boolean {
        return value!=null||(!vararg?.value.isNullOrEmpty())
    }
}


class CommandIllegalArgsException(message: String?,val type:ArgType<*>) : Exception(message)

@Deprecated("非推奨", level = DeprecationLevel.WARNING)
class Args(val args: List<String>) {
    private var index = 0
    @Deprecated("非推奨", level = DeprecationLevel.WARNING)
    fun <T : Any> getArg(type: ArgType<T>, default: T? = null): T? {
        return (args.getOrNull(index)?.ifBlank { null } ?: return default).let {
            index++
            type.cast(it)
        }
    }

    fun getOptions(): List<String> {
        return args.filter { it.startsWith('-') }.flatMap { it.split("") }
    }
}

@Deprecated(
    "非推奨", level = DeprecationLevel.WARNING, replaceWith = ReplaceWith("Args(this)", "core.commands.parser.Args")
)
fun List<String>.toArgs() = Args(this)

