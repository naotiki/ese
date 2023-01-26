package core.commands.parser

import core.Vfs
import core.commands.CommandManager
import core.vfs.Directory
import core.vfs.Path
import core.vfs.toDirectoryOrNull

/**
 * コマンドの基底クラス
 * @param name コマンドの名前、[CommandManager.tryResolve]などで使用されます
 * @param description コマンドの説明、ヘルプで使用されます。
 * @param R [execute]戻り値の型、基本は[Unit]
 * */
abstract class Command<R>(val name: String, val description: String = "") {

    private val argParser: SuperArgsParser = SuperArgsParser()

    val help by option(ArgType.Boolean, "help", "h", "ヘルプを表示します").default(false)


    fun <T : Any> option(
        type: ArgType<T>, name: String, shortName: String? = null, description: String? = null
    ): Opt<T> {
        val o = Opt(type, name, shortName, description)
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


    /**
     * 内部用
     * */
    internal fun verbose(args: List<String>){
        if (help) {
            out.println("helpオプションと併用できません")
            return
        }
        try {
            argParser.parse(args)
            out.println("引数")
            argParser.args.forEach {
                out.println("${it.name}/${it.type.javaClass.simpleName}:${it.vararg?.value?:it.value}")
            }
            out.println("オプション")
            argParser.opts.forEach {
                out.println("${it.name}/${it.type.javaClass.simpleName}:${it.multiple?.value?:it.value}")
            }
        } catch (e: CommandIllegalArgsException) {
            e.printStackTrace()
            e.printStackTrace(out)
        } catch (e: CommandParserException) {
            e.printStackTrace()
            e.printStackTrace(out)
        } catch (e: Exception) {
            e.printStackTrace()
            e.printStackTrace(out)
        }finally {

        }
    }

    @Throws(CommandIllegalArgsException::class, CommandParserException::class)
    suspend fun resolve(args: List<String>): CommandResult<R> {
        return try {
            argParser.parse(args)
            if (help) return CommandResult.Nothing()
            val r = execute(args)
            CommandResult.Success(r)
        } catch (e: CommandIllegalArgsException) {
            e.printStackTrace()
            e.printStackTrace(out)
            CommandResult.Error()
        } catch (e: CommandParserException) {
            e.printStackTrace()
            e.printStackTrace(out)
            CommandResult.Error()
        } catch (e: Exception) {
            e.printStackTrace()
            e.printStackTrace(out)
            CommandResult.Error()
        }finally {
            //ヘルプは別処理
            if (help) {
                out.println(
                    buildString {
                        appendLine("$name コマンドヘルプ")
                        appendLine("構文")
                        appendLine(
                            "$name ${argParser.opts.joinToString(" ") { "[-${it.shortName}|--${it.name}]" }} " +
                                    argParser.args.joinToString(" ") { it.name + if (it.vararg != null) "..." else "" }
                        )
                        appendLine("説明：$description")
                        appendLine("引数")
                        argParser.args.forEach {
                            appendLine("${it.name}/${it.type.javaClass.simpleName}")
                            appendLine(it.description)
                        }
                        appendLine("オプション")
                        argParser.opts.forEach {
                            appendLine("${it.name}/${it.type.javaClass.simpleName}")
                            appendLine(it.description)
                        }
                    }
                )
            }
        }
    }

    abstract suspend fun execute(args: List<String>): R
}

sealed class ArgType<T : Any>(val cast: (kotlin.String) -> T?) {
    //Primitive Types
    object Int : ArgType<kotlin.Int>(kotlin.String::toIntOrNull)
    object String : ArgType<kotlin.String>({ it })
    object Boolean : ArgType<kotlin.Boolean>(kotlin.String::toBooleanStrictOrNull)

    //Original Types TODO:Feature For Argument Suggestion
    object File : ArgType<core.vfs.File>({
        Vfs.tryResolve(Path(it))
    })

    object Dir : ArgType<Directory>({
        Vfs.tryResolve(Path(it))?.toDirectoryOrNull()
    })

    class Define<T : Any>(translator: (kotlin.String) -> T?) : ArgType<T>(translator)
}


sealed class CommandResult<T> {
    class Nothing<T> : CommandResult<T>()
    class Success<T>(val value: T) : CommandResult<T>()
    class Error<T> : CommandResult<T>()
}
