package core.commands

import core.Variable
import core.commands.parser.ArgType
import core.commands.parser.CommandIllegalArgsException
import core.commands.parser.Executable
import core.user.UserManager
import core.vfs.FileSystem
import core.vfs.Permission
import core.vfs.TextFile
import core.vfs.dsl.dir
import core.vfs.dsl.file
import kotlinx.coroutines.delay
import org.koin.core.component.inject

//Man
class Help : Executable<Unit>(
    "help", """
        役に立ちます。
""".trimIndent()
) {
    val ex by inject<Expression>()
    override suspend fun execute(rawArgs: List<String>) {
        val exes = ex.getExecutables().map { it.executable }
        out.println("現在、以下の${exes.count()}個のコマンドが使用可能です。")
        exes.forEach {
            out.println(it.name)
            out.println("  " + it.description)
        }

    }
}


class ListSegments : Executable<Unit>(
    "ls", """
    今いる場所のファイルを一覧表示します。
""".trimIndent()
) {
    val um by inject<UserManager>()
    val fs by inject<FileSystem>()
    val detail by option(ArgType.Boolean, "list", "l", "ディレクトリの内容を詳細表示します。").default(false)
    val all by option(ArgType.Boolean, "all", "a", "すべてのファイルを一覧表示します。").default(false)
    val directory by argument(ArgType.Dir, "target", "一覧表示するディレクトリ").optional()
    override suspend fun execute(rawArgs: List<String>) {
        (directory ?: fs.currentDirectory).getChildren(um.user, all)?.forEach { (name, dir) ->
            if (detail) {
                dir.run {
                    out.println("$permission ${owner.name} ${ownerGroup.name} ??? 1970 1 1 09:00 $name")
                }
            } else out.print("$name ")
        } ?: out.println("権限が足りません。")
        //書き込み
        out.println()
    }
}


class Remove : Executable<Unit>(
    "rm", """
    今いる場所のファイルを一覧表示します。
""".trimIndent()
) {
    val fs by inject<FileSystem>()
    val recursive by option(ArgType.Boolean, "recursive", "r", "ディレクトリを削除します。")
    val force by option(ArgType.Boolean, "force", "f", "強制的に削除します。")
    val interactive by option(ArgType.Boolean, "interactive", "i", "削除前に確認します。")

    val file by argument(ArgType.File, "target").vararg()

    override suspend fun execute(rawArgs: List<String>) {

        /* if (file is Directory) {
             if ((file as Directory).children.isEmpty()) {
                 if (file.parent?.removeChild(file) == true) {
                     out.println("${file.name}が削除されました")
                 }
             }
         } else file.parent?.removeChild(file)*/
    }
}


class ChangeDirectory : Executable<Unit>(
    "cd", """
    対象のディレクトリに移動します。
""".trimIndent()
) {
    val fs by inject<FileSystem>()
    val directory by argument(ArgType.Dir, "target", "一覧表示するディレクトリ")
    override suspend fun execute(rawArgs: List<String>) {
        val dir = directory//args.firstOrNull()?.let { Vfs.tryResolve(Path(it)) } as? Directory
        fs.setCurrentPath(dir)
    }
}

class Yes : Executable<Unit>(
    "yes", """
    YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES
""".trimIndent()
) {
    val value by argument(ArgType.String, "value", "出力する文字列").optional()
    override suspend fun execute(rawArgs: List<String>) {
        val b = value ?: "yes"

        while (true) {
            out.println(b)
            //yield()にすると ASSERT: 51.500000 != 51.750000 って出るから適度な休憩をあげましょう
            delay(10)
        }
    }
}


//😼
class Cat : Executable<Unit>(
    "cat", """
    😼😼😼😼😼😼😼😼😼😼
    対象のファイルを表示します。
""".trimIndent()
) {
    private val txt by argument(ArgType.File, "target")
    override suspend fun execute(rawArgs: List<String>) {

        if (txt is TextFile) {
            out.println((txt as TextFile).content)
        } else out.println("無効なファイル")
    }
}

class Echo : Executable<Unit>("echo", "メッセージを出力します") {
    val variable by inject<Variable>()
    val input by argument(ArgType.String, "msg", "出力するメッセージ").vararg()
    override suspend fun execute(rawArgs: List<String>) {
        input.joinToString(" ").let { out.println(variable.expandVariable(it)) }
    }
}

class Clear : Executable<Unit>("clear", "コンソールの出力を削除します") {
    override suspend fun execute(rawArgs: List<String>) {
        console.clear()
    }
}

class SugoiUserDo : Executable<Unit>(
    "sudo", """Sugoi User DO
    | すごいユーザーの権限でコマンドを実行します。""".trimMargin()
) {
    var isConfirm = false
    val userManager by inject<UserManager>()
    val cmd by argument(ArgType.Executable, "command", "実行するコマンドです")
    val targetArgs by argument(ArgType.String, "args", "commandに渡す引数です").vararg(true)
    override suspend fun execute(rawArgs: List<String>) {
        //by Linux
        if (!isConfirm) {
            out.println(
                """あなたはsudoコマンドの講習を受けたはずです。
    これは通常、以下の3点に要約されます:
    
        #1) 他人のプライバシーを尊重すること。
        #2) タイプする前に考えること。
        #3) 大いなる力には大いなる責任が伴うこと。"""
            )
        }
        val n = io.newPrompt(console, "実行しますか？(続行するにはあなたのユーザー名を入力) >>")
        if (n == userManager.user.name) {
            isConfirm = true
            val u = userManager.user
            userManager.setUser(userManager.uRoot)
            cmd.resolve(targetArgs)
            userManager.setUser(u)
        } else {
            out.println("残念、間違いなユーザー名")
        }
    }
}

class Exit : Executable<Unit>("exit", "Ese Linux を終了します。") {
    override suspend fun execute(rawArgs: List<String>) {
        out.println("終了します")
        console.exit()
    }
}

class MakeDirectory : Executable<Unit>("mkdir", "ディレクトリを作成します。") {
    val fs by inject<FileSystem>()
    val dirName by argument(ArgType.String, "name", "作成するディレクトリの名前")
    override suspend fun execute(rawArgs: List<String>) {
        fs.currentDirectory.dir(dirName)
    }
}

class Touch : Executable<Unit>("touch", "書き込み可能ファイルを作成します。") {
    val fs by inject<FileSystem>()
    val fileName by argument(ArgType.String, "name", "作成するファイルの名前")
    override suspend fun execute(rawArgs: List<String>) {
        fs.currentDirectory.file(fileName,"")
    }
}
class Chmod : Executable<Unit>("chmod", "権限を変更します。") {
    val fs by inject<FileSystem>()
    val value by argument(ArgType.String, "target", "作成するファイルの名前")
    val file by argument(ArgType.File, "target", "作成するファイルの名前")
    override suspend fun execute(rawArgs: List<String>) {
        val p=value.toIntOrNull(8)
        if (p==null||p>511){
            throw CommandIllegalArgsException("不正な権限値",ArgType.String)
        }

        file.permission=Permission(p)
    }
}


