package core.vfs

import core.user.*
import core.user.VUM.rootGroup
import core.user.VUM.uNaotiki
import core.user.VUM.uRoot
import core.vfs.FireTree.root
import core.vfs.dsl.*

@JvmInline
value class Path(val value: String) {
    fun asAbsolute(origin: Directory ) {
        TODO("いつか実装")
    }
}



object FireTree{
    val root = Directory("", null, uRoot, rootGroup)
    lateinit var home:Directory
    init {
        rootDir {

            //動的ディレクトリ
            dynDir("bin") {

            }
            println("bin:OK")
            home=dir("home") {
                uNaotiki.homeDir=dir("naotiki", uNaotiki){
                    file("ひみつのファイル","""
                        みるなよ
                    """.trimIndent())
                }
            }
            dir("usr"){

            }
            dir("sbin"){

            }
            dir("mnt"){

            }
        }
    }
}

/**
 * ぼくのかんがえたさいきょうのVirtual File System
 */
class VFS(currentDirectory: Directory,var homeDir:Directory?=null,) {
    var currentDirectory :Directory=currentDirectory
        private set
    var currentPath: Path = currentDirectory.getFullPath()
        private set
    /**
     * VFSファイルツリーを初期化します。
     * @param user homeDirの所有者
     * */

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

            partialPath.drop(if (isHomeDir) 1 else 0).fold<String, File?>(if (isHomeDir) homeDir else root) { dir, partial ->
                println(partial+":dir:"+dir?.name)
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