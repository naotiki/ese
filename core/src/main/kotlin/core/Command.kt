package core

import java.io.BufferedReader
import java.io.PrintStream

abstract class Command(val name: String, val description: String = "") {
    val out get() = CommandManager.out!!
    val reader get() = CommandManager.reader!!
    val console get() = CommandManager.consoleImpl!!
    abstract fun execute(args: List<String>)
}

class Args(args:List<Args>){

    sealed class ArgType<T:Any>(translator:(kotlin.String)->T?){
        object Int: ArgType<kotlin.Int>({it.toIntOrNull()})
        object String: ArgType<kotlin.String>({it})
        object Boolean: ArgType<kotlin.Boolean>({it.toBooleanStrictOrNull()})

        class Define<T : Any>(translator: (kotlin.String) -> T?): ArgType<T>(translator)
    }
}

object ListFile : Command(
    "ls", """
    今いる場所のファイルを一覧表示します
""".trimIndent()
) {
    override fun execute(args: List<String>) {
        LocationManager.currentDirectory.children.forEach { (fileName,_)->
            out.println(fileName)
        }
    }
}

object CD : Command("cd") {
    override fun execute(args: List<String>) {
        val dir = args.firstOrNull()?.let { LocationManager.tryResolve(Path(it)) } as? Directory
        if (dir != null) {
            LocationManager.setPath(dir)
        } else out.println("無効なディレクトリ")
    }
}

//😼
object Cat : Command("cat") {
    override fun execute(args: List<String>) {
        val txt = args.firstOrNull()?.let { LocationManager.tryResolve(Path(it)) } as? TextFile
        if (txt != null) {
            out.println(txt.content)
        } else out.println("無効なファイル")

    }
}


object SugoiUserDo : Command("sudo") {
    override fun execute(args: List<String>) {
        args.firstOrNull()?.let { CommandManager.tryResolve(it)?.execute(args.drop(1)) }
    }
}

class ExitException(message: String?) : Throwable(message = message)
object Exit : Command("exit") {
    override fun execute(args: List<String>) {
        out.println("終了します")
        throw ExitException("ばいばーい")
    }
}


internal object CommandManager {
    private val _commandList = mutableMapOf<String, Command>()
    val commandList get() = _commandList.toMap()
    var out: PrintStream?=null
    var reader:BufferedReader?=null
    var consoleImpl:ConsoleInterface?=null
    fun initialize(out: PrintStream, reader:BufferedReader,consoleImpl:ConsoleInterface, vararg cmd: Command) {
        _commandList.clear()
        _commandList.putAll(cmd.associateBy { it.name })
        this.out=out
        this.reader=reader
        this.consoleImpl=consoleImpl
    }

    fun add(cmd: Command) {
        _commandList[cmd.name] = cmd
    }

    fun tryResolve(cmd: String): Command? = _commandList[cmd]
}