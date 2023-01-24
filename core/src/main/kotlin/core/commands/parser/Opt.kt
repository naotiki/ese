package core.commands.parser

import kotlin.reflect.KProperty

class MultipleOpt<T : Any>(
    val type: ArgType<T>,
    val name: String,
) {
    var value: MutableList<T> = mutableListOf()
    operator fun getValue(thisRef: Any?, property: KProperty<*>): List<T> {
        return value
    }
    var required = false
    fun required(): GetWrapper<List<T>> {
        required = true
        return object :GetWrapper<List<T>>{
            override operator fun getValue(thisRef: Any?, property: KProperty<*>): List<T> {
                return value.ifEmpty { null }!!
            }
        }
    }
    fun addValue(str: String) {
        type.cast(str)?.let { value.add(it) }?:throw CommandIllegalArgsException("$name が無効な数値です。",type)
    }
}
interface GetWrapper<T>{
    operator fun getValue(thisRef: Any?, property: KProperty<*>):T
}
class Opt<T : Any>(
    val type: ArgType<T>, override val name: String,
    //一文字
    val shortName: String? = null, override val description: String? = null
) : SafetyString<T> {
    init {
        if (name.isBlank() || shortName?.isBlank() == true || shortName?.length != 1) {
            throw IllegalArgumentException("コマンド定義エラー")
        }
    }

    var defaultValue:T?=null
    override var value: T? =defaultValue

    fun default(value:T): GetWrapper<T> {
        defaultValue=value
        return  object :GetWrapper<T>{
            override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
                return this@Opt.value?:defaultValue!!
            }
        }
    }
    override fun updateValue(str: String) {
        value = type.cast(str)?:throw CommandIllegalArgsException("$name が無効な数値です。",type)
    }

    var required = false
    fun required(): GetWrapper<T> {
        required = true
        return object :GetWrapper<T>{
            override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
                return value!!
            }
        }
    }

    var multiple: MultipleOpt<T>? = null
    /**
     * オプションを複数渡せるようになります。
     * */
    fun multiple(): MultipleOpt<T> {
        multiple = MultipleOpt(
            (type),name
        )
        return multiple as MultipleOpt<T>
    }

    override fun reset() {
        super.reset()
        value=defaultValue
        multiple?.value?.clear()
    }

    /**
     * 委譲ってコト・・！？
     */
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return value
    }

    override fun hasValue(): Boolean {
       return value!=null||(!multiple?.value.isNullOrEmpty())
    }
}