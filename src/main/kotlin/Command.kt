abstract class Command(val name: String) {
    abstract fun execute( args:List<String>)
}

object ListFile : Command("ls") {
    override fun execute(args:List<String>) {
        LocationManager.currentDirectory.children.keys.forEach {
            println(it)
        }
    }
}
//ねこ
object Cat: Command("cat"){
    override fun execute(args: List<String>) {
        val txt=LocationManager.tryResolve(Path(args.first())) as? TextFile
        if (txt != null) {
            println(txt.content)
        } else println("無効なファイル")

    }
}

object Exit : Command("exit") {
    override fun execute(args:List<String>) {
        println("終了します")
        throw Exception("ばいばい")
    }
}



object CommandManager {
    private val commandList = mutableMapOf<String, Command>()
    fun initialize(vararg cmd: Command) {
        commandList.clear()
        commandList.putAll(cmd.associateBy { it.name })
    }

    fun add(cmd: Command) {
        commandList[cmd.name] = cmd
    }

    fun tryResolve(cmd: String): Command? = commandList[cmd]
}