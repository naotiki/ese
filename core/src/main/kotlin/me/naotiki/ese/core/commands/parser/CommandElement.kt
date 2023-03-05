package me.naotiki.ese.core.commands.parser

import org.koin.core.component.KoinComponent
import kotlin.reflect.KProperty

interface CommandElement<T : Any> :KoinComponent{
    val name: String
    val description: String?
    var value: T?
    fun reset(){
        value=null
    }
    fun updateValue(str: String)
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T?
    fun hasValue():Boolean
}