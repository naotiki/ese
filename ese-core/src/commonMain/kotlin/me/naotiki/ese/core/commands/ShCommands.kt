package me.naotiki.ese.core.commands


import me.naotiki.ese.core.Shell
import me.naotiki.ese.core.commands.Operator.*
import me.naotiki.ese.core.commands.parser.Executable
import me.naotiki.ese.core.user.User


class If : Executable<Unit>("if") {
    override suspend fun execute(user: User, rawArgs: List<String>) {

    }

}

class Test : Executable<Boolean>("test") {
    override suspend fun execute(user: User, rawArgs: List<String>): Boolean {

        if (rawArgs.isEmpty()) return false

        if (rawArgs.size == 1) {
            return rawArgs.first() == "true"
        }

        val conditional = rawArgs.take(3).map { it.trim() }
        val o = values().firstOrNull {
            it.string == conditional[1]
        }
        val a = Shell.Variable.expandVariable(conditional.first())
        val b = Shell.Variable.expandVariable(conditional[2])
        return when (o) {
            Equal -> a==b
            NotEqual -> a!=b
            Less -> a.toInt()<b.toInt()
            LessEq -> a.toInt()<=b.toInt()
            Greater -> a.toInt()>b.toInt()
            GreaterEq -> a.toInt()>=b.toInt()
            null -> false
        }
    }
}

enum class Operator(val string: String) {
    Equal("=="),
    NotEqual("!="),
    Less("<"),
    LessEq("<="),
    Greater(">"),
    GreaterEq(">=")
}