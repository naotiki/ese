package core.commands

import core.ConsoleInterface
import core.Variable.expandVariable
import core.Vfs
import core.commands.parser.ArgType
import core.commands.parser.Command
import core.commands.parser.toArgs
import core.vfs.Directory
import core.vfs.Path
import core.vfs.TextFile
import kotlinx.coroutines.delay
import java.io.BufferedReader
import java.io.PrintStream
import kotlin.reflect.KProperty


object ListFile : Command<Unit>(
    "ls", """
    今いる場所のファイルを一覧表示します
""".trimIndent()
) {
    val option by option(ArgType.Boolean,"","","")
    val arg1 by argument(ArgType.Dir,"","")
    override suspend fun execute(args: List<String>) {
        val b = args.toArgs().getArg(ArgType.Dir, Vfs.currentDirectory) ?: let {
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
        val b = args.toArgs().getArg(ArgType.File, Vfs.currentDirectory) ?: let {
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
        val dir = args.firstOrNull()?.let { Vfs.tryResolve(Path(it)) } as? Directory
        if (dir != null) {
            Vfs.setPath(dir)
        } else out.println("無効なディレクトリ")
    }
}

object Yes : Command<Unit>("yes") {
    override suspend fun execute(args: List<String>) {
        val b = args.toArgs().getArg(ArgType.String, "yes") ?: return

        while (true) {
            out.println(b)
            delay(10)
        }
    }
}


//😼
object Cat : Command<Unit>("cat") {
    override suspend fun execute(args: List<String>) {
        val txt = args.toArgs().getArg(ArgType.File)
        if (txt is TextFile) {
            out.println(txt.content)
        } else out.println("無効なファイル")
    }
}

object Echo : Command<Unit>("echo") {
    override suspend fun execute(args: List<String>) {
        args.joinToString(" ").let { out.println(expandVariable(it)) }
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