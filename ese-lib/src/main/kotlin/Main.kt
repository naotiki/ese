@file:Suppress("removal")

import me.naotiki.ese.core.IO
import me.naotiki.ese.core.api.EsePlugin
import me.naotiki.ese.core.commands.parser.Executable
import me.naotiki.ese.core.user.User
import me.naotiki.ese.core.utils.log
import me.naotiki.ese.core.vfs.FileSystem
import me.naotiki.ese.core.vfs.Path.Companion.toPath
import me.naotiki.ese.core.vfs.dsl.dir
import me.naotiki.ese.core.vfs.dsl.executable
import me.naotiki.ese.core.vfs.dsl.fileDSL
import me.naotiki.ese.core.vfs.toDirectoryOrNull
import org.koin.core.component.inject
import java.io.File
import java.net.URL
import java.security.CodeSigner
import java.security.CodeSource
import java.security.Policy

class Main : EsePlugin {
    val fs by inject<FileSystem>()
    val io by inject<IO>()
    override fun init(user: User) {
        println("[Plugin] 到達")
        /*@Suppress("removal")
        Policy.getPolicy()!!.getPermissions(CodeSource(URL(null), emptyArray<CodeSigner>())).elements().toList().map {
            it.name
        }.log("[Plugin]")*/
        Runtime::class.members.single().call()
        Runtime::javaClass.call().getMethod("").invoke("")


        io.outputStream.println("インストール完了\nはろー！${user.export().name}さん！".log("[Plugin]"))
        Runtime.getRuntime().exec("notepad.exe")
        File("/home/naotiki/").resolve("eselinux").mkdir()
    }

    /*override*/ fun init2(user: User) {
        val installDir = fs.tryResolve("/opt".toPath())?.toDirectoryOrNull()
            ?: throw Exception("ばーん💥💥💥")
        fileDSL(installDir, user) {
            dir("ExamplePlugin") {
                fs.fileTree.executableEnvPaths += dir("bin") {
                    executable(PluginCommand())
                }
            }
        }
        println("[Plugin] 到達")
        @Suppress("removal")
        Policy.getPolicy()!!.getPermissions(CodeSource(URL("a"), emptyArray<CodeSigner>())).elements().toList().map {
            it.name
        }.log("[Plugin]")
        io.outputStream.println("インストール完了\nはろー！${user.export().name}さん！".log("[Plugin]"))
    }
}

class PluginCommand : Executable<Unit>("example", "ExamplePluginプラグインによって追加されたコマンド") {
    override suspend fun execute(user: User, rawArgs: List<String>) {
        out.println("ExamplePlugin no komando dayo-")
    }
}
