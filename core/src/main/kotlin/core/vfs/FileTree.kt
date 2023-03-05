package core.vfs

import core.commands.*
import core.commands.dev.Parse
import core.commands.dev.Status
import core.commands.parser.Executable
import core.user.UserManager
import core.vfs.dsl.dir
import core.vfs.dsl.executable
import core.vfs.dsl.file
import core.vfs.dsl.rootDir

class FileTree(val userManager: UserManager)  {
    val userDir: Directory? get() = userManager.user.dir
    val root = Directory("", null, userManager.uRoot, userManager.rootGroup, Permission(0b111_111_111), false)
    lateinit var home: Directory
    val executableEnvPaths = mutableListOf<Directory>()

    init {
        val initialCommands = listOf<Executable<*>>(
            ListSegments(), ChangeDirectory(), Cat(), Exit(), SugoiUserDo(),
            Yes(), Clear(), Echo(), Remove(), Test(),
            Help(),MakeDirectory(),Touch(),Chmod(),WriteToFile(),Exec(),Udon()
        )
        rootDir {

            //動的ディレクトリ
            executableEnvPaths+= dir("bin") {
                initialCommands.forEach {
                    executable(it)
                }
            }
            home = dir("home") {
                userManager.uNaotiki.dir = dir("naotiki", userManager.uNaotiki) {
                    file(
                        "ひみつのファイル", """
                        みるなよ
                    """.trimIndent()
                    )
                }
            }
            dir("sbin") {

            }
            dir(".ese", hidden = true) {
                executableEnvPaths+= dir("bin") {
                    executable(Parse(), hidden = true)
                    executable(Status(), hidden = true)
                }
            }
            dir("opt"){

            }
        }
    }
}