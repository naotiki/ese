package core.commands.parser

import core.ConsoleInterface
import core.IO
import core.commands.Expression
import core.user.User
import core.user.UserManager
import core.vfs.*
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.cancellation.CancellationException

//TODO サブコマンド
abstract class SubCommand<R>(val name: String, val regex: Regex? = null) {
    val args = mutableListOf<Arg<*>>()
    val opts = mutableListOf<Opt<*>>()
    fun <T : Any> option(
        type: ArgType<T>, name: String, shortName: String? = null, description: String? = null
    ): Opt<T> {
        val o = Opt(type, name, shortName, description)
        opts.add(o)
        return o
    }

    fun <T : Any> argument(
        type: ArgType<T>, name: String, description: String? = null
    ): Arg<T> {
        val a = Arg(type, name, description)
        args.add(a)
        return a
    }

    abstract suspend fun execute(args: List<String>): R
}

/**
 * すべてのコマンドの基底クラス
 * Ese Linux 内のコマンドはこのクラスを継承し、引数は[argument]や[option]で定義する必要があります。
 * [help]オプションはデフォルトで自動生成されています。
 * helpの挙動を変更するには[outputHelp]をオーバーライドしてください。
 *
 *
 * @param name コマンドの名前、[Expression.tryResolve]などで使用されます
 * @param description コマンドの説明、ヘルプで使用されます。(オプション)
 * @param R [execute]戻り値の型、基本は[Unit]
 * */
abstract class Executable<R>(val name: String, val description: String? = null) : KoinComponent {
    val um by inject<UserManager>()
    val io by inject<IO>()
    internal val argParser: SuperArgsParser = SuperArgsParser()
    val help by option(ArgType.Boolean, "help", "h", "ヘルプを表示します。").default(false)
    //fun isHelp(args: List<String>)=args.contains("-h")||args.contains("--help")

    //TODO いつかやる
    private var subCommands: List<SubCommand<*>> = this::class.nestedClasses.map {
        it.java.getDeclaredConstructor().newInstance()
    }.filterIsInstance<SubCommand<*>>()

    init {
        println(subCommands)
    }

    /**
     * @param type 引数の型
     * @param name 指定するときの名前 呼び出すときにプレフィックス"--"を付加する必要があります
     * @param shortName 指定するときの短い名前 一文字である必要があり、呼び出すときにプレフィックス"-"を付加する必要があります(オプション)
     * @param description helpで使用される説明(オプション)
     * */
    fun <T : Any> option(
        type: ArgType<T>, name: String, shortName: String? = null, description: String? = null
    ): Opt<T> {
        val o = Opt(type, name, shortName, description)
        argParser.opts.add(o)
        return o
    }

    /**
     * @param type 引数の型
     * @param name helpで表示される名前
     * @param description helpで使用される説明(オプション)
     * @param includeOption 引数にオプションを含めるかどうか
     * */
    fun <T : Any> argument(
        type: ArgType<T>, name: String, description: String? = null
    ): Arg<T> {
        val a = Arg(type, name, description)
        argParser.args.add(a)
        return a
    }

    val out get() = io.outputStream
    val reader get() = io.reader
    val console by inject<ConsoleInterface>()

    /**
     * For Development
     * 解析情報出力用
     * */
    internal fun verbose(args: List<String>) {
        try {
            println(args)
            /*if (isHelp(args)) {
                out.println("helpオプションと併用できません")
                return
            }*/
            argParser.parse(this, args)

            out.println("引数")
            argParser.args.forEach {
                out.println("${it.name}/${it.type.javaClass.simpleName}:${it.vararg?.value ?: it.value}")
            }
            out.println("オプション")
            argParser.opts.forEach {
                out.println("${it.name}/${it.type.javaClass.simpleName}:${it.multiple?.value ?: it.value}")
            }
        } catch (e: CommandIllegalArgsException) {
            println(e.localizedMessage)
            out.println(e.localizedMessage)
        } catch (e: CommandParserException) {
            println(e.localizedMessage)
            out.println(e.localizedMessage)
        } catch (e: Exception) {
            println(e.localizedMessage)
            out.println(e.localizedMessage)
        } finally {

        }
    }

    fun generateHelpText() =
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

    /**
     * ヘルプを出力します。
     * */
    open fun outputHelp(): CommandResult.Nothing<R> {
        out.println(
            generateHelpText()
        )
        return CommandResult.Nothing()
    }

    /**
     * 引数を解析して[execute]を実行します
     * @param args 解析前の引数
     * @return 結果
     * @throws CommandIllegalArgsException 型変換に失敗したとき
     * @throws CommandParserException 引数の形式が定義と異なるとき
     * */
    @Throws(CommandIllegalArgsException::class, CommandParserException::class)
    suspend fun resolve(user:User,args: List<String>): CommandResult<R> {

        return try {
            //if (isHelp(args)) return outputHelp()
            try {
                argParser.parse(this, args)
            }catch (e:CancellationException){

            } catch (e: Exception) {
                if (help) {
                    return outputHelp()
                } else {
                    throw e
                }
            }
            if (help) {
                return outputHelp()
            }
            val r = execute(user,args)
            CommandResult.Success(r)

        } catch (e: CommandIllegalArgsException) {
            println(e.localizedMessage)
            out.println(e.localizedMessage)
            CommandResult.Error()
        } catch (e: CommandParserException) {
            println(e.localizedMessage)
            out.println(e.localizedMessage)
            CommandResult.Error()
        } catch (e: Exception) {
            e.printStackTrace()
            e.printStackTrace(out)
            CommandResult.Error()
        }
    }

    /**
     * 実際に実行される関数
     * @param rawArgs 生の引数
     * */
    protected abstract suspend fun execute(user: User, rawArgs: List<String>): R
}

sealed class ArgType<T : Any>(val converter: Koin.(kotlin.String) -> T?) {

    //Primitive Types
    object Int : ArgType<kotlin.Int>({ it.toIntOrNull() })
    object String : ArgType<kotlin.String>({ it })
    object Boolean : ArgType<kotlin.Boolean>({ it.toBooleanStrictOrNull() })

    //Special Types
    object File : ArgType<core.vfs.File>({
        get<FileSystem>().tryResolve(Path(it))
    })

    object Dir : ArgType<Directory>({
        get<FileSystem>().tryResolve(Path(it))?.toDirectoryOrNull()
    })

    object Executable : ArgType<ExecutableFile<*>>({ get<Expression>().tryResolve(it) })

    class Define<T : Any>(converter: Koin.(kotlin.String) -> T?) : ArgType<T>(converter)
}


sealed class CommandResult<T> {
    class Nothing<T> : CommandResult<T>()
    class Success<T>(val value: T) : CommandResult<T>()
    class Error<T> : CommandResult<T>()
}
