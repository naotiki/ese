import me.naotiki.ese.core.EseSystem
import me.naotiki.ese.core.api.EsePlugin
import me.naotiki.ese.core.commands.parser.Executable
import me.naotiki.ese.core.user.User
import me.naotiki.ese.core.vfs.Path.Companion.toPath
import me.naotiki.ese.core.vfs.dsl.dir
import me.naotiki.ese.core.vfs.dsl.executable
import me.naotiki.ese.core.vfs.dsl.fileDSL
import me.naotiki.ese.core.vfs.toDirectoryOrNull

class Main : EsePlugin {
    /*override suspend fun init(user: User) {
        println("[Plugin] 到達")
        *//*@Suppress("removal")
        Policy.getPolicy()!!.getPermissions(CodeSource(URL(null), emptyArray<CodeSigner>())).elements().toList().map {
            it.name
        }.log("[Plugin]")*//*
        Runtime::class.members.single().call()
        Runtime::javaClass.call().getMethod("").invoke("")


        EseSystem.IO.printChannel.println("インストール完了\nはろー！${user.name}さん！")
        Runtime.getRuntime().exec("notepad.exe")
        File("/home/naotiki/").resolve("eselinux").mkdir()
    }*/

    override suspend fun init(user: User) {
        val installDir = EseSystem.FileTree.tryResolve("/opt".toPath())?.toDirectoryOrNull()
            ?: fileDSL(EseSystem.FileTree.root, user) {
                dir("/opt")
            }
        fileDSL(installDir, user) {
            dir("ExamplePlugin") {
                EseSystem.FileTree.executableEnvPaths += dir("bin") {
                    executable(PluginCommand())
                }
            }
        }
        println("[Plugin] 到達")
        EseSystem.IO.printChannel.println("インストール完了\nはろー！${user.name}さん！")
    }
}

class PluginCommand : Executable<Unit>("example", "ExamplePluginプラグインによって追加されたコマンド") {
    override suspend fun execute(user: User, rawArgs: List<String>) {
        out.println("ExamplePlugin no komando dayo-")
    }
}
