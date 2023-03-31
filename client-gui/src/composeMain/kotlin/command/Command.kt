package command

import me.naotiki.ese.core.commands.parser.ArgType
import me.naotiki.ese.core.commands.parser.Executable
import me.naotiki.ese.core.user.User
import me.naotiki.ese.core.utils.AtomicInteger

class Easy:Executable<Unit>("me/naotiki/ese/gui/component/assistant") {
    val enable by argument(ArgType.Boolean,"mode").optional()
    override suspend fun execute(user: User, rawArgs: List<String>) {
        if (enable!=null) {

        }
    }
}