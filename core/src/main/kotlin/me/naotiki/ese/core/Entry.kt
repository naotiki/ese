package me.naotiki.ese.core

import me.naotiki.ese.core.commands.Expression
import me.naotiki.ese.core.commands.parser.CommandResult
import me.naotiki.ese.core.user.Group
import me.naotiki.ese.core.user.User
import me.naotiki.ese.core.user.UserManager
import me.naotiki.ese.core.utils.splitArgs
import me.naotiki.ese.core.vfs.FileSystem
import me.naotiki.ese.core.vfs.FileTree
import me.naotiki.ese.core.vfs.dsl.dir
import me.naotiki.ese.core.vfs.dsl.file
import me.naotiki.ese.core.vfs.dsl.fileDSL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
import java.io.File
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintStream

//Pluginsフォルダなど
val dataDir = File(System.getProperty("compose.application.resources.dir") ?: "client-gui/resources/common/")

const val version = "0.0.0-dev"
private val module = module {
    single { Variable() }
    single { Expression() }
    single { UserManager() }
    single { FileTree(get()) }
    single { IO() }
    single { FileSystem(get<FileTree>().root) }
}

object Program {
    var debug: Boolean = false
        internal set
}

fun programArg(args: List<String>) {
    args.forEach {
        when (it) {
            "-D" -> {
                Program.debug = true
            }
        }
    }
}

fun prepareKoinInjection(): KoinApplication = startKoin {
    printLogger(Level.INFO)
    modules(module)
    allowOverride(false)
}

private var initialized = false
suspend fun initialize(koin: Koin, clientImpl: ClientImpl) {
    if (initialized) return
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
        userName = io.newPrompt(clientImpl, "あなたの名前は？:", "a")
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

    val newUser = User(userManager, userName, Group(userManager, userName))
    newUser.setHomeDir { user, group ->
        fileDSL(fileTree.home, userManager.uRoot) {
            dir(user.name, user, group) {
                println(
                    "LOG:" + file(
                        "Readme.txt",
                        """
            TODO:なんか書く
            """.trimIndent()
                    ).parent?.name
                )
            }
        }
    }
    val fileSystem = koin.get<FileSystem>()
    fileSystem.setCurrentPath(newUser.dir!!)
    userManager.setUser(newUser)
    val expression = koin.get<Expression>()

    loadKoinModules(module {
        single { clientImpl }
        single { expression }
    })
    initialized = true
    while (true/*TODO 終了機能*/) {
        val input = io.newPrompt(clientImpl, "${userManager.user.name}:${fileSystem.currentPath.value}>")
            .ifBlank {
                null
            } ?: continue
        val inputArgs = input.splitArgs()
        val cmd = expression.tryResolve(inputArgs.first())
        expression._commandHistory.add(0, input)
        if (cmd != null) {
            //非同期実行
            withContext(Dispatchers.Default) {
                expression.currentJob = launch {
                    val result = cmd.execute(userManager.user, inputArgs.drop(1))
                    if (result is CommandResult.Success) {
                        //io.outputStream.println("[DEBUG] RETURN:${result.value}")
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

    /*private*/ val inputStream = PipedInputStream()

    val reader = inputStream.reader()
    val outputStream = PrintStream(PipedOutputStream(inputStream), true)

    private val consoleInput = PipedInputStream()
    val consoleReader = consoleInput.bufferedReader()
    val consoleWriter = PrintStream(PipedOutputStream(consoleInput), true)
    suspend fun newPrompt(clientImpl: ClientImpl, promptText: String, value: String = ""): String =
        withContext(
            Dispatchers
                .IO
        ) {
            clientImpl.prompt(promptText, value)
            return@withContext consoleReader.readLine()
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

