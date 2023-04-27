package me.naotiki.ese.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.naotiki.ese.core.EseSystem.ClientImpl
import me.naotiki.ese.core.EseSystem.FileTree
import me.naotiki.ese.core.EseSystem.UserManager
import me.naotiki.ese.core.Shell.Expression
import me.naotiki.ese.core.Shell.FileSystem
import me.naotiki.ese.core.commands.parser.CommandResult
import me.naotiki.ese.core.user.Group
import me.naotiki.ese.core.user.User
import me.naotiki.ese.core.utils.splitArgs
import me.naotiki.ese.core.vfs.dsl.dir
import me.naotiki.ese.core.vfs.dsl.fileDSL
import me.naotiki.ese.core.vfs.dsl.textFile

const val appName = "Ese"

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

/**
 * Ese Coreが初期化されたかどうか
 * */
var eseInitialized = false
    private set

suspend fun initialize(clientImpl: ClientImpl, initMessage: String = "") {
    check(!eseInitialized)
    ClientImpl = clientImpl
    var userName: String
    EseSystem.IO.printChannel.println(
        """
        |Easy Shell Environment ver.$appVersion - ${clientImpl.getClientName()}
        |$initMessage
        """.trimMargin()
    )
    //名前設定
    while (true) {
        userName = EseSystem.IO.newPromptAsync(clientImpl, "あなたの名前は？:", "")
        when {
            userName.isBlank() -> {
                EseSystem.IO.printChannel.println("空白は使用できません")
            }
            !userName.matches(Regex("[0-9A-z]+")) -> {
                EseSystem.IO.printChannel.println("使用できる文字は0~9の数字とアルファベットと一部記号です")
            }

            UserManager.userList.any { it.name == userName } -> {
                EseSystem.IO.printChannel.println("既にあるユーザー名です")
            }
            else -> break
        }
    }

    val newUser = User(UserManager, userName, Group(UserManager, userName))
    newUser.setHomeDir { user, group ->
        fileDSL(FileTree.home, UserManager.uRoot) {
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
    FileSystem.setCurrentPath(newUser.dir!!)
    UserManager.setUser(newUser)

    eseInitialized = true
    while (true/*TODO 終了機能*/) {
        val input = EseSystem.IO.newPromptAsync(clientImpl, "${UserManager.user.name}:${FileSystem.currentPath.value}>")
            .ifBlank {
                null
            } ?: continue
        val inputArgs = input.splitArgs()
        val cmd = Expression.tryResolve(inputArgs.first())
        Expression.addHistory(input)
        if (cmd != null) {
            //非同期実行
            withContext(Dispatchers.Default) {
                Expression.currentJob = launch {
                    val result = cmd.execute(UserManager.user, inputArgs.drop(1))
                    if (result is CommandResult.Success) {
                        //io.outputStream.println("[DEBUG] RETURN:${result.value}")
                    }
                }
                //待機
                Expression.currentJob?.join()
                Expression.currentJob = null
            }
        } else {
            if (!Expression.expressionParser(input)) {
                EseSystem.IO.printChannel.println(
                    """
            そのようなコマンドはありません。
            help と入力するとヒントが得られるかも・・・？
            """.trimIndent()
                )
            }
        }

    }


}


