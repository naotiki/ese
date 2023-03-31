package me.naotiki.ese.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.naotiki.ese.core.commands.Expression
import me.naotiki.ese.core.commands.parser.CommandResult
import me.naotiki.ese.core.user.Group
import me.naotiki.ese.core.user.User
import me.naotiki.ese.core.user.UserManager
import me.naotiki.ese.core.utils.splitArgs
import me.naotiki.ese.core.vfs.FileSystem
import me.naotiki.ese.core.vfs.FileTree
import me.naotiki.ese.core.vfs.dsl.dir
import me.naotiki.ese.core.vfs.dsl.fileDSL
import me.naotiki.ese.core.vfs.dsl.textFile
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module



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

fun prepareKoinInjection(level:Level=Level.INFO): KoinApplication =
    startKoin {
        printLogger(level)
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
    io.printChannel.println(
        """
        Ese Linux ver.$version - ${clientImpl.getClientName()}
        """.trimIndent()
    )

    //名前設定
    while (true) {
        userName = io.newPromptAsync(clientImpl, "あなたの名前は？:", "")

        when {
            userName.isBlank() -> {
                io.printChannel.println("空白は使用できません")
            }

            !userName.matches(Regex("[0-9A-z]+")) -> {
                io.printChannel.println("使用できる文字は0~9の数字とアルファベットと一部記号です")
            }

            userManager.userList.any { it.name == userName } -> {
                io.printChannel.println("既にあるユーザー名です")
            }

            else -> break
        }
    }

    val newUser = User(userManager, userName, Group(userManager, userName))
    newUser.setHomeDir { user, group ->
        fileDSL(fileTree.home, userManager.uRoot) {
            dir(user.name, user, group) {

                    "LOG:" + textFile(
                        "Readme.txt",
                        """
            TODO:なんか書く
            """.trimIndent()
                    ).parent?.name

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
        val input = io.newPromptAsync(clientImpl, "${userManager.user.name}:${fileSystem.currentPath.value}>")
            .ifBlank {
                null
            } ?: continue
        val inputArgs = input.splitArgs()
        val cmd = expression.tryResolve(inputArgs.first())
        expression.addHistory(input)
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
                io.printChannel.println(
                    """
            そのようなコマンドはありません。
            help と入力するとヒントが得られるかも・・・？
            """.trimIndent()
                )
            }
        }

    }


}


