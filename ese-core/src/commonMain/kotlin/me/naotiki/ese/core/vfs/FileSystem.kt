package me.naotiki.ese.core.vfs

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import me.naotiki.ese.core.EseSystem.FileTree
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class Path(val value: String) {
    fun asAbsolute(origin: Directory) {
        TODO("いつか実装")
    }
    companion object{
        fun String.toPath()=Path(this)
    }
}


/**
 * ぼくのかんがえたさいきょうのVirtual File System
 */
class FileSystem(currentDirectory: Directory)  {
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
    fun setCurrentPath(path: Path) = FileTree.tryResolve(path)?.toDirectoryOrNull()?.let {
        currentPath = path
        currentDirectory = it
        setCurrentPath(it, path)
    }

    /**
     * Overload [setCurrentPath]
     * */
    fun setCurrentPath(dir: Directory) = setCurrentPath(dir, dir.getFullPath())
}