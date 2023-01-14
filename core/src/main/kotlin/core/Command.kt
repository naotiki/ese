package core

abstract class Command(val name: String, val description: String = "") {

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
            println(fileName)
        }
    }
}

object CD : Command("cd") {
    override fun execute(args: List<String>) {
        val dir = args.firstOrNull()?.let { LocationManager.tryResolve(Path(it)) } as? Directory
        if (dir != null) {
            LocationManager.setPath(dir)
        } else println("無効なディレクトリ")
    }
}

//😼
object Cat : Command("cat") {
    override fun execute(args: List<String>) {
        val txt = args.firstOrNull()?.let { LocationManager.tryResolve(Path(it)) } as? TextFile
        if (txt != null) {
            println(txt.content)
        } else println("無効なファイル")

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
        println("終了します")
        throw ExitException("ばいばーい")
    }
}


object CommandManager {
    private val _commandList = mutableMapOf<String, Command>()
    val commandList get() = _commandList.toMap()
    fun initialize(vararg cmd: Command) {
        _commandList.clear()
        _commandList.putAll(cmd.associateBy { it.name })
    }

    fun add(cmd: Command) {
        _commandList[cmd.name] = cmd
    }

    fun tryResolve(cmd: String): Command? = _commandList[cmd]
}