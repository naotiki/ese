package core.vfs.dsl

import core.commands.parser.Command
import core.user.Group
import core.user.User
import core.vfs.Directory
import core.vfs.DynamicDirectory
import core.vfs.FireTree.root
import core.vfs.TextFile


/**
 * FileTreeDSLを開始します
 * @param block DSLを使用し[root]にファイルを追加します
 */
inline fun rootDir(block: Directory.() -> Unit) {
    root.block()
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
    block: Directory.() -> Unit
):
        Directory {
    return Directory(name, this, owner, group).also {
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
    block: Directory.() -> Unit
): Directory {
    return DynamicDirectory(name, this, owner, group).also {
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
fun Directory.file(name: String, content: String, owner: User = this.owner, group: Group = owner.group) =
    addChildren(TextFile(name, this, content, owner, group))

fun Directory.executable(name: String, content: Command<*>, owner: User = this.owner, group: Group = this.ownerGroup) =
    addChildren(TextFile(name, this, "", owner, group))