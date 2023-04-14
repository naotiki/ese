package me.naotiki.ese.core.commands.parser


import me.naotiki.ese.core.EseSystem.ClientImpl
import me.naotiki.ese.core.EseSystem.IO
import me.naotiki.ese.core.EseError
import me.naotiki.ese.core.commands.dev.CommandDefineException
import me.naotiki.ese.core.user.User
import org.koin.core.component.KoinComponent
import kotlin.coroutines.cancellation.CancellationException

abstract class CommandDefine<R>(val name: String, val description: String? = null) : KoinComponent {
    val args = mutableListOf<Arg<*>>()
    val opts = mutableListOf<Opt<*>>()


    abstract inner class SubCommandDefine<R>(name: String, description: String?) {
        val args = mutableListOf<Arg<*>>()
        val opts = mutableListOf<Opt<*>>()
    }
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
abstract class Executable<R>(val name: String, val description: String? = null)  {
    internal val argParser: SuperArgsParser = SuperArgsParser()
    val help by option(ArgType.Boolean, "help", "h", "ヘルプを表示します。").default(false)

    //サブコマンド
    open val subCommands: List<SubCommand<*>> = emptyList()

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
        if (subCommands.isNotEmpty())
            throw CommandDefineException("Executable having SubCommand can't have Args.")
        val a = Arg(type, name, description)
        argParser.args.add(a)
        return a
    }

    val out get() = IO.printChannel
    val reader get() = IO.readChannel
    val client by lazy { ClientImpl }

    /**
     * For Development
     * 解析情報出力用
     * */
    internal fun verbose(args: List<String>) {
        kotlin.runCatching {
            argParser.parse(this, args)

            out.tryPrintln("引数")
            argParser.args.forEach {
                out.tryPrintln("${it.name} / ${it.type::class.simpleName}:${it.vararg?.value ?: it.value}")
            }
            out.tryPrintln("オプション")
            argParser.opts.forEach {
                out.tryPrintln("${it.name} / ${it.type::class.simpleName}:${it.isMultiple?.value ?: it.value}")
            }
        }.onFailure {
            out.tryPrintln(it.message)
        }
    }

    fun generateHelpText() =
        buildString {
            appendLine("$name コマンドヘルプ")
            appendLine("構文")
            appendLine(
                "$name ${argParser.opts.joinToString(" ") {
                    if (it.shortName!=null) {
                        "[-${it.shortName}|--${it.name}]"
                    }else "[--${it.name}]"
                     
                }} " +
                        argParser.args.joinToString(" ") { it.name + if (it.vararg != null) "..." else "" }
            )
            appendLine("説明：$description")
            appendLine("引数")
            argParser.args.forEach {
                appendLine("${it.name} / ${it.type::class.simpleName}")
                appendLine(it.description)
            }
            appendLine("オプション")
            argParser.opts.forEach {
                appendLine("${it.name} / ${it.type::class.simpleName}")
                appendLine(it.description)
            }
        }

    /**
     * ヘルプを出力します。
     * */
    open fun outputHelp(): CommandResult.Nothing<R> {
        out.tryPrintln(
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
    suspend fun resolve(user: User, args: List<String>): CommandResult<out Any?> = kotlin.runCatching {
        //if (isHelp(args)) return outputHelp()
        try {
            val subcommand = argParser.parse(this, args)
            if (subcommand != null)
                return subcommand.first.resolve(user, subcommand.second, args)
        } catch (_: CancellationException) {
            //Ctrl+Cを検知してしまうので握りつぶす
        } catch (e: Exception) {
            if (help) return outputHelp()
            throw e
        }
        if (help) {
            return outputHelp()
        }
        return@runCatching execute(user, args)
    }.fold(
        onSuccess = {
            CommandResult.Success(it)
        },
        onFailure = {
            if (it is EseError) {
                out.println(it.errorName)
            } else {
                out.println(it.stackTraceToString())


            }
            CommandResult.Error()
        }
    )

    /**
     * 実際に実行される関数
     * @param rawArgs 生の引数
     * */
    protected abstract suspend fun execute(user: User, rawArgs: List<String>): R

    //TODO サブコマンド
    abstract inner class SubCommand<R>(val name: String, val description: String? = null) {
        private val argParser: SuperArgsParser = SuperArgsParser()
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

        abstract suspend fun execute(user: User, rawArgs: List<String>): R

        /**
         * 引数を解析して[execute]を実行します
         * @param args 解析前の引数
         * @return 結果
         * @throws CommandIllegalArgsException 型変換に失敗したとき
         * @throws CommandParserException 引数の形式が定義と異なるとき
         * */
        @Throws(CommandIllegalArgsException::class, CommandParserException::class)
        suspend fun resolve(user: User, args: List<String>, rawArgs: List<String>): CommandResult<out Any?> =
            kotlin.runCatching {
                try {
                    argParser.parse(this@Executable, args, this)
                } catch (_: CancellationException) {

                } catch (e: Exception) {
                    if (help) {
                        //return outputHelp()
                    } else {
                        throw e
                    }
                }
                if (help) {
                    // return outputHelp()
                }

                execute(user, rawArgs)
            }.fold(
                onSuccess = {
                    CommandResult.Success(it)
                },
                onFailure = {
                    if (it is EseError) {
                        out.println(it.errorName)
                    } else {
                        out.tryPrintln(it.stackTraceToString())

                    }
                    CommandResult.Error()
                }
            )
    }
}


sealed interface CommandResult<T> {
    class Nothing<T> : CommandResult<T>
    class Success<T>(val value: T) : CommandResult<T>
    class Error<T> : CommandResult<T>

}
