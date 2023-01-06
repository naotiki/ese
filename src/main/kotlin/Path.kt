@JvmInline
value class Path(val value: String) {
    fun asAbsolute() {

    }
}

object LocationManager {
    var currentDirectory = FileManager.homeDir
        private set
    var currentPath: Path = currentDirectory.getFullPath()
        private set



    @JvmName("getterPath")
    fun getPath(): Path {
        return currentPath
    }


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
     * 渡された[path]を解決します
     * @return 見つからなければnull
     * */
    fun tryResolve(path: Path): File? {
        val isAbsolute = path.value.startsWith("/")
        val isHomeDir = path.value.startsWith("~")
        val partialPath = path.value.split("/")
        return if (isAbsolute || isHomeDir) {
            partialPath.fold<String, File?>(if (isHomeDir) FileManager.homeDir else root) { dir, partial ->
                (dir as? Directory)?.children?.get(partial)
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