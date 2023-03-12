package core.vfs

import core.commands.parser.CommandResult
import core.commands.parser.Executable
import core.commands.parser.SuperArgsParser
import core.export.ExportableData
import core.export.ExportableFile
import core.user.Group
import core.user.User
import core.vfs.Permission.Companion.Operation


class FileValue<T>(private val file: File, private var internalValue: T) {
    fun set(user: User, value: T): Boolean {
        if (file.checkPermission(user, Operation.Write)) {
            internalValue = value
            return true
        }
        return false
    }

    fun get(): T = internalValue
}

class SealedFileValue<T>(private val file: File, private var internalValue: T) {
    fun set(user: User, value: T): Boolean {
        if (file.checkPermission(user, Operation.Write)) {
            internalValue = value
            return true
        }
        return false
    }

    fun get(user: User): T? {
        if (file.checkPermission(user, Operation.Read)) {
            return internalValue
        }
        return null
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

    abstract fun export(): ExportableFile

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
    override fun export(): ExportableFile {
        return ExportableFile(
            name,
            ExportableData.TextData(_content),
            owner.get().id,ownerGroup.get().id,
            permission.get(),hidden.get()
        )
    }
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

    override fun export(): ExportableFile {
        return ExportableFile(
            name,
            ExportableData.ExeData(executable::class.java),
            owner.get().id,ownerGroup.get().id,
            permission.get(),hidden.get()
        )
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
        return children.get(user)?.filterValues { !it.hidden.get() || includeHidden }?.toMap()
    }

    fun addChildren(user: User, vararg childDir: File): Boolean {
        return children.get(user)?.putAll(childDir.associateBy { it.name }) != null
    }

    fun removeChild(user: User, childDir: File): Boolean {
        println("削除：${childDir.name}")
        return if (checkPermission(user, Operation.Write)) {
            children.get(user)?.remove(childDir.name) != null
        } else false
    }

    override fun export(): ExportableFile {
        return ExportableFile(
            name,
            ExportableData.DirectoryData(_children.values.map { it.export() }),
            owner.get().id,
            ownerGroup.get().id,
            permission.get(),
            hidden.get()
        )
    }
}

