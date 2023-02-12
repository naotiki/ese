package core

import core.commands.Expression
import core.commands.parser.CommandResult
import core.user.Group
import core.user.User
import core.user.UserManager
import core.vfs.FileSystem
import core.vfs.FileTree
import core.vfs.dsl.dir
import core.vfs.dsl.file
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintStream


const val version = "0.0.0-dev"
val m = module {
    single { Variable() }
}

fun prepareKoinInjection(): KoinApplication {

    val io = IO()
    val userManager = UserManager()

    m.apply {
        single { userManager }
        single { FileTree(get()) }
        single { io }
    }
    return startKoin {
        printLogger(Level.DEBUG)
        modules(m)
    }
}

suspend fun initialize(koin: Koin, consoleInterface: ConsoleInterface) {

    val io = koin.get<IO>()
    val userManager = koin.get<UserManager>()
    val fileTree = koin.get<FileTree>()
    var userName: String
    io.outputStream.println(
        """
        Ese Linux ver.$version
        """.trimIndent()
    )

    //名前設定
    while (true) {
        userName = io.newPrompt(consoleInterface, "あなたの名前は？:", "ktln")
        println(userManager.userList)
        when {
            userName.isBlank() -> {
                io.outputStream.println("空白は使用できません")
            }

            !userName.matches(Regex("[0-9A-z]+")) -> {
                io.outputStream.println("使用できる文字は0~9の数字とアルファベットと一部記号です")
            }

            userManager.userList.any { it.name == userName } -> {
                io.outputStream.println("既にあるユーザー名です")
            }

            else -> break
        }
    }

    val newUser = User(userManager,userName, Group(userManager,userName))
    newUser.setHomeDir { user, group ->
        fileTree.home.dir(user.name, user, group) {
            file(
                "Readme.txt",
                """
            やぁみんな俺だ！
            このファイルを開いてくれたんだな！
            """.trimIndent()
            )
        }
    }
    val fileSystem = FileSystem(newUser.dir!!)
    userManager.setUser(newUser)
    val expression = Expression()

    loadKoinModules(module {
        single { consoleInterface }
        single { fileSystem }
        single { expression }
    })
    while (true/*TODO 終了機能*/) {
        val input = io.newPrompt(consoleInterface, "${userManager.user.name}:${fileSystem.currentPath.value}>")
            .ifBlank {
                null
            } ?: continue
        val inputArgs = input.split(' ')
        val cmd = expression.tryResolve(inputArgs.first())
        expression._commandHistory.add(0, input)
        if (cmd != null) {
            //非同期実行
            withContext(Dispatchers.Default) {
                expression.currentJob = launch {
                    val result = cmd.resolve(inputArgs.drop(1))
                    if (result is CommandResult.Success) {
                        io.outputStream.println("[DEBUG] RETURN:${result.value}")
                    }
                }
                //待機
                expression.currentJob?.join()
                expression.currentJob = null
            }
        } else {
            if (!expression.expressionParser(input)) {
                io.outputStream.println(
                    """
            そのようなコマンドはありません。
            help と入力するとヒントが得られるかも・・・？
            """.trimIndent()
                )
            }
        }

    }


}

class IO {
    private val inputStream = PipedInputStream()
    val reader = inputStream.bufferedReader()
    internal val outputStream = PrintStream(PipedOutputStream(inputStream), true)

    private val consoleInput = PipedInputStream()
    internal val consoleReader = consoleInput.bufferedReader()
    val consoleWriter = PrintStream(PipedOutputStream(consoleInput), true)
    suspend fun newPrompt(consoleInterface: ConsoleInterface, promptText: String, value: String = ""): String =
        withContext(
            Dispatchers
                .IO
        ) {
            consoleInterface.prompt(promptText, value)
            return@withContext consoleReader.readLine()
        }
}


val environments = mutableListOf<Environment>()


data class Environment(
    val io: IO,
    val fileSystem: FileSystem,
    val userManager: UserManager,
    val expression: Expression,
    val variable: Variable,
    val commandExecuteHistory: MutableList<String> = mutableListOf(),
    val consoleInterface: ConsoleInterface,
    private var currentJob: Job? = null,
) : KoinComponent {
    init {
        environments.add(this)
    }


    /**キャンセルされた場合は true
     * Jobがnullの場合はfalse
     * */
    fun cancelCommand(): Boolean {
        currentJob?.cancel() ?: return false
        io.outputStream.println("Ctrl+Cによってキャンセルされました")
        return true
    }
}


class Variable {
    val nameRule = Regex("[A-z]+")
    val map = mutableMapOf<String, String>()

    fun expandVariable(string: String): String {
        println(string)
        return Regex("\\$$nameRule").replace(string) {
            map.getOrDefault(it.value.trimStart('$'), "")
        }
    }
}

