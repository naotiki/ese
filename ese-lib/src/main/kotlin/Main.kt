
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
import java.io.File

class Main : EsePlugin {
    val fs by inject<FileSystem>()
    override fun init(user: User) {
        val installDir = fs.tryResolve("/opt".toPath()) as? Directory ?: throw Exception("ばーん")


        fileDSL(installDir, user) {
            dir("ExamplePlugin") {
                fs.fileTree.executableEnvPaths += dir("bin") {
                    executable(PluginCommand())
                }
            }
        }

        println("はろー！${user.export()}\n\nうわぁぁぁっぁぁぁぁぁっぁ")
        File("testtest").mkdir()

    }
}

class PluginCommand : Executable<Unit>("pc", "プラグインによって追加されたコマンド") {
    override suspend fun execute(user: User, rawArgs: List<String>) {
        out.println("うへへへへへ")
    }
}
