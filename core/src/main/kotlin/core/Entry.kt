package core
import java.io.*

var userName:String? = null

fun main(args: Array<String>) {
    do {
        if (userName !=null) println("使用できる文字は0~9の数字とアルファベットと一部記号です")
        print("あなたの名前は？:")
        userName = readln()

    } while (userName.isNullOrBlank() || !userName.orEmpty().matches(Regex("[0-9A-z]+")))

    CommandManager.initialize(ListFile, CD, Cat, Exit, SugoiUserDo)
    println(
        """
        Hi! $userName
        """.trimIndent()
    )
    /*while (true) {
        prompt()
    }*/
    println("Program arguments: ${args.joinToString()}")
}

val inputStream=PipedInputStream()
val reader get()=    inputStream.bufferedReader()
private val outputStream=PrintStream(PipedOutputStream(inputStream),true)


private val consoleInput=PipedInputStream()
private val consoleReader get()= consoleInput.bufferedReader()
val consoleWriter=PrintStream(PipedOutputStream(consoleInput),true)



fun initialize(consoleInterface:ConsoleInterface){

    do {
        if (userName !=null) outputStream.println("使用できる文字は0~9の数字とアルファベットと一部記号です")
        consoleInterface.prompt("あなたの名前は？:")
        userName = consoleReader.readLine()
        println("a$userName")
    } while (userName.isNullOrBlank() || !userName.orEmpty().matches(Regex("[0-9A-z]+")))
    CommandManager.initialize(ListFile, CD, Cat, Exit, SugoiUserDo)
    while (true/*TODO 終了機能*/){
        consoleInterface.prompt("$userName:${LocationManager.currentPath.value}>")
        consoleReader.readLine()
    }
}

fun prompt() {

    outputStream.println("${esc}[1;3;32m$userName${esc}[0m:${LocationManager.currentPath.value} >")

    val input = readln().ifBlank {
        null
    }?.split(' ') ?: return
    val cmd = CommandManager.tryResolve(input.first())
    if (cmd != null) {
        cmd.execute(input.drop(1))
    } else {
        println(
            """
            そのようなコマンドはありません。
            help と入力するとヒントが得られるかも・・・？
            """.trimIndent()
        )
    }
}
