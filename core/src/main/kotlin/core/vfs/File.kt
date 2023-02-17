package core.vfs

import core.EventManager
import core.commands.parser.Executable
import core.user.Group
import core.user.User
import kotlinx.serialization.json.JsonNull.content
import org.koin.core.component.KoinComponent


class FileValue<T>(private val file: File, private var internalValue:T):KoinComponent{
    fun set(user: User,value: T):Boolean {
        internalValue=value
        return true
    }
    fun get(): T {
        return internalValue
    }
}
class SealedFileValue<T>(private val file: File, private var internalValue:T):KoinComponent{
    fun set(user: User,value: T):Boolean {
        internalValue=value
        return true
    }
    fun get(user: User): T? {
        return internalValue
    }
}


fun <T> File.sealedValue(value:T)=SealedFileValue(this,value)
fun <T> File.value(value:T)=FileValue(this,value)
/**
 * 仮想的ファイルの抽象クラス
 * ディレクトリもファイルとする。
 *  @param name ファイルの名前
 *  @param parent 親ディレクトリ、ルートの場合はnull
 *  @param hidden 属性 [Boolean]をとる
 */
open class File(
    var name: String,  val parent: Directory? = null,hidden: Boolean, owner: User, group: Group, permission: Permission,
) {
    val hidden = value(hidden)
    val owner=value(owner)
    val ownerGroup=value(group)
    val permission=value(permission)
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

fun File.toDirectoryOrNull(): Directory? {
    return if (this is Directory) {
        this
    } else null
}


/**
 * 表示可能な文字列を持つファイル
 * @param [content] 内容
 * */
class TextFile(
    name: String,
    parent: Directory?,
    content: String,
    owner: User,
    group: Group,
    permission: Permission,
    hidden: Boolean
) : File(name, parent, owner = owner, group = group, hidden = hidden, permission = permission) {
    val content = sealedValue(content)
}

class ExecutableFile<R>(
    name: String, parent: Directory?, executable: Executable<R>, owner: User, group: Group, permission: Permission,
    hidden: Boolean
) : File(name, parent, owner = owner, group = group, hidden = hidden, permission = permission) {
    val executable = value(executable)
}

open class Directory(
    name: String, parent: Directory?, owner: User, group: Group, permission: Permission,
    hidden: Boolean
) : File(
    name, parent = parent, owner = owner, group = group, hidden = hidden, permission = permission
) {
    open var _children=sealedValue(mutableMapOf<String,File>())
    fun getChildren(user: User, includeHidden: Boolean = false): Map<String, File>? {
        return _children.get(user)?.filterValues { !it.hidden.get() || includeHidden }?.toMap()
    }

    /*fun getChildren(user: User, includeHidden: Boolean = false): Map<String, File>? {
        return if (
            permission.get().has(
                when {
                    user == owner.get() -> {
                        PermissionTarget.OwnerR
                    }

                    user.group == ownerGroup.get() -> {
                        PermissionTarget.GroupR
                    }

                    else -> {
                        PermissionTarget.OtherR
                    }
                }
            )
        ) _children.filterValues { !it.hidden.get() || includeHidden }.toMap() else null


    }*/

    fun addChildren(user:User,vararg childDir: File): Boolean {
        return _children.get(user)?.putAll(childDir.associateBy { it.name })!=null
    }

    fun removeChild(user:User,childDir: File): Boolean {
        return _children.get(user)?.remove(childDir.name) != null
    }
}

/*
// 進捗状況に応じて中身が変わるディレクトリ TODO 実装やれ
class DynamicDirectory(
    name: String,
    parent: Directory?,
    owner: User,
    group: Group,
    permission: Permission,
    hidden: Boolean
) :
    Directory(name, parent, owner, group, permission, hidden) {
    init {
        EventManager.addEventListener {

        }
    }

    override var _children: MutableMap<String, File>
        get() = super._children
        set(value) {}
}
*/



