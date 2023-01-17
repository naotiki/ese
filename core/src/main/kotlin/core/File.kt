package core

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

    fun toDirectoryOrNull():Directory?{
        return this as? Directory
    }
}

/**
 * 表示可能な文字列を持つファイル
 * @param [content] 内容
 * */
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
class DynamicDirectory(name: String, parent: Directory?) : Directory(name, parent){
    init {
        EventManager.addEventListener {

        }
    }
    override var _children: MutableMap<String, File>
        get() = super._children
        set(value) {}
}


//初期状態

object FileManager {
    lateinit var homeDir: Directory
    init {
        listOf<String>("").joinToString()
        rootDir {
            dynDir("bin") {

            }
            dir("home") {
                homeDir = dir(userName!!) {
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

/**
 * FileTreeDSLを開始します
 * @param block [dir],[dynDir],[file]などを使用し[root]にファイルを追加します
 */
inline fun rootDir(block: Directory.() -> Unit) {
    root.block()
}

/**
 * ディレクトリを追加します。
 * @param name ディレクトリ名
 * @param block ディレクトリの子を定義するFileTreeDSL
 */
inline fun Directory.dir(name: String, block: Directory.() -> Unit): Directory {
    return Directory(name, this).also {
        addChildren(it)
        it.block()
    }
}
/**
 * 子が動的に変化するディレクトリを追加します。
 * @param name ディレクトリ名
 * @param block ディレクトリの子を定義するFileTreeDSL
 */
inline fun Directory.dynDir(name: String, block: Directory.() -> Unit): Directory {
    return DynamicDirectory(name, this).also {
        addChildren(it)
        it.block()
    }
}

/**
 * テキストファイルを追加します。
 * @param name ファイル名
 * @param content ファイルの内容
 */
fun Directory.file(name: String, content: String) = addChildren(TextFile(name, this, content))


enum class FileType {

}

object FileAttribute {
    const val None = 0
    const val Hide = 1

}
