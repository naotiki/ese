abstract class Command(val name: String) {
    abstract fun execute()
}

object ListFile : Command("ls") {
    override fun execute() {
        println("Wtf? Wtf!")
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

    fun resolve(cmd: String): Command? = commandList[cmd]
}