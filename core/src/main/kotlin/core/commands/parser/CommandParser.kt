package core.commands.parser

import core.Vfs
import core.commands.CommandManager
import core.vfs.Directory
import core.vfs.Path
import kotlin.reflect.KProperty


class CommandParserException(s: String) : Exception("コマンド解析エラー:$s")
class ARGSSUS {
    val args = mutableListOf<Arg<*>>()
    val opts = mutableListOf<Opt<*>>()

    //解析
    @Throws(CommandIllegalArgsException::class)
    fun parse(argList: List<String>) {
        val q = args.sortedWith { o1: Arg<*>, o2: Arg<*> ->
            if (o1.vararg) {
                1
            } else if (o2.vararg) {
                -1
            } else 0
        }.iterator()
        var inOption: Opt<*>? = null
        var target: Arg<*>? = null
        argList.forEachIndexed { index, s ->
            when {

                //オプション
                s.startsWith("-") -> {
                    val name = s.trimStart('-')
                    val o = opts.filter {
                        if (s.startsWith("--")) {
                            it.name == name
                        } else {
                            // ls -lhaなどのBooleanの複数羅列対応
                            (it.type is ArgType.Boolean && it.shortName?.let { it in name } == true)
                                    || it.shortName == name
                        }
                    }
                    if (o.isEmpty()) {
                        throw CommandParserException("オプション解析エラー:不明な名前")
                    }
                    o.forEach {
                        if (it.type is ArgType.Boolean) {
                            it.strValue = "true"
                        } else inOption = it

                    }
                }

                inOption != null -> {
                    if (inOption!!.multiple) {
                        inOption!!.strValue += " $s"
                    } else inOption!!.strValue = s
                    inOption = null
                }

                else -> {
                    if (target?.vararg != true) target = q.next()
                    if (target!!.vararg) {
                        target?.strValue += " $s"
                    } else {
                        target!!.strValue = s
                    }
                }
            }
        }
    }


}

class Opt<T : Any>(
    val type: ArgType<T>,
    val name: String,
    //一文字
    val shortName: String? = null,
    val description: String? = null
) {
    init {
        if (name.isBlank() || shortName?.isBlank() == true || shortName?.length != 1) {
            throw IllegalArgumentException("コマンド定義エラー")
        }
    }

    var strValue: String = ""

    var required = false
    fun required(): Opt<T> {
        required = true
        return this
    }

    var multiple = false
    fun multiple(): Opt<List<T?>> {
        multiple = true
        return Opt(
            varArgType(type), name, description
        )
    }

    /**
     * 委譲ってコト・・！？
     */
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return type.translator(strValue)
    }


}


open class Arg<T : Any>(val type: ArgType<T>, val name: String, val description: String? = null) {
    var strValue: String = ""

    var optional = false
    var vararg = false
    fun vararg(): Arg<List<T?>> {
        vararg = true
        return Arg(
            varArgType(type), name, description
        )
    }

    fun optional(): Arg<T> {
        optional = true
        return this
    }

    open operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return type.translator(strValue)
    }
}


abstract class Command<R>(val name: String, val description: String = "") {

    val argParser: ARGSSUS = ARGSSUS()


    fun <T : Any> option(
        type: ArgType<T>,
        name: String,
        shortName: String? = null,
        description: String? = null
    ): Opt<T> {
        val o = Opt(type, name, shortName, description)
        argParser.opts.add(o)
        return o
    }

    fun <T : Any> argument(
        type: ArgType<T>,
        name: String,
        description: String? = null
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

@Deprecated("非推奨", level = DeprecationLevel.WARNING,
    replaceWith = ReplaceWith("Args(this)", "core.commands.parser.Args")
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

    class Define<T : Any>(translator: (kotlin.String) -> T?) :
        ArgType<T>(translator)
}

private fun <T : Any> varArgType(type: ArgType<T>) = ArgType.Define {
    it.split(" ").map {
        type.translator(it)
    }
}