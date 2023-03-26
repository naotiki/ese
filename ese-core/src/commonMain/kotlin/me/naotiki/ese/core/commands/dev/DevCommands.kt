package me.naotiki.ese.core.commands.dev

import me.naotiki.ese.core.commands.parser.ArgType
import me.naotiki.ese.core.commands.parser.Executable
import me.naotiki.ese.core.user.User

class Parse : Executable<Unit>(
    "devp", """
    Print verbose log of parser  
    開発用 / For development
""".trimIndent()
) {
    val cmd by  argument(ArgType.Executable,"cmd")
    val bypassArgs by argument(ArgType.String, "args").vararg(true)
    override suspend fun execute(user: User, rawArgs: List<String>) {
        cmd.verbose(bypassArgs)
    }
}



class CommandDefineException(message:String):Exception(message)