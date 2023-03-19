package me.naotiki.ese.core.vfs

import me.naotiki.ese.core.commands.*
import me.naotiki.ese.core.commands.dev.Parse
import me.naotiki.ese.core.commands.dev.Status
import me.naotiki.ese.core.commands.parser.Executable
import me.naotiki.ese.core.user.UserManager
import me.naotiki.ese.core.vfs.dsl.dir
import me.naotiki.ese.core.vfs.dsl.executable
import me.naotiki.ese.core.vfs.dsl.rootDir
import me.naotiki.ese.core.vfs.dsl.textFile

class FileTree(val userManager: UserManager)  {
    val userDir: Directory? get() = userManager.user.dir
    val root = Directory("", null, userManager.uRoot, userManager.rootGroup, Permission(0b111_111_111), false)
    lateinit var home: Directory
    val executableEnvPaths = mutableListOf<Directory>()


    init {
        val initialCommands = listOf<Executable<*>>(
            ListSegments(), ChangeDirectory(), Cat(), Exit(), SugoiUserDo(),
            Yes(), Clear(), Echo(), Remove(), Test(), Help(),MakeDirectory(),
            Touch(),Chmod(),WriteToFile(),Udon()
        )
        rootDir {
            executableEnvPaths+= dir("bin") {
                initialCommands.forEach {
                    executable(it)
                }
            }
            home = dir("home") {
                userManager.uNaotiki.dir = dir("naotiki", userManager.uNaotiki) {
                    textFile(
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