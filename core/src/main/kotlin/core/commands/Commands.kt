package core.commands

import core.Variable
import core.commands.parser.ArgType
import core.commands.parser.Args
import core.commands.parser.Command
import core.user.UserManager
import core.vfs.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

//Man
class help : Command<Unit>(
    "help", """
        helpを表示します
""".trimIndent()
) {
    val cmd by argument(ArgType.String, "cmd").vararg()
    override suspend fun execute(rawArgs: List<String>) {

    }
}



class ListFile : Command<Unit>(
    "ls", """
    今いる場所のファイルを一覧表示します
""".trimIndent()
) {
    val fs by inject<FileSystem>()
    val detail by option(ArgType.Boolean, "list", "l", "ディレクトリの内容を詳細表示します。").default(false)
    val all by option(ArgType.Boolean, "all", "a", "すべてのファイルを一覧表示します。").default(false)
    val directory by argument(ArgType.Dir, "target", "一覧表示するディレクトリ").optional()
    override suspend fun execute(rawArgs: List<String>) {
        (directory ?: fs.currentDirectory).children.filter { (_, f) -> !f.hidden || all }.forEach { (name, dir) ->
            if (detail) {
                dir.run {
                    out.println("$permission ${owner.name} ${ownerGroup.name} ??? 1970 1 1 09:00 $name")
                }
            } else out.print("$name ")
        }
        //書き込み
        out.println()
    }
}


class Remove : Command<Unit>(
    "rm", """
    今いる場所のファイルを一覧表示します
""".trimIndent()
) {
    val fs by inject<FileSystem>()

    override suspend fun execute(rawArgs: List<String>) {
        val b = Args(rawArgs).getArg(ArgType.File, fs.currentDirectory) ?: let {
            out.println("引数の形式が正しくありません。")
            null
        } ?: return
        if (b is Directory) {
            if (b.children.isEmpty()) {
                if (b.parent?.removeChild(b) == true) {
                    out.println("${b.name}が削除されました")
                }
            }
        } else b.parent?.removeChild(b)
    }
}


class ChangeDirectory : Command<Unit>("cd") {
    val fs by inject<FileSystem>()
    val directory by argument(ArgType.Dir, "target", "一覧表示するディレクトリ")
    override suspend fun execute(rawArgs: List<String>) {
        val dir = directory//args.firstOrNull()?.let { Vfs.tryResolve(Path(it)) } as? Directory
        fs.setCurrentPath(dir)
    }
}

class Yes : Command<Unit>("yes") {
    val value by argument(ArgType.String, "value", "出力する文字列").optional()
    override suspend fun execute(rawArgs: List<String>) {
        val b = value ?: "yes"

        while (true) {
            out.println(b)
            //Bits per sec yield()にすると ASSERT: 51.500000 != 51.750000 って出るから適度な休憩をあげましょう
            delay(10)
        }
    }
}


//😼
class Cat : Command<Unit>("cat") {
    override suspend fun execute(rawArgs: List<String>) {
        val txt = Args(rawArgs).getArg(ArgType.File)
        if (txt is TextFile) {
            out.println(txt.content)
        } else out.println("無効なファイル")
    }
}

class Echo : Command<Unit>("echo") {
    val variable by inject<Variable>()
    override suspend fun execute(rawArgs: List<String>) {
        rawArgs.joinToString(" ").let { out.println(variable.expandVariable(it)) }
    }
}

class Clear : Command<Unit>("clear") {
    override suspend fun execute(rawArgs: List<String>) {
        console.clear()
    }
}

class SugoiUserDo : Command<Unit>("sudo", "SUDO ~Sugoi User DO~ すごいユーザーの権限でコマンドを実行します") {
    val userManager by inject<UserManager>()
    val cmd by argument(ArgType.Command, "command", "実行するコマンドです")
    val targetArgs by argument(ArgType.String, "args", "commandに渡す引数です").vararg(true)
    override suspend fun execute(rawArgs: List<String>) {
        out.println(
            """あなたはテキストファイルからsudoコマンドの講習を受けたはずです。
これは通常、以下の3点に要約されます:

    #1) 他人のプライバシーを尊重すること。
    #2) タイプする前に考えること。
    #3) 大いなる力には大いなる責任が伴うこと。"""
        )
        val n = io.newPrompt(console, "実行しますか？(続行するにはあなたのユーザー名を入力) >>")
        if (n == userManager.user.name) {
            cmd.resolve(targetArgs)
        } else {
            out.println("残念、無効なユーザー名")
        }
    }
}

class Exit : Command<Unit>("exit") {
    override suspend fun execute(rawArgs: List<String>) {
        out.println("終了します")
        console.exit()
    }
}


class Expression : KoinComponent {
    private val fileTree by inject<FileTree>()
    private val variable by inject<Variable>()
    var currentJob: Job? = null

    internal val _commandHistory = mutableListOf<String>()
    val commandHistory get() = _commandHistory.toList()
    fun tryResolve(cmd: String): Command<*>? {
        fileTree.executableEnvPaths.forEach {
            it.children.entries.firstOrNull { (name, _) -> cmd == name }?.let { (_, f) ->
                if (f is ExecutableFile<*>) {
                    return f.command
                }
            }
        }
        return null
    }

    fun expressionParser(string: String): Boolean {

        val assignment = Regex("^${variable.nameRule}=")
        when {
            string.contains(assignment) -> {

                val a = string.replaceFirst(assignment, "")
                variable.map[assignment.matchAt(string, 0)!!.value.trimEnd('=')] = a
            }

            else -> return false
        }
        println(variable.map)
        return true
    }

    fun cancelJob(): Boolean {
        currentJob?.cancel() ?: return false
        return true
    }
}