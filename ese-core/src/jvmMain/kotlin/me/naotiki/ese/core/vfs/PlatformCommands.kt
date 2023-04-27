package me.naotiki.ese.core.vfs

import me.naotiki.ese.core.EseError
import me.naotiki.ese.core.EseSystem.IO
import me.naotiki.ese.core.appName
import me.naotiki.ese.core.commands.parser.ArgType
import me.naotiki.ese.core.commands.parser.Executable
import me.naotiki.ese.core.platformImpl
import me.naotiki.ese.core.secure.PluginLoader
import me.naotiki.ese.core.user.User
import me.naotiki.ese.core.utils.normalizeYesNoAnswer
import me.naotiki.ese.core.vfs.Udon.SearchType.*
import java.io.File
import java.io.FileFilter

actual val platformCommands: List<Executable<*>> =
    listOf(Udon())
actual val platformDevCommands: List<Executable<*>> =
    listOf(Status())

class Status : Executable<Unit>(
    "stat", """
    Print JRE Status  
    開発用 / For development
""".trimIndent()
) {
    override suspend fun execute(user: User, rawArgs: List<String>) {

        out.println(
            """
           $appName MemoryInfo
           Free : ${"%,6d MB".format((Runtime.getRuntime().freeMemory() / (1024 * 1024)))}
           Total: ${"%,6d MB".format((Runtime.getRuntime().totalMemory() / (1024 * 1024)))}
           Max  : ${"%,6d MB".format((Runtime.getRuntime().maxMemory() / (1024 * 1024)))}
        """.trimIndent()
        )

    }
}


class Udon : Executable<Unit>("udon", "Udon : Downloader Of Noodles") {
    var agree = false
    override val subCommands: List<SubCommand<*>> = listOf(Install(), LocalInstall(), Search())

    //うどんワールド
    inner class Install : SubCommand<Unit>("world", "世界中からNoodleをインストールします。") {

        val pkgName by argument(ArgType.String, "packageName", "パッケージ名")
        override suspend fun execute(user: User, rawArgs: List<String>) {
            out.println("[DEMO]Installing $pkgName ")

        }
    }

    //ディレクトリの存在が保証されます。
    private fun getPluginDirOrThrow(): File {
        return platformImpl.getEseHomeDir()?.takeIf { it.exists() }
            ?: throw EseError.CommandExecutionError("Ese Homeフォルダーが見つかりませんでした。")
    }

    inner class LocalInstall : SubCommand<Unit>("local", "ローカルファイルからインストールします。") {
        val pkgName by argument(ArgType.String, "packageName", "パッケージ名")
        override suspend fun execute(user: User, rawArgs: List<String>) {
            val pluginDir = getPluginDirOrThrow()
            val ndlFile = pluginDir.getPluginFiles {
                it.nameWithoutExtension == pkgName
            }.single()
            var ans: Boolean?
            do {
                ans = normalizeYesNoAnswer(
                    IO.newPrompt(client, "Noodle ${ndlFile.nameWithoutExtension} を本当にインストールしますか？(yes/no)")
                )
            } while (ans == null)
            if (ans != true) {
                return
            }
            val plugin = PluginLoader.loadPluginFromFile(ndlFile)

            plugin?.init(user)?:throw EseError.CommandExecutionError("UDON ERROR:Noodle 「${ndlFile.nameWithoutExtension}」を ロードできませんでした")
        }
    }

    private enum class SearchType {
        First,
        Last,
        Contains,
        Complete,
    }

    inner class Search : SubCommand<Unit>("search", "Noodleを検索します") {

        val ndlName by argument(ArgType.String, "packageName", "パッケージ名")
        private val type by option(ArgType.Choice<SearchType>(), "type", "t", "検索方法").default(First)
        private val isLocal by option(ArgType.Boolean, "local", description = "ローカルファイルのみを検索します")
        private val isWorld by option(ArgType.Boolean, "world", description = "ローカルファイルのみを検索します")
        override suspend fun execute(user: User, rawArgs: List<String>) {

            if (isLocal != false) {

                out.println("Search ${platformImpl.getEseHomeDir()?.absolutePath}")
                val eseDir = getPluginDirOrThrow()
                val files = eseDir.getPluginFiles {
                    when (type) {
                        First -> it.nameWithoutExtension.startsWith(ndlName)
                        Last -> it.nameWithoutExtension.endsWith(ndlName)
                        Contains -> it.nameWithoutExtension.contains(ndlName)
                        Complete -> it.nameWithoutExtension.contentEquals(ndlName)//TODO REGEXでパッケージ名規約作成→バージョン部と区別
                    }
                }
                out.println("${files.size}件 ヒット")
                files.forEach {
                    out.println("${it.nameWithoutExtension} \n  → ${it.absolutePath} ")
                }
            }

            if (isWorld != false) {

            }


        }
    }

    override suspend fun execute(user: User, rawArgs: List<String>) {
        out.println(
            """
            Udon : Downloader Of Noodles.
            Udon は $appName のプラグインマネージャーです。
            """.trimIndent()
        )
    }

    companion object {
        const val fileExtension = "ndl"

        fun File.getPluginFiles(predicate: (File) -> Boolean): Array<out File> {
            return listFiles(FileFilter {
                it.name.endsWith(".$fileExtension") && predicate(it)
            }).orEmpty()
        }
    }
}

