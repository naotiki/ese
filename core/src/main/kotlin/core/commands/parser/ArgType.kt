package core.commands.parser

import core.commands.Expression
import core.vfs.*
import org.koin.core.Koin

sealed class ArgType<out T : Any> constructor(inline val converter: Koin.(kotlin.String) -> T?) {


    //Primitive Types
    object Int : ArgType<kotlin.Int>({ it.toIntOrNull() })
    object String : ArgType<kotlin.String>({ it })
    object Boolean : ArgType<kotlin.Boolean>({ it.toBooleanStrictOrNull() })

    //Special Types
    object File : ArgType<core.vfs.File>({
        get<FileSystem>().tryResolve(Path(it))
    })


    object Dir : ArgType<Directory>({
        get<FileSystem>().tryResolve(Path(it))?.toDirectoryOrNull()
    })

    object Executable : ArgType<ExecutableFile<*>>({ get<Expression>().tryResolve(it) })


    class Define<T : Any>(converter: Koin.(kotlin.String) -> T?) : ArgType<T>(converter)

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