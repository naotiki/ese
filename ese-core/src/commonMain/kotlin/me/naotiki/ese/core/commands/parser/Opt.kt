package me.naotiki.ese.core.commands.parser

import kotlin.reflect.KProperty

class MultipleOpt<T : Any>(
    val type: ArgType<T>,
    val name: String,
    var validator:((T)->Boolean)?
) {
    var value: MutableList<T> = mutableListOf()
    operator fun getValue(thisRef: Any?, property: KProperty<*>): List<T> {
        return value
    }
    var isRequired = false
    fun required(): GetWrapper<List<T>> {
        isRequired = true
        return object :GetWrapper<List<T>>{
            override operator fun getValue(thisRef: Any?, property: KProperty<*>): List<T> {
                return value.ifEmpty { null }!!
            }
        }
    }
    fun validation(validator: (T) -> Boolean): MultipleOpt<T> {
        this.validator = validator
        return this
    }
    fun addValue(str: String) {
        val casted=type.converter(str)?:throw CommandIllegalArgsException("$name が無効な数値です。",
            type)
        if (validator?.invoke(casted) != false) {
            value += casted
        } else {
            TODO("Fire!!!!!")
        }
    }
}

interface GetWrapper<T>{
    operator fun getValue(thisRef: Any?, property: KProperty<*>):T
}

class Opt<T : Any>(
    val type: ArgType<T>, override val name: String,
    //一文字
    val shortName: String? = null, override val description: String? = null
) : CommandElement<T> {
    init {
        if (name.isBlank() || shortName?.isBlank() == true || (shortName?.length ?: 1) != 1) {
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

        val casted = type.converter(str)?:throw CommandIllegalArgsException("$name が無効な数値です。",type)
        if (validator?.invoke(casted) != false) {
            value = casted
        } else {
            TODO("Fire!!!!!")
        }
    }

    var isRequired = false
    fun required(): GetWrapper<T> {
        isRequired = true
        return   object :GetWrapper<T>{
            override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
                return value!!
            }
        }
    }
    var validator: ((T) -> Boolean)? = null
    fun validation(validator: (T) -> Boolean): Opt<T> {
        this.validator = validator
        return this
    }
    var isMultiple: MultipleOpt<T>? = null
    /**
     * オプションを複数渡せるようになります。
     * */
    fun multiple(): MultipleOpt<T> {
        isMultiple = MultipleOpt(
            type,name,validator
        )
        return isMultiple as MultipleOpt<T>
    }

    override fun reset() {
        super.reset()
        value=defaultValue
        isMultiple?.value?.clear()
    }

    /**
     * 委譲ってコト・・！？
     */
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return value
    }

    override fun hasValue(): Boolean {
       return value!=null||(!isMultiple?.value.isNullOrEmpty())
    }
}