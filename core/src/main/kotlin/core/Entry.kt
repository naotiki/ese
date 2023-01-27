package core

import core.commands.*
import core.commands.parser.CommandResult
import core.user.Group
import core.user.User
import core.user.VUM
import core.vfs.FireTree
import core.vfs.VFS
import core.vfs.dsl.dir
import core.vfs.dsl.file
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintStream

var userName: String? = null

val inputStream = PipedInputStream()
val reader = inputStream.bufferedReader()
private val outputStream = PrintStream(PipedOutputStream(inputStream), true)

private val consoleInput = PipedInputStream()
private val consoleReader = consoleInput.bufferedReader()
val consoleWriter = PrintStream(PipedOutputStream(consoleInput), true)


suspend fun ConsoleInterface.newPrompt(promptText: String, value: String = ""): String = withContext(Dispatchers.IO) {
    prompt(promptText, value)
    return@withContext consoleReader.readLine()
}

var job: Job? = null
const val version = "0.0.0-dev"
suspend fun initialize(consoleInterface: ConsoleInterface ) {
    outputStream.println(
        """
        Ese Linux ver.$version
        """.trimIndent()
    )

    //名前設定
    while (true) {
        userName = consoleInterface.newPrompt("あなたの名前は？:","ktln")
        println(VUM.userList)
        when {
            userName.isNullOrBlank() -> {
                outputStream.println("空白は使用できません")
            }
            !userName.orEmpty().matches(Regex("[0-9A-z]+")) -> {
                outputStream.println("使用できる文字は0~9の数字とアルファベットと一部記号です")
            }
            VUM.userList.any { it.name == userName } -> {
                outputStream.println("既にあるユーザー名です")
            }
            else -> break
        }
    }

    val newUser = User(userName!!, Group(userName!!))
    newUser.setHomeDir { user, group ->
        FireTree.home.dir(user.name, user, group) {
            file(
                "Readme.txt",
                """
            やぁみんな俺だ！
            このファイルを開いてくれたんだな！
            """.trimIndent()
            )
        }
    }
    Vfs.setCurrentPath(newUser.homeDir!!)
    VUM.setUser(newUser)


    //コマンド初期化
    CommandManager.initialize(
        outputStream, consoleReader, consoleInterface,
        ListFile, ChangeDirectory, Cat, Exit, SugoiUserDo,
        Yes, Clear, Echo, Remove, Test, Parse
    )
    //無限ルーチン
    while (true/*TODO 終了機能*/) {
        val input = consoleInterface.newPrompt("$userName:${Vfs.currentPath.value}>").ifBlank {
            null
        } ?: continue
        val inputArgs = input.split(' ')
        val cmd = CommandManager.tryResolve(inputArgs.first())
        commandHistoryImpl.add(0, input)
        if (cmd != null) {
            //非同期実行
            withContext(Dispatchers.Default) {
                job = launch {
                    val result = cmd.resolve(inputArgs.drop(1))
                    if (result is CommandResult.Success) {
                        outputStream.println("[DEBUG] RETURN:${result.value}")
                    }
                }
                //待機
                job?.join()
                job = null
            }
        } else {
            if (!expressionParser(input)) {
                outputStream.println(
                    """
            そのようなコマンドはありません。
            help と入力するとヒントが得られるかも・・・？
            """.trimIndent()
                )
            }
        }

    }
}

var Vfs: VFS = VFS(FireTree.root)


private val commandHistoryImpl = mutableListOf<String>()
val commandHistory get() = commandHistoryImpl.toList()

object Variable {
    val nameRule = Regex("[A-z]+")
    val map = mutableMapOf<String, String>()

    fun expandVariable(string: String): String {

        println(string)
        return Regex("\\$$nameRule").replace(string) {
            map.getOrDefault(it.value.trimStart('$'), "")
        }
    }
}

/**キャンセルされた場合は true
 * Jobがnullの場合はfalse
 * */
fun cancelCommand(): Boolean {
    job?.cancel() ?: return false

    outputStream.println("Ctrl+Cによってキャンセルされました")
    return true
}