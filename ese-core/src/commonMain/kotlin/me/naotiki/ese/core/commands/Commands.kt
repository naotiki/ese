package me.naotiki.ese.core.commands


import kotlinx.atomicfu.atomic
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import me.naotiki.ese.core.EseError
import me.naotiki.ese.core.EseSystem.IO
import me.naotiki.ese.core.EseSystem.UserManager
import me.naotiki.ese.core.Shell.Expression
import me.naotiki.ese.core.Shell.FileSystem
import me.naotiki.ese.core.Shell.Variable
import me.naotiki.ese.core.commands.parser.ArgType
import me.naotiki.ese.core.commands.parser.Executable
import me.naotiki.ese.core.user.User
import me.naotiki.ese.core.utils.format
import me.naotiki.ese.core.utils.normalizeYesNoAnswer
import me.naotiki.ese.core.version
import me.naotiki.ese.core.vfs.Directory
import me.naotiki.ese.core.vfs.File
import me.naotiki.ese.core.vfs.Permission
import me.naotiki.ese.core.vfs.TextFile
import me.naotiki.ese.core.vfs.dsl.dir
import me.naotiki.ese.core.vfs.dsl.fileDSL
import me.naotiki.ese.core.vfs.dsl.textFile
import kotlin.math.roundToInt

//  UDON is a Downloader Of Noodles


/*class Exec : Executable<Unit>("exec", "RUN") {
    override suspend fun execute(user: User, rawArgs: List<String>) {
        withContext(Dispatchers.IO) {
            val process = ProcessBuilder("medley.exe").start()
            launch {
                withContext(Dispatchers.IO) {
                    process.inputStream.transferTo(io.printChannel)
                }
            }
            launch {
                withContext(Dispatchers.IO) {
                    process.inputStream.transferTo(io.printChannel)
                }
            }
            process.waitFor()
        }
    }

}*/



//Man
class Help : Executable<Unit>(
    "help", """
        役に立ちます。
""".trimIndent()
) {
    override suspend fun execute(user: User, rawArgs: List<String>) {
        val exes = Expression.getExecutables(includeHidden = false).map { it }
        out.println("現在、以下の${exes.count()}個のコマンドが使用可能です。")
        exes.forEach {
            out.println(it.name)
            out.println("  " + it.description)
        }
        out.println()
        out.println("詳細はコマンドに--helpを付けると確認できます。")
    }
}


class ListSegments : Executable<Unit>(
    "ls", """
    今いる場所のファイルを一覧表示します。
""".trimIndent()
) {
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
        (directory ?: FileSystem.currentDirectory).getChildren(user, all)?.forEach { (name, file) ->
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
    val recursive by option(ArgType.Boolean, "recursive", "r", "ディレクトリを削除します。").default(false)
    val interactive by option(ArgType.Boolean, "interactive", "i", "削除前に確認します。").default(false)

    val files by argument(ArgType.File, "target").vararg()

    suspend fun interactiveRemove(user: User, file: File): Boolean {
        val text = if (file is Directory) {
            "ディレクトリ"
        } else {
            "ファイル"
        }
        val ans = IO.newPrompt(client, "$text ${file.getFullPath().value}を削除しますか？ (y/N)")
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
    val directory by argument(ArgType.Dir, "target", "一覧表示するディレクトリ")
    override suspend fun execute(user: User, rawArgs: List<String>) {
        val dir = directory//args.firstOrNull()?.let { Vfs.tryResolve(Path(it)) } as? Directory
        FileSystem.setCurrentPath(dir)
    }
}
class Yes : Executable<Unit>(
    "yes", """
    YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES YES
""".trimIndent()
) {
    val noClean by option(
        ArgType.Boolean, "no-clean",
        description = "ベンチマーク開始前にコンソールをクリアしません。\n--benchmarkと併用します。"
    ).default(false)
    val benchmark by option(
        ArgType.Int,
        "benchmark",
        "b",
        "一秒間のyes数を計測します。\n実行するとコンソールの内容は削除されます。"
    ).validation {
        it>=0
    }.default(0)

    val delay by option(ArgType.Int, "delay", "d", "出力間隔(ms)").validation {
        it >= 0
    }.default(10)

    val value by argument(ArgType.String, "value", "出力する文字列").optional()
    override suspend fun execute(user: User, rawArgs: List<String>) {
        val v = value ?: "yes"

        if (benchmark > 0) {
            val results = mutableListOf<Int>()
            repeat(benchmark) {
                if (!noClean) client.clear()
                val c = atomic(0)

                withTimeoutOrNull(1000) {
                    while (true) {
                        out.println(v)
                        c.incrementAndGet()
                        delay(delay.toLong())
                    }
                }
                results += c.value
            }
            if (!noClean) client.clear()
            out.println()
            out.println(
                """
                |
                |Yes Benchmark ($version) - ${client.getClientName()}
                |Executed by ${user.name}
                |Input  : yes ${rawArgs.joinToString(" ")}
                |Output : "$v"
                |
                |--- Result ---
                |${results.mapIndexed { index, i -> "%3d : %5d yps".format((index + 1), i) }.joinToString("\n")}
                |
                |Min : ${"%5d yps".format(results.min())}
                |Max : ${"%5d yps".format(results.max())}
                |Avg : ${"%5d yps".format(results.average().roundToInt())}
            """.trimMargin()
            )
        } else {
            while (true) {
                out.println(v)
                delay(delay.toLong())
            }
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
            out.println((txt as TextFile).content.getOrNull(user))
        } else out.println("無効なファイル")
    }
}

class Echo : Executable<Unit>("echo", "メッセージを出力します。") {
    private val input by argument(ArgType.String, "msg", "出力するメッセージ").vararg()
    override suspend fun execute(user: User, rawArgs: List<String>) {
        input.joinToString(" ").let { out.println(Variable.expandVariable(it)) }
    }
}

class Clear : Executable<Unit>("clear", "コンソールの出力を削除します。") {
    override suspend fun execute(user: User, rawArgs: List<String>) {
        client.clear()
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
        val n = IO.newPrompt(client, "実行しますか？(続行するにはあなたのユーザー名を入力) >>")
        if (n == user.name) {
            isConfirm = true
            UserManager.setUser(UserManager.uRoot)
            cmd.execute(UserManager.uRoot, targetArgs)
            UserManager.setUser(user)
        } else {
            out.println("残念、間違いなユーザー名")
        }
    }
}

class Exit : Executable<Unit>("exit", "Ese Linux を終了します。") {
    override suspend fun execute(user: User, rawArgs: List<String>) {
        out.println("終了します")
        client.exit()
    }
}

class MakeDirectory : Executable<Unit>("mkdir", "ディレクトリを作成します。") {
    val dirName by argument(ArgType.String, "name", "作成するディレクトリの名前")
    override suspend fun execute(user: User, rawArgs: List<String>) {
        fileDSL(FileSystem.currentDirectory, UserManager.user) {
            dir(dirName)
        }

    }
}

class Touch : Executable<Unit>("touch", "書き込み可能ファイルを作成します。") {
    val fileName by argument(ArgType.String, "name", "作成するファイルの名前")
    override suspend fun execute(user: User, rawArgs: List<String>) {
        fileDSL(FileSystem.currentDirectory, UserManager.user) {
            textFile(fileName, "")
        }
    }
}

class Chmod : Executable<Unit>("chmod", "権限を変更します。") {
    val value by argument(ArgType.String, "value", "権限の値(8進数9桁)")
    val file by argument(ArgType.File, "target", "変更するファイルの名前")
    override suspend fun execute(user: User, rawArgs: List<String>) {
        val p = value.toIntOrNull(8)
        if (p == null || p > 511) {
            throw EseError.CommandIllegalArgumentError("不正な権限値")
        }

        file.permission.set(UserManager.user, Permission(p))
    }
}

class WriteToFile : Executable<Unit>(
    "wf", """
        テキストファイルになにかを書き込みます。
        -aまたは-o オプションで書き込み方法を指定する必要があります。""".trimIndent()
) {
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
                    it.content.set(user, it.content.getOrNull(user) + value)
                }

                else -> {
                    out.println("-aまたは-o オプションで書き込み方法を指定してください。")
                }
            }
        } ?: out.println("有効なファイルではありません")
    }
}


