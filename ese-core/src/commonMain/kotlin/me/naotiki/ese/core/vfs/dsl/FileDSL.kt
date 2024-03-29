package me.naotiki.ese.core.vfs.dsl

import me.naotiki.ese.core.EseSystem
import me.naotiki.ese.core.commands.parser.Executable
import me.naotiki.ese.core.user.Group
import me.naotiki.ese.core.user.User
import me.naotiki.ese.core.vfs.*

@DslMarker
internal annotation class EseFileTreeDslMarker

@EseFileTreeDslMarker
data class FileDslContext @PublishedApi internal constructor(val dir: Directory, val operator: User)

/**
 * FileTreeDSLを開始します
 * @param block DSLを使用し[root]にファイルを追加します
 */
internal inline fun FileTree.rootDir(block: FileDslContext.() -> Unit) {
    fileDSL(this.root, EseSystem.UserManager.uRoot,block)
}

@EseFileTreeDslMarker
inline fun <T> fileDSL(parent: Directory, operator: User, block: FileDslContext.() -> T): T {
    return FileDslContext(parent, operator).block()
}

/**
 * ディレクトリを追加します。
 * 所有者・グループは親ディレクトリから継承されます。
 * @param name ディレクトリ名
 * @param block ディレクトリの子を定義するFileTreeDSL
 */
inline fun FileDslContext.dir(
    name: String,
    owner: User = dir.owner.get(),
    group: Group = dir.ownerGroup.get(),
    permission: Permission = Permission.dirDefault,
    hidden: Boolean = false,
    block: FileDslContext.() -> Unit = {}
):Directory {
    return Directory(name, dir, owner, group, permission, hidden).also {
        dir.addChild(operator, it)
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
fun FileDslContext.textFile(
    name: String,
    content: String,
    owner: User = dir.owner.get(),
    group: Group = owner.group,
    permission: Permission = Permission.fileDefault,
    hidden: Boolean = false,
) = TextFile(name, dir, content, owner, group, permission, hidden).also {
    dir.addChild(operator, it)
}

inline operator fun FileDslContext.invoke(block:FileDslContext.()->Unit)=block()

fun <R> FileDslContext.executable(
    executable: Executable<R>,
    name: String = executable.name,
    owner: User = dir.owner.get(),
    group: Group = dir.ownerGroup.get(),
    permission: Permission = Permission.exeDefault,
    hidden: Boolean = false,
) =ExecutableFile(executable, name, dir, owner, group, permission, hidden).also {

    dir.addChild(
        operator,
        it
    )
}