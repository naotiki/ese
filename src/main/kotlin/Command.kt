abstract class Command(val name: String, val description: String = TODO()) {
    abstract fun execute(args: List<String>)
}

object ListFile : Command(
    "ls", """
    今いる場所のファイルを一覧表示します
""".trimIndent()
) {
    override fun execute(args: List<String>) {
        LocationManager.currentDirectory.children.keys.forEach {
            println(it)
        }
    }
}

object CD : Command("cd") {
    override fun execute(args: List<String>) {
        val dir = LocationManager.tryResolve(Path(args.first())) as? Directory
        if (dir != null) {
           LocationManager.setPath(dir)
        } else println("無効なディレクトリ")

    }
}

//ねこ
object Cat : Command("cat") {
    override fun execute(args: List<String>) {
        val txt = LocationManager.tryResolve(Path(args.first())) as? TextFile
        if (txt != null) {
            println(txt.content)
        } else println("無効なファイル")

    }
}

object Exit : Command("exit") {
    override fun execute(args: List<String>) {
        println("終了します")
        throw Exception("ばいばい")
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