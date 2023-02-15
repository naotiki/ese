package command

import core.commands.parser.ArgType
import core.commands.parser.Executable

class Easy:Executable<Unit>("easy") {
    val enable by argument(ArgType.Boolean,"mode").optional()
    override suspend fun execute(rawArgs: List<String>) {
        if (enable!=null) {

        }
    }
}