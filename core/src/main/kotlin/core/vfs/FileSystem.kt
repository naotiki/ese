package core.vfs

import core.user.UserManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Serializable
@JvmInline
value class Path(val value: String) {
    fun asAbsolute(origin: Directory) {
        TODO("いつか実装")
    }
}


/**
 * ぼくのかんがえたさいきょうのVirtual File System
 */
class FileSystem(currentDirectory: Directory) : KoinComponent {
    val fileTree by inject<FileTree>()
    val userManager by inject<UserManager>()
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
                    partialPath.first() == "" -> return fileTree.root
                    isHomeDir -> return fileTree.userDir
                }
            }
            partialPath.drop(if (isHomeDir) 1 else 0)
                .fold<String, File?>(if (isHomeDir) fileTree.userDir else fileTree.root) { dir, partial ->
                    println(partial + ":dir:" + dir?.name)
                    dir?.toDirectoryOrNull()?.getChildren(userManager.user)?.get(partial)
                }
        } else {
            partialPath.fold<String, File?>(currentDirectory) { dir, partial ->
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