package me.naotiki.ese.core.vfs

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.naotiki.ese.core.EseSystem
import me.naotiki.ese.core.EseSystem.IO
import me.naotiki.ese.core.commands.parser.ArgType
import me.naotiki.ese.core.commands.parser.Executable
import me.naotiki.ese.core.secure.PluginLoader
import me.naotiki.ese.core.user.User
import me.naotiki.ese.core.utils.normalizeYesNoAnswer
import java.io.File
import java.util.jar.JarFile

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
           EseLinux MemoryInfo
           Free : ${"%,6d MB".format((Runtime.getRuntime().freeMemory() / (1024 * 1024)))}
           Total: ${"%,6d MB".format((Runtime.getRuntime().totalMemory() / (1024 * 1024)))}
           Max  : ${"%,6d MB".format((Runtime.getRuntime().maxMemory() / (1024 * 1024)))}
        """.trimIndent()
        )

    }
}

//Pluginsフォルダなど
val dataDir = File(System.getProperty("compose.application.resources.dir") ?: "client-gui/resources/common/")

class Udon : Executable<Unit>("udon", "UDON is a Downloader Of Noodles") {
    var agree = false
    override val subCommands: List<SubCommand<*>>
    = listOf(Install(),LocalInstall())

    //さぶこまんど
    inner class Install : SubCommand<Unit>("world", "世界中からインストールします。") {

        val pkgName by argument(ArgType.String, "packageName", "パッケージ名")
        override suspend fun execute(user: User, rawArgs: List<String>) {
            out.println("[DEMO]Installing $pkgName ")

        }
    }

    inner class LocalInstall : SubCommand<Unit>("local", "ローカルファイルからインストールします。") {
        val pkgName by argument(ArgType.String, "packageName", "パッケージ名")
        override suspend fun execute(user: User, rawArgs: List<String>) {
            val pluginDir = File(dataDir, "plugins")
            if (!pluginDir.exists()) {
                out.println("pluginフォルダーが見つかりませんでした。\n${pluginDir.absolutePath}に作成してください。")
            }
            val file = pluginDir.listFiles { dir, name -> name.endsWith(".$fileExtension") }.orEmpty().singleOrNull {
                it.nameWithoutExtension == pkgName
            } ?: return
            val jarFile = withContext(Dispatchers.IO) {
                JarFile(file)
            }
            val targetClassName = jarFile.manifest.mainAttributes.getValue("Plugin-Class")
            var ans: Boolean?
            do {
                ans = normalizeYesNoAnswer(
                    IO.newPrompt(client, "プラグイン ${file.nameWithoutExtension} を本当にインストールしますか？(yes/no)")
                )
            } while (ans == null)
            if (ans != true) {

                return
            }
            val plugin = PluginLoader.loadPluginFromFile(file)

            plugin?.init(user)
        }
    }

    override suspend fun execute(user: User, rawArgs: List<String>) {
        out.println(
            """
            Udon は EseLinuxのプラグインマネージャーです。
            """.trimIndent()
        )
    }

    companion object {
        const val fileExtension = "ndl"
    }
}

