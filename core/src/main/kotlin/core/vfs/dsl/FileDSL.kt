package core.vfs.dsl

import core.commands.parser.Executable
import core.user.Group
import core.user.User
import core.vfs.*

@DslMarker
internal annotation class FileDSLMarker
@FileDSLMarker
data class DSLContext @PublishedApi internal constructor(val dir: Directory, val operator: User)

/**
 * FileTreeDSLを開始します
 * @param block DSLを使用し[root]にファイルを追加します
 */
internal inline fun FileTree.rootDir(block: DSLContext.() -> Unit) {
    fileDSL(this.root, this.userManager.uRoot,block)
}

inline fun <T> fileDSL(parent: Directory, operator: User, block: DSLContext.() -> T): T {
    return DSLContext(parent, operator).block()
}

/**
 * ディレクトリを追加します。
 * 所有者・グループは親ディレクトリから継承されます。
 * @param name ディレクトリ名
 * @param block ディレクトリの子を定義するFileTreeDSL
 */
inline fun DSLContext.dir(
    name: String,
    owner: User = dir.owner.get(),
    group: Group = dir.ownerGroup.get(),
    permission: Permission = Permission.dirDefault,
    hidden: Boolean = false,
    block: DSLContext.() -> Unit = {}
):
        Directory {
    return Directory(name, dir, owner, group, permission, hidden).also {
        dir.addChildren(operator, it)
        copy(dir = it).block()
    }
}

/**
 * 子が動的に変化するディレクトリを追加します。
 * 所有者・グループは親ディレクトリから継承されます。
 * @param name ディレクトリ名
 * @param block ディレクトリの子を定義するFileTreeDSL
 */
/*inline fun Directory.dynDir(
    name: String,
    owner: User = this.owner.get(),
    group: Group = owner.group,
    permission: Permission = Permission.dirDefault,
    hidden:Boolean=false,
    block: Directory.() -> Unit
): Directory {
    return DynamicDirectory(name, this, owner, group, permission,hidden).also {
        addChildren(it)
        it.block()
    }
}*/

/**
 * テキストファイルを追加します。
 * 所有者・グループは親ディレクトリから継承されます。
 * @param name ファイル名
 * @param content ファイルの内容
 */
fun DSLContext.file(
    name: String,
    content: String,
    owner: User = dir.owner.get(),
    group: Group = owner.group,
    permission: Permission = Permission.fileDefault,
    hidden: Boolean = false,
) = TextFile(name, dir, content, owner, group, permission, hidden).also {
    dir.addChildren(operator, it)
}

inline operator fun DSLContext.invoke(block:DSLContext.()->Unit)=block()

fun <R> DSLContext.executable(
    executable: Executable<R>,
    name: String = executable.name,
    owner: User = dir.owner.get(),
    group: Group = dir.ownerGroup.get(),
    permission: Permission = Permission.exeDefault,
    hidden: Boolean = false,
) =
    dir.addChildren(operator, ExecutableFile(executable, name, dir, owner, group, permission, hidden))