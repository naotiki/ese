package me.naotiki.ese.core.vfs

import me.naotiki.ese.core.EseError
import me.naotiki.ese.core.commands.parser.CommandResult
import me.naotiki.ese.core.commands.parser.Executable
import me.naotiki.ese.core.commands.parser.SuperArgsParser
import me.naotiki.ese.core.user.Group
import me.naotiki.ese.core.user.User
import me.naotiki.ese.core.vfs.Permission.Companion.Operation


class FileValue<T>(private val file: File, private var value: T) {
    fun set(user: User, value: T): Boolean {
        if (file.checkPermission(user, Operation.Write)) {
            this.value = value
            return true
        }
        return false
    }

    fun get(): T = value
}

class SealedFileValue<T>(private val file: File, private var internalValue: T) {
    fun set(user: User, value: T): Boolean {
        if (file.checkPermission(user, Operation.Write)) {
            internalValue = value
            return true
        }
        return false
    }

    fun getOrNull(user: User): T? {
        if (file.checkPermission(user, Operation.Read)) {
            return internalValue
        }
        return null
    }

    fun get(user: User):T{
        if (file.checkPermission(user, Operation.Read)) {
            return internalValue
        }
        throw EseError.FilePermissionError("${user.name}")
    }

}


fun <T> File.sealedValue(value: T) = SealedFileValue(this, value)
fun <T> File.value(value: T) = FileValue(this, value)

/**
 * 仮想的ファイルの抽象クラス
 * ディレクトリもファイルとする。
 *  @param name ファイルの名前
 *  @param parent 親ディレクトリ、ルートの場合はnull
 *  @param hidden 属性 [Boolean]をとる
 */
abstract class File(
    var name: String, val parent: Directory? = null, hidden: Boolean, owner: User, group: Group, permission: Permission,
) {
    val hidden = value(hidden)
    val owner = value(owner)
    val ownerGroup = value(group)
    val permission = value(permission)
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
    private val _content = content
    val content = sealedValue(_content)

}

class ExecutableFile<R>(
    private val executable: Executable<R>,
    name: String = executable.name,
    parent: Directory?,
    owner: User,
    group: Group,
    permission:
    Permission,
    hidden: Boolean
) : File(name, parent, owner = owner, group = group, hidden = hidden, permission = permission) {
    val argParser: SuperArgsParser get() = executable.argParser
    val description get() = executable.description
    fun generateHelpText() = executable.generateHelpText()
    fun verbose(args: List<String>) = executable.verbose(args)
    suspend fun execute(user: User, args: List<String>): CommandResult<out Any?> {

        return if (checkPermission(user, Permission.Companion.Operation.Execute)) {
            executable.resolve(user, args)
        } else {
            executable.out.println("実行権限が不足しています。\nls -lで確認してみましょう。")
            CommandResult.Error()
        }
    }


}

open class Directory(
    name: String, parent: Directory?, owner: User, group: Group, permission: Permission,
    hidden: Boolean
) : File(
    name, parent = parent, owner = owner, group = group, hidden = hidden, permission = permission
) {
    private val _children = mutableMapOf<String, File>()
    private val children = sealedValue(_children)
    fun getChildren(user: User, includeHidden: Boolean = false): Map<String, File>? {
        return children.getOrNull(user)?.filterValues { !it.hidden.get() || includeHidden }?.toMap()
    }

    fun exists(user: User,name: String)=children.getOrNull(user)?.get(name)

    fun addChild(user:User,child: File): Boolean {
        val children=this.children.getOrNull(user)?: return false
        children.put(child.name,child)?:return false
        return true
    }

    @Deprecated("addChild", replaceWith = ReplaceWith("this.addChild(user,childFile.single())"))
    fun addChildren(user: User, vararg childFile: File): Boolean {
        val children=this.children.getOrNull(user)?:return false
        childFile.forEach{ f ->
            children.put(f.name,f)?:return false
        }
        return true
    }

    fun removeChild(user: User, child: File): Boolean {
        return if (checkPermission(user, Operation.Write)) {
            children.getOrNull(user)?.remove(child.name) != null
        } else false
    }

}

