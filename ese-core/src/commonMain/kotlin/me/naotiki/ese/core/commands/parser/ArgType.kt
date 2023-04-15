package me.naotiki.ese.core.commands.parser

import me.naotiki.ese.core.EseSystem.FileTree
import me.naotiki.ese.core.Shell.Expression
import me.naotiki.ese.core.vfs.Directory
import me.naotiki.ese.core.vfs.ExecutableFile
import me.naotiki.ese.core.vfs.Path
import me.naotiki.ese.core.vfs.toDirectoryOrNull

sealed class ArgType<out T : Any>(inline val converter: (kotlin.String) -> T?) {


    //Primitive Types
    object Int : ArgType<kotlin.Int>({ it.toIntOrNull() })
    object String : ArgType<kotlin.String>({ it })
    object Boolean : ArgType<kotlin.Boolean>({ it.toBooleanStrictOrNull() })

    //Special Types
    object File : ArgType<me.naotiki.ese.core.vfs.File>({
        FileTree.tryResolve(Path(it))
    })
    object Dir : ArgType<Directory>({
        FileTree.tryResolve(Path(it))?.toDirectoryOrNull()
    })

    object Executable : ArgType<ExecutableFile<*>>({ Expression.tryResolve(it) })


    class Define<T : Any>(converter: (kotlin.String) -> T?) : ArgType<T>(converter)

    class Choice<T : Any>(val choices: List<T>) : ArgType<T>({ str ->
        choices.singleOrNull { it.toString().equals(str, true) }
    })

    companion object {
        inline fun <reified T : Enum<T>> Choice(
        ): Choice<T> {
            return Choice(enumValues<T>().toList())
        }
    }
}