package core.vfs.dsl

import core.commands.parser.Executable
import core.user.Group
import core.user.User
import core.vfs.*


/**
 * FileTreeDSLを開始します
 * @param block DSLを使用し[root]にファイルを追加します
 */
inline fun FileTree.rootDir(block: Directory.() -> Unit) {
    this.root.block()
}

/**
 * ディレクトリを追加します。
 * 所有者・グループは親ディレクトリから継承されます。
 * @param name ディレクトリ名
 * @param block ディレクトリの子を定義するFileTreeDSL
 */
inline fun Directory.dir(
    name: String,
    owner: User = this.owner,
    group: Group = owner.group,
    permission: Permission = Permission.dirDefault,
    hidden:Boolean=false,
    block: Directory.() -> Unit={}
):
        Directory {
    return Directory(name, this, owner, group, permission,hidden).also {
        addChildren(it)
        it.block()
    }
}

/**
 * 子が動的に変化するディレクトリを追加します。
 * 所有者・グループは親ディレクトリから継承されます。
 * @param name ディレクトリ名
 * @param block ディレクトリの子を定義するFileTreeDSL
 */
inline fun Directory.dynDir(
    name: String,
    owner: User = this.owner,
    group: Group = owner.group,
    permission: Permission = Permission.dirDefault,
    hidden:Boolean=false,
    block: Directory.() -> Unit
): Directory {
    return DynamicDirectory(name, this, owner, group, permission,hidden).also {
        addChildren(it)
        it.block()
    }
}

/**
 * テキストファイルを追加します。
 * 所有者・グループは親ディレクトリから継承されます。
 * @param name ファイル名
 * @param content ファイルの内容
 */
fun Directory.file(
    name: String,
    content: String,
    owner: User = this.owner,
    group: Group = owner.group,
    permission: Permission = Permission.fileDefault,
    hidden:Boolean=false,
) =
    addChildren(TextFile(name, this, content, owner, group, permission,hidden))

fun <R> Directory.executable(
    executable: Executable<R>,
    name: String=executable.name,
    owner: User = this.owner,
    group: Group = this.ownerGroup,
    permission: Permission=Permission.exeDefault,
    hidden:Boolean=false,
) =
    addChildren(ExecutableFile(name, this, executable, owner, group,permission,hidden))