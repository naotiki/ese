/**
 * 仮想的ファイルの抽象クラス
 * ディレクトリもファイルとする。
 *  @param name ファイルの名前
 *  @param parent 親ディレクトリ、ルートの場合はnull
 *  @param attribute 属性 [FileAttribute]をとる
 */
abstract class File(
    var name: String,
    var parent: Directory?,
    var attribute: Int = FileAttribute.None
) {
    fun getFullPath(): Path {
        val path = mutableListOf<String>()
        var f: File? = this
        while (f?.parent != null) {
            path.add(f.name)
            f = f.parent
        }
        return Path(path.reversed().joinToString("/", "/"))
    }
}

class TextFile(
    name: String, parent: Directory?, content: String
) : File(name, parent) {
     var content = content
         private set

}


open class Directory(name: String, parent: Directory?) : File(name, parent = parent) {
    protected open var _children: MutableMap<String, File> = mutableMapOf()
    val children get() = _children.toMap()


    fun addChildren(vararg childDir: File) {
        _children.putAll(childDir.associateBy { it.name })
    }
}

// 進捗状況に応じて中身が変わるディレクトリ
class DynamicDirectory(name: String, parent: Directory?) :Directory(name, parent){
    init {
        EventManager.addEventListener {

        }
    }
    override var _children: MutableMap<String, File>
        get() = super._children
        set(value) {}
}

object EventManager{
    inline fun addEventListener(block:()->Unit){

    }
}


//初期状態

object FileManager {
    lateinit var homeDir: Directory

    init {
        rootDir {
            dir("bin") {

            }
            dir("home") {
                homeDir = dir(userName) {
                    file(
                        "Readme.txt",
                        """
                        やぁみんな俺だ！
                        このファイルを開いてくれたんだな！
                        """.trimIndent()
                    )
                }
            }
        }
    }
}


val root = Directory("", null)
inline fun rootDir(block: Directory.() -> Unit) {
    root.block()
}

fun Directory.dir(name: String, block: Directory.() -> Unit): Directory {
    return Directory(name, this).also {
        addChildren(it)
        it.block()
    }
}

fun Directory.file(name: String, content: String) = addChildren(TextFile(name, this, content))


enum class FileType {

}

object FileAttribute {
    const val None = 0
    const val Hide = 1

}
