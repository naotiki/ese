package core.vfs

@JvmInline
value class Path(val value: String) {
    fun asAbsolute(origin: Directory = VFS.currentDirectory) {
        TODO("いつか実装")
    }
}


/**
 * Virtual File System
 */
object VFS {
    var currentDirectory = FileManager.homeDir
        private set
    var currentPath: Path = currentDirectory.getFullPath()
        private set

    fun setPath(path: Path): Boolean {
        return (tryResolve(path) as? Directory)?.let {
            currentPath = path
            currentDirectory = it
        } != null
    }

    fun setPath(dir: Directory) {
        currentPath = dir.getFullPath()
        currentDirectory = dir
    }


    /**
     * 渡された[path]を解決し、[File]を返します
     * @return [File],見つからなければnull
     * */
    fun tryResolve(path: Path): File? {
        val isAbsolute = path.value.startsWith("/")
        val isHomeDir = path.value.startsWith("~")
        val partialPath = path.value.trim('/').split("/")
        return if (isAbsolute || isHomeDir) {
            if (partialPath.size == 1) {
                when {
                    partialPath.first() == "" -> return root
                    isHomeDir -> return FileManager.homeDir
                }
            }

            partialPath.drop(if (isHomeDir) 1 else 0).fold<String, File?>(if (isHomeDir) FileManager.homeDir else root) { dir, partial ->
                println(partial+":dir:"+dir?.name)
                dir?.toDirectoryOrNull()?.children?.get(partial)
            }
        } else {
            partialPath.fold<String, File?>(currentDirectory) { dir, partial ->
                (dir as? Directory)?.let {
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