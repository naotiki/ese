package core.commands

import core.Variable
import core.commands.parser.ArgType
import core.commands.parser.CommandIllegalArgsException
import core.commands.parser.Executable
import core.dataDir
import core.user.User
import core.utils.normalizeYesNoAnswer
import core.vfs.*
import core.vfs.dsl.dir
import core.vfs.dsl.file
import core.vfs.dsl.fileDSL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.inject
import java.net.URL
import java.net.URLClassLoader
import java.util.jar.JarFile


//  UDON is a Downloader Of Noodles
class Udon : Executable<Unit>("udon", "UDON is a Downloader Of Noodles") {
    var agree=false
    //さぶこまんど
    inner class Install : SubCommand<Unit>("world", "世界中からインストールします。") {

        val pkgName by argument(ArgType.String, "packageName", "パッケージ名")
        override suspend fun execute(user: User, rawArgs: List<String>) {
            out.println("[DEMO]Installing $pkgName ")
            println(pkgName)
        }
    }

    inner class LocalInstall : SubCommand<Unit>("local", "ローカルファイルからインストールします。") {
        val pkgName by argument(ArgType.String, "packageName", "パッケージ名")
        override suspend fun execute(user: User, rawArgs: List<String>) {
            val pluginDir=java.io.File(dataDir,"plugins")
            if(!pluginDir.exists()) {
                out.println("pluginフォルダーが見つかりませんでした。\n${pluginDir.absolutePath}に作成してください。")
            }
            val jarFile=withContext(Dispatchers.IO) {
                JarFile(java.io.File(""))
            }
            val targetClassName=jarFile.manifest.mainAttributes.getValue("Plugin-Class")
            out.println("[DEMO]Installing $pkgName")

            val child = URLClassLoader(
                arrayOf<URL>(java.io.File("").toURI().toURL()),
                this.javaClass.classLoader
            )
            child.loadClass("targetClassName")
            child.getResource("")

           out.println( )
        }
    }

    override suspend fun execute(user: User, rawArgs: List<String>) {
        out.println("""
            Udon は EseLinuxのプラグインマネージャーです。
            """.trimIndent())
    }
    companion object{
        const val fileExtension="ndl"
    }
}

class Exec : Executable<Unit>("exec", "RUN") {
    override suspend fun execute(user: User, rawArgs: List<String>) {
        withContext(Dispatchers.IO) {
            val process = ProcessBuilder("medley.exe").start()
            launch {
                withContext(Dispatchers.IO) {
                    process.inputStream.transferTo(io.outputStream)
                }
            }
            launch {
                withContext(Dispatchers.IO) {
                    process.inputStream.transferTo(io.outputStream)
                }
            }
            println("ssss")
            process.waitFor()
        }
    }

}

//Man
class Help : Executable<Unit>(
    "help", """
        役に立ちます。
""".trimIndent()
) {
    private val ex by inject<Expression>()
    override suspend fun execute(user: User, rawArgs: List<String>) {
        val exes = ex.getExecutables(includeHidden = false).map { it }
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
    val fs by inject<FileSystem>()
    val detail by option(
        ArgType.Boolean,
        "list", "l", "ディレクトリの内容を詳細表示します。"
    ).default(false)
    val all by option(
        ArgType.Boolean,
        "all", "a", "すべてのファイルを一覧表示します。"
    ).default(false)
    private val directory by argument(ArgType.Dir, "target", "一覧表示するディレクトリ").optional()
    override suspend fun execute(user: User, rawArgs: List<String>) {
        (directory ?: fs.currentDirectory).getChildren(user, all)?.forEach { (name, file) ->
            if (detail) {
                file.run {
                    out.println(
                        (if (file is Directory) {
                            "d"
                        } else "-") + "${permission.get()} ${owner.get().name} ${ownerGroup.get().name} $name"
                    )
                }
            } else out.print("$name ")
        } ?: out.println("権限が足りません。")
        //書き込み
        out.println()
    }
}


class Remove : Executable<Unit>(
    "rm", """
    ファイルを削除します。
""".trimIndent()
) {
    val fs by inject<FileSystem>()
    val recursive by option(ArgType.Boolean, "recursive", "r", "ディレクトリを削除します。").default(false)
    val interactive by option(ArgType.Boolean, "interactive", "i", "削除前に確認します。").default(false)

    val files by argument(ArgType.File, "target").vararg()

    suspend fun interactiveRemove(user: User, file: File): Boolean {
        val text = if (file is Directory) {
            "ディレクトリ"
        } else {
            "ファイル"
        }
        val ans = io.newPrompt(console, "$text ${file.getFullPath().value}を削除しますか？ (y/N)")
        return if (normalizeYesNoAnswer(ans) == true) {
            file.parent?.removeChild(user, file) == true
        } else {
            out.println("削除しませんでした。")
            false
        }
    }

    suspend fun remove(user: User, files: List<File>) {
        files.forEach {
            if (it is Directory) {
                if (!recursive) {
                    out.println("ディレクトリを削除するには--recursiveオプションが必要です。")
                    return
                }
                val children = it.getChildren(user, true)
                if (children == null) {
                    out.println("権限不足です。")
                    return
                }

                if (children.isEmpty()) {
                    if (interactive) {
                        interactiveRemove(user, it)
                    } else it.parent?.removeChild(user, it)
                } else {
                    remove(user, children.values.toList())

                    if (it.getChildren(user, true)!!.isEmpty()) {
                        if (interactive) {
                            interactiveRemove(user, it)
                        } else it.parent?.removeChild(user, it)
                    } else {
                        out.println("ファイルが残っているため${it.getFullPath().value}を削除できませんでした。")
                    }
                }

            } else {
                if (interactive) {
                    interactiveRemove(user, it)
                } else it.parent?.removeChild(user, it)
            }
        }
    }

    override suspend fun execute(user: User, rawArgs: List<String>) {
        out.println(user.name)
        remove(user, files)
    }
}


class ChangeDirectory : Executable<Unit>(
    "cd", """
    対象のディレクトリに移動します。
""".trimIndent()
) {
    private val fs by inject<FileSystem>()
    val directory by argument(ArgType.Dir, "target", "一覧表示するディレクトリ")
    override suspend fun execute(user: User, rawArgs: List<String>) {
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
    override suspend fun execute(user: User, rawArgs: List<String>) {
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
    private val txt by argument(ArgType.File, "target", "表示するファイル")
    override suspend fun execute(user: User, rawArgs: List<String>) {

        if (txt is TextFile) {
            out.println((txt as TextFile).content.get(user))
        } else out.println("無効なファイル")
    }
}

class Echo : Executable<Unit>("echo", "メッセージを出力します。") {
    private val variable by inject<Variable>()
    private val input by argument(ArgType.String, "msg", "出力するメッセージ").vararg()
    override suspend fun execute(user: User, rawArgs: List<String>) {
        input.joinToString(" ").let { out.println(variable.expandVariable(it)) }
    }
}

class Clear : Executable<Unit>("clear", "コンソールの出力を削除します。") {
    override suspend fun execute(user: User, rawArgs: List<String>) {
        console.clear()
    }
}

class SugoiUserDo : Executable<Unit>(
    "sudo", """Sugoi User DO
        すごいユーザーの権限でコマンドを実行します。""".trimIndent()
) {
    private var isConfirm = false
    private val cmd by argument(ArgType.Executable, "command", "実行するコマンド")
    private val targetArgs by argument(ArgType.String, "args", "commandに渡す引数").vararg(true)
    override suspend fun execute(user: User, rawArgs: List<String>) {
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
        if (n == user.name) {
            isConfirm = true
            um.setUser(um.uRoot)
            cmd.execute(um.uRoot, targetArgs)
            um.setUser(user)
        } else {
            out.println("残念、間違いなユーザー名")
        }
    }
}

class Exit : Executable<Unit>("exit", "Ese Linux を終了します。") {
    override suspend fun execute(user: User, rawArgs: List<String>) {
        out.println("終了します")
        console.exit()
    }
}

class MakeDirectory : Executable<Unit>("mkdir", "ディレクトリを作成します。") {
    val fs by inject<FileSystem>()
    val dirName by argument(ArgType.String, "name", "作成するディレクトリの名前")
    override suspend fun execute(user: User, rawArgs: List<String>) {
        fileDSL(fs.currentDirectory, um.user) {
            dir(dirName)
        }

    }
}

class Touch : Executable<Unit>("touch", "書き込み可能ファイルを作成します。") {
    val fs by inject<FileSystem>()
    val fileName by argument(ArgType.String, "name", "作成するファイルの名前")
    override suspend fun execute(user: User, rawArgs: List<String>) {
        fileDSL(fs.currentDirectory, um.user) {
            file(fileName, "")
        }
    }
}

class Chmod : Executable<Unit>("chmod", "権限を変更します。") {
    val fs by inject<FileSystem>()
    val value by argument(ArgType.String, "value", "権限の値(8進数9桁)")
    val file by argument(ArgType.File, "target", "変更するファイルの名前")
    override suspend fun execute(user: User, rawArgs: List<String>) {
        val p = value.toIntOrNull(8)
        if (p == null || p > 511) {
            throw CommandIllegalArgsException("不正な権限値", ArgType.String)
        }

        file.permission.set(um.user, Permission(p))
    }
}

class WriteToFile : Executable<Unit>(
    "wf", """
        テキストファイルになにかを書き込みます。
        -aまたは-o オプションで書き込み方法を指定する必要があります。""".trimIndent()
) {
    val fs by inject<FileSystem>()
    val value by argument(ArgType.String, "text", "書き込む内容")
    val file by argument(ArgType.File, "file", "書き込むファイルの名前")

    val overwrite by option(ArgType.Boolean, "overwrite", "o", "上書きするかどうか")
    val append by option(ArgType.Boolean, "append", "a", "追記するかどうか")
    override suspend fun execute(user: User, rawArgs: List<String>) {
        val value = value.replace("\\n", "\n")
        (file as? TextFile)?.let {
            when {
                overwrite == true -> {
                    it.content.set(user, value)
                }

                append == true -> {
                    it.content.set(user, it.content.get(user) + value)
                }

                else -> {
                    out.println("-aまたは-o オプションで書き込み方法を指定してください。")
                }
            }
        } ?: out.println("有効なファイルではありません")
    }
}


