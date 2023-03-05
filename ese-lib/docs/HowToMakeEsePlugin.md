# How to make Ese Plugin (Japanese)
## 基本
```kotlin

import me.naotiki.ese.core.IO
import me.naotiki.ese.core.commands.parser.Executable
import me.naotiki.ese.core.plugins.EsePlugin
import me.naotiki.ese.core.user.User
import me.naotiki.ese.core.vfs.Directory
import me.naotiki.ese.core.vfs.FileSystem
import me.naotiki.ese.core.vfs.Path.Companion.toPath
import me.naotiki.ese.core.vfs.dsl.dir
import me.naotiki.ese.core.vfs.dsl.executable
import me.naotiki.ese.core.vfs.dsl.fileDSL
import org.koin.core.component.inject

class Main : EsePlugin {
    val fs by inject<FileSystem>()
    val io by inject<IO>()
    override fun init(user: User) {
        val installDir = fs.tryResolve("/opt".toPath()) as? Directory
            ?: return


        fileDSL(installDir, user) {
            dir("ExamplePlugin") {
                fs.fileTree.executableEnvPaths += dir("bin") {
                    executable(PluginCommand())
                }
            }
        }

        io.outputStream.println("インストール完了\nはろー！${user.export().name}さん！")

    }
}

class PluginCommand : Executable<Unit>("example", "ExamplePluginプラグインによって追加されたコマンド") {
    override suspend fun execute(user: User, rawArgs: List<String>) {
        out.println("ExamplePlugin no komando dayo-")
    }
}
```
