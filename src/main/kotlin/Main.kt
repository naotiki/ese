var userName=""
fun main(args: Array<String>) {
    print("あなたの名前は？:")
    userName=readln().ifBlank {
        "名無しさん"
    }
    "".isEmpty()
    CommandManager.initialize(ListFile,CD,Cat,Exit)
    println("""
        Hi! $userName
        
        """.trimIndent())
    while (true) {
        prompt()
    }
    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")
}

fun prompt() {
    print("$userName:${LocationManager.getPath().value} >")
    val input = readln().takeIf {
        it.isNotBlank()
    }?.split(" ")?:return
    val cmd = CommandManager.tryResolve(input.first())
    if (cmd != null) {
        cmd.execute(input.drop(1))
    } else {
        println("""
            そのようなコマンドはありません。
            help と入力するとヒントが得られるかも・・・？""".trimIndent())
    }
}
