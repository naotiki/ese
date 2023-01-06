fun main(args: Array<String>) {
    CommandManager.initialize(ListFile)
    println("Hello World!")
    while (true) {
        prompt()
    }
    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")
}

fun prompt() {
    print(PathManager.getPath().value + " >")
    val input = readln()
    if (input.isBlank()) return
    val cmd = CommandManager.resolve(input)
    if (cmd != null) {
        cmd.execute()
    } else {
        println("そのようなコマンドはありません")
    }
}
