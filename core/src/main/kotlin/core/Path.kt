package core

@JvmInline
value class Path(val value: String) {
    fun asAbsolute(origin: Directory = LocationManager.currentDirectory) {

    }
}

object LocationManager {
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
        return  if (isAbsolute || isHomeDir) {
            if (partialPath.size==1) {
                when{
                    partialPath.first()==""->return root
                    isHomeDir->return FileManager.homeDir
                }
            }
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