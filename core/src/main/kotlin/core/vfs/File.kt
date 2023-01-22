package core.vfs

import core.EventManager
import core.user.Group
import core.user.User




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
    var attribute: Int = FileAttribute.None,
    var owner: User ,
    var ownerGroup:Group

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

    fun toDirectoryOrNull(): Directory?{
        return this as? Directory
    }
}

/**
 * 表示可能な文字列を持つファイル
 * @param [content] 内容
 * */
class TextFile(
    name: String, parent: Directory?, content: String, owner: User, group: Group
) : File(name, parent, owner = owner, ownerGroup = group) {
    var content = content
        private set
}

class ExecutableFile(
    name: String, parent: Directory?, content: String, owner: User, group: Group
) : File(name, parent, owner = owner, ownerGroup = group) {
    var content = content
        private set
}



open class Directory(name: String, parent: Directory?, owner: User, group: Group) : File(
    name,
    parent = parent, owner = owner, ownerGroup = group
) {
    protected open var _children: MutableMap<String, File> = mutableMapOf()
    val children get() = _children.toMap()
    fun addChildren(vararg childDir: File) {
        _children.putAll(childDir.associateBy { it.name })
    }
    fun removeChild(childDir: File): Boolean {
        return _children.remove(childDir.name)!=null
    }
}

// 進捗状況に応じて中身が変わるディレクトリ
class DynamicDirectory(name: String, parent: Directory?, owner: User, group: Group) : Directory(name, parent,owner, group){
    init {
        EventManager.addEventListener {

        }
    }
    override var _children: MutableMap<String, File>
        get() = super._children
        set(value) {}
}


//初期状態







enum class FileType {

}

object FileAttribute {
    const val None = 0
    const val Hide = 1

}
