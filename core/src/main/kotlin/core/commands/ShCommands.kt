package core.commands

import core.Variable
import core.commands.Operator.*
import core.commands.parser.Command
import org.koin.core.component.inject


class If : Command<Unit>("if") {
    override suspend fun execute(rawArgs: List<String>) {

    }

}

class Test : Command<Boolean>("test") {
    val variable by inject<Variable>()
    override suspend fun execute(rawArgs: List<String>): Boolean {

        if (rawArgs.isEmpty()) return false

        if (rawArgs.size == 1) {
            return rawArgs.first() == "true"
        }

        val conditional = rawArgs.take(3).map { it.trim() }
        val o = Operator.values().firstOrNull {
            it.string == conditional[1]
        }
        val a = variable.expandVariable(conditional.first())
        val b = variable.expandVariable(conditional[2])
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