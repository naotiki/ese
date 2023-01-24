package core.commands.parser

import core.Vfs
import core.commands.CommandManager
import core.vfs.Directory
import core.vfs.Path

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
                            "$name ${argParser.opts.map { "[-${it.shortName}|--${it.name}]" }.joinToString(" ")} " +
                                    argParser.args.map { it.name + if (it.vararg != null) "..." else "" }.joinToString
                                        (" ")
                        )
                        appendLine("説明：$description")
                        appendLine("引数")
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

    //Unique Types TODO:Feature For Argument Suggestion
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
