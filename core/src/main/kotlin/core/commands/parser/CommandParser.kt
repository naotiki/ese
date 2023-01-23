/*
* Respect https://github.com/Kotlin/kotlinx-cli
*/
package core.commands.parser

import core.Vfs
import core.commands.CommandManager
import core.vfs.Directory
import core.vfs.Path
import kotlin.reflect.KProperty

interface SafetyString<T : Any> {
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

class MultipleOpt<T : Any>(
    val type: ArgType<T>,
) {
    var value: MutableList<T> = mutableListOf()
    operator fun getValue(thisRef: Any?, property: KProperty<*>): List<T>? {
        return value
    }

    fun addValue(str: String) {
        type.translator(str)?.let { value.add(it) }
    }
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

    override var value: T? = null
    override fun updateValue(str: String) {
        value = type.translator(str)!!
    }

    var required = false
    fun required(): Opt<T> {
        required = true
        return this
    }

    var multiple: MultipleOpt<T>? = null
    /**
     * オプションを複数渡せるようになります。
     * [required]と組み合わせる場合はこの関数を最後に呼び出してください。
     * */
    fun multiple(): MultipleOpt<T> {
        multiple = MultipleOpt(
            (type)
        )
        return multiple as MultipleOpt<T>
    }

    override fun reset() {
        super.reset()
        multiple?.value?.clear()
    }

    /**
     * 委譲ってコト・・！？
     */
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return value!!
    }

    override fun hasValue(): Boolean {
       return value!=null||(!multiple?.value.isNullOrEmpty())
    }
}


class VarArg<T : Any>(
    val type: ArgType<T>,
) {
    var value: MutableList<T> = mutableListOf()
    operator fun getValue(thisRef: Any?, property: KProperty<*>): List<T>? {
        return value
    }

    fun addValue(str: String) {
        type.translator(str)?.let { value.add(it) }
    }
}

class Arg<T : Any>
    (
    val type: ArgType<T>, override val name: String, override val description: String? = null
) : SafetyString<T> {
    override var value: T? = null

    var optional = false
    fun optional(): Arg<T> {
        optional = true
        return this
    }

    var vararg: VarArg<T>? = null
    fun vararg(): VarArg<T> {
        vararg = VarArg(type)
        return vararg as VarArg<T>
    }

    override fun updateValue(str: String) {
        value = type.translator(str)!!
    }

    override fun reset() {
        super.reset()
        vararg?.value?.clear()
    }

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return value!!
    }

    override fun hasValue(): Boolean {
        return value!=null||(!vararg?.value.isNullOrEmpty())
    }
}


abstract class Command<R>(val name: String, val description: String = "") {

    val argParser: SuperArgsParser = SuperArgsParser()


    fun <T : Any> option(
        type: ArgType<T>, name: String, shortName: String? = null, description: String? = null
    ): Opt<T> {
        val o = Opt<T>(type, name, shortName, description)
        argParser.opts.add(o)
        return o
    }

    fun <T : Any> argument(
        type: ArgType<T>, name: String, description: String? = null
    ): Arg<T> {
        val a = Arg(type, name, description)
        argParser.args.add(a)
        return a
    }


    val out get() = CommandManager.out!!
    val reader get() = CommandManager.reader!!
    val console get() = CommandManager.consoleImpl!!

    @Deprecated("ひすいしょう")
    fun <T> T?.expect(message: String): T? {
        if (this == null) {
            out.println(message)
        }
        return this
    }

    @Throws(CommandIllegalArgsException::class)
    suspend fun resolve(args: List<String>) {
        try {
            argParser.parse(args)
            execute(args)
        } catch (e: CommandIllegalArgsException) {
            throw e
        }
    }

    abstract suspend fun execute(args: List<String>): R
}

class CommandIllegalArgsException : Exception()

@Deprecated("非推奨", level = DeprecationLevel.WARNING)
class Args(val args: List<String>) {
    private var index = 0
    fun <T : Any> getArg(type: ArgType<T>, default: T? = null): T? {
        return (args.getOrNull(index)?.ifBlank { null } ?: return default).let {
            index++
            type.translator(it)
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

//Respect kotlinx.cli
sealed class ArgType<T : Any>(val translator: (kotlin.String) -> T?) {
    //Primitive Types
    object Int : ArgType<kotlin.Int>(kotlin.String::toIntOrNull)
    object String : ArgType<kotlin.String>({ it })
    object Boolean : ArgType<kotlin.Boolean>(kotlin.String::toBooleanStrictOrNull)

    object File : ArgType<core.vfs.File>({
        Vfs.tryResolve(Path(it))
    })

    object Dir : ArgType<Directory>({
        Vfs.tryResolve(Path(it))?.toDirectoryOrNull()
    })

    class Define<T : Any>(translator: (kotlin.String) -> T?) : ArgType<T>(translator)
}
