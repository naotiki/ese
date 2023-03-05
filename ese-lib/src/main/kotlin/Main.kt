
import core.IO
import core.commands.parser.Executable
import core.plugins.EsePlugin
import core.user.User
import core.vfs.Directory
import core.vfs.FileSystem
import core.vfs.Path.Companion.toPath
import core.vfs.dsl.dir
import core.vfs.dsl.executable
import core.vfs.dsl.fileDSL
import org.koin.core.component.inject

class Main : EsePlugin {
    val fs by inject<FileSystem>()
    val io by inject<IO>()
    override fun init(user: User) {
        val installDir = fs.tryResolve("/opt".toPath()) as? Directory
            ?: throw Exception("ばーん💥💥💥")


        fileDSL(installDir, user) {
            dir("AsashoryuPlugin") {
                fs.fileTree.executableEnvPaths += dir("bin") {
                    executable(PluginCommand())
                }
            }
        }

        io.outputStream.println("インストール完了\nはろー！${user.export().name}さん！")

    }
}

class PluginCommand : Executable<Unit>("asa", "Asashoryuプラグインによって追加されたコマンド") {
    override suspend fun execute(user: User, rawArgs: List<String>) {
        out.println("ありえない話し‼️")
    }
}
