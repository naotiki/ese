package command

import core.commands.parser.ArgType
import core.commands.parser.Executable
import core.user.User

class Easy:Executable<Unit>("component/assistant") {
    val enable by argument(ArgType.Boolean,"mode").optional()
    override suspend fun execute(user: User, rawArgs: List<String>) {
        if (enable!=null) {

        }
    }
}