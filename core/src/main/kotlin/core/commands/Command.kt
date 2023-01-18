package core.commands

import core.*
import kotlinx.coroutines.delay
import java.io.BufferedReader
import java.io.PrintStream

abstract class Command<R>(val name: String, val description: String = "") {
    val out get() = CommandManager.out!!
    val reader get() = CommandManager.reader!!
    val console get() = CommandManager.consoleImpl!!

    fun <T> T?.expect(message: String): T? {
        if (this == null) {
            out.println(message)
        }
        return this
    }

    abstract suspend fun execute(args: List<String>): R
}

class Args(val args: List<String>) {
    var index = 0
    fun <T : Any> getArg(type: ArgType<T>, default: T? = null): T? {
        return (args.getOrNull(index)?.ifBlank { null } ?: return default).let {
            index++
            type.translator(it)
        }
    }
    /*class ArgValue<T : Any>(val type:ArgType<T>,val target:String){
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T {

        }
    }*/
}

//Respect kotlinx.cli
sealed class ArgType<T : Any>(val translator: (kotlin.String) -> T?) {
    object Int : ArgType<kotlin.Int>(kotlin.String::toIntOrNull)
    object String : ArgType<kotlin.String>({ it })
    object Boolean : ArgType<kotlin.Boolean>(kotlin.String::toBooleanStrictOrNull)


    object File : ArgType<core.File>({
        LocationManager.tryResolve(Path(it))
    })

    object Dir : ArgType<Directory>({
        LocationManager.tryResolve(Path(it))?.toDirectoryOrNull()
    })

    class Define<T : Any>(translator: (kotlin.String) -> T?) : ArgType<T>(translator)
}

object ListFile : Command<Unit>(
    "ls", """
    今いる場所のファイルを一覧表示します
""".trimIndent()
) {
    override suspend fun execute(args: List<String>) {
        val a = Args(args)
        val b = a.getArg(ArgType.Dir, LocationManager.currentDirectory) ?: let {
            out.println("引数の形式が正しくありません。")
            null
        } ?: return
        //LocationManager.currentDirectory
        b.children.forEach { (fileName, _) ->
            out.println(fileName)
        }
    }
}

object Remove : Command<Unit>(
    "rm", """
    今いる場所のファイルを一覧表示します
""".trimIndent()
) {
    override suspend fun execute(args: List<String>) {
        val b = Args(args).getArg(ArgType.File, LocationManager.currentDirectory) ?: let {
            out.println("引数の形式が正しくありません。")
            null
        } ?: return
        if (b is Directory){
            if(b.children.isEmpty()){
                if(b.parent?.removeChild(b) == true){
                    out.println("${b.name}が削除されました")
                }
            }
        } else b.parent?.removeChild(b)

    }
}


object ChangeDirectory : Command<Unit>("cd") {
    override suspend fun execute(args: List<String>) {
        val dir = args.firstOrNull()?.let { LocationManager.tryResolve(Path(it)) } as? Directory
        if (dir != null) {
            LocationManager.setPath(dir)
        } else out.println("無効なディレクトリ")
    }
}

object Yes : Command<Unit>("yes") {
    override suspend fun execute(args: List<String>) {
        val a = Args(args)
        val b = a.getArg(ArgType.String, "yes") ?: return

        while (true) {
            out.println(b)
            delay(10)
        }
    }
}


//😼
object Cat : Command<Unit>("cat") {
    override suspend fun execute(args: List<String>) {
        val txt = args.firstOrNull()?.let { LocationManager.tryResolve(Path(it)) } as? TextFile
        if (txt != null) {
            out.println(txt.content)
        } else out.println("無効なファイル")
    }
}

object Echo : Command<Unit>("echo") {
    override suspend fun execute(args: List<String>) {
        args.firstOrNull()?.let { out.println(it) }
    }
}

object Clear : Command<Unit>("clear") {
    override suspend fun execute(args: List<String>) {
        console.clear()
    }
}

object SugoiUserDo : Command<Unit>("sudo") {
    override suspend fun execute(args: List<String>) {
        args.firstOrNull()?.let { CommandManager.tryResolve(it)?.execute(args.drop(1)) }
    }
}

object Exit : Command<Unit>("exit") {
    override suspend fun execute(args: List<String>) {
        out.println("終了します")
        console.exit()
    }
}


internal object CommandManager {
    private val _commandList = mutableMapOf<String, Command<*>>()
    val commandList get() = _commandList.toMap()
    var out: PrintStream? = null
    var reader: BufferedReader? = null
    var consoleImpl: ConsoleInterface? = null
    fun initialize(out: PrintStream, reader: BufferedReader, consoleImpl: ConsoleInterface, vararg cmd: Command<*>) {
        _commandList.clear()
        _commandList.putAll(cmd.associateBy { it.name })
        CommandManager.out = out
        CommandManager.reader = reader
        CommandManager.consoleImpl = consoleImpl
    }

    fun add(cmd: Command<*>) {
        _commandList[cmd.name] = cmd
    }
    fun tryResolve(cmd: String): Command<*>? = _commandList[cmd]
}