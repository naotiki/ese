package core.vfs

import core.commands.*
import core.commands.parser.Command
import core.user.VUM
import core.user.VUM.rootGroup
import core.user.VUM.uNaotiki
import core.user.VUM.uRoot
import core.vfs.FireTree.root
import core.vfs.dsl.*
import kotlinx.coroutines.flow.MutableStateFlow

@JvmInline
value class Path(val value: String) {
    fun asAbsolute(origin: Directory) {
        TODO("いつか実装")
    }
}


object FireTree {
    val root = Directory("", null, uRoot, rootGroup, Permission(0b111_111_111))
    lateinit var home: Directory
    val executableEnvPaths = mutableListOf<Directory>()

    init {
        val initialCommands = listOf<Command<*>>(
            ListFile(), ChangeDirectory(), Cat(), Exit(), SugoiUserDo(),
            Yes(), Clear(), Echo(), Remove(), Test(), Parse()
        )
        rootDir {

            //動的ディレクトリ
            executableEnvPaths.add(dynDir("bin") {
                initialCommands.forEach {
                    executable(it)
                }
            })
            println("bin:OK")
            home = dir("home") {
                uNaotiki.homeDir = dir("naotiki", uNaotiki) {
                    file(
                        "ひみつのファイル", """
                        みるなよ
                    """.trimIndent()
                    )
                }
            }
            dir("usr") {

            }
            dir("sbin") {

            }
            dir("mnt") {

            }
        }
    }
}

/**
 * ぼくのかんがえたさいきょうのVirtual File System
 */
class VFS(currentDirectory: Directory) {
    val homeDir: Directory? get() = VUM.user?.homeDir
    var currentDirectory: Directory = currentDirectory
        private set
    var currentPath: Path = currentDirectory.getFullPath()
        private set

    val currentDirectoryFlow = MutableStateFlow(currentDirectory)

    private fun setCurrentPath(dir: Directory, path: Path) {
        currentDirectory = dir
        currentPath = path
        currentDirectoryFlow.tryEmit(dir)
    }

    /**
     * Overload [setCurrentPath]
     * */
    fun setCurrentPath(path: Path) = tryResolve(path)?.toDirectoryOrNull()?.let {
        currentPath = path
        currentDirectory = it
        setCurrentPath(it, path)
    }

    /**
     * Overload [setCurrentPath]
     * */
    fun setCurrentPath(dir: Directory) = setCurrentPath(dir, dir.getFullPath())


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
                    isHomeDir -> return homeDir
                }
            }

            partialPath.drop(if (isHomeDir) 1 else 0)
                .fold<String, File?>(if (isHomeDir) homeDir else root) { dir, partial ->
                    println(partial + ":dir:" + dir?.name)
                    dir?.toDirectoryOrNull()?.children?.get(partial)
                }
        } else {
            partialPath.fold<String, File?>(currentDirectory) { dir, partial ->
                dir?.toDirectoryOrNull()?.let {
                    when (partial) {
                        "." -> it
                        ".." -> it.parent
                        else -> it.children[partial]
                    }
                }
            }
        }
    }
}