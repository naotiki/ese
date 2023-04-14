package me.naotiki.ese.core.vfs

import me.naotiki.ese.core.Shell
import me.naotiki.ese.core.commands.*
import me.naotiki.ese.core.commands.dev.Parse
import me.naotiki.ese.core.commands.parser.Executable
import me.naotiki.ese.core.user.UserManager
import me.naotiki.ese.core.vfs.dsl.dir
import me.naotiki.ese.core.vfs.dsl.executable
import me.naotiki.ese.core.vfs.dsl.rootDir
import me.naotiki.ese.core.vfs.dsl.textFile
expect val platformCommands:List<Executable<*>>
expect val platformDevCommands:List<Executable<*>>
class FileTree(val userManager: UserManager)  {
    val userDir: Directory? get() = userManager.user.dir
    val root = Directory("", null, userManager.uRoot, userManager.rootGroup, Permission(0b111_111_111), false)
    lateinit var home: Directory
    val executableEnvPaths = mutableListOf<Directory>()


    init {
        val initialCommands = listOf<Executable<*>>(
            ListSegments(), ChangeDirectory(), Cat(), Exit(), SugoiUserDo(),
            Yes(), Clear(), Echo(), Remove(), Test(), Help(),MakeDirectory(),
            Touch(),Chmod(),WriteToFile()
        )+platformCommands
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
                    platformDevCommands.forEach {
                        executable(it, hidden = true)
                    }
                }
            }
            dir("opt"){

            }
        }
    }

    /**
     * 渡された[path]を解決し、[File]を返します
     * @return [File],見つからなければ null
     * */
    fun tryResolve(path: Path): File? {
        val isAbsolute = path.value.startsWith("/")
        val isHomeDir = path.value.startsWith("~")
        val partialPath = path.value.trim('/').split("/")
        return if (isAbsolute || isHomeDir) {
            if (partialPath.size == 1) {
                when {
                    partialPath.first() == "" -> return root
                    isHomeDir -> return userDir
                }
            }
            partialPath.drop(if (isHomeDir) 1 else 0)
                .fold<String, File?>(if (isHomeDir) userDir else root) { dir, partial ->
                    dir?.toDirectoryOrNull()?.getChildren(userManager.user)?.get(partial)
                }
        } else {
            partialPath.fold<String, File?>(Shell.FileSystem.currentDirectory) { dir, partial ->
                dir?.toDirectoryOrNull()?.let {
                    when (partial) {
                        "." -> it
                        ".." -> it.parent
                        else -> it.getChildren(userManager.user)?.get(partial)
                    }
                }
            }
        }
    }
}