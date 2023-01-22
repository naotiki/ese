package core.commands

import core.Variable
import core.commands.Operator.*
import core.commands.parser.Command
import core.expressionParser


object If : Command<Unit>("if") {
    override suspend fun execute(args: List<String>) {

    }

}

object Test : Command<Boolean>("test") {
    override suspend fun execute(args: List<String>): Boolean {
        if (args.isEmpty()) return false

        if (args.size == 1) {
            return args.first() == "true"
        }

        val conditional = args.take(3).map { it.trim() }
        val o = Operator.values().firstOrNull {
            it.string == conditional[1]
        }
        val a = Variable.expandVariable(conditional.first())
        val b = Variable.expandVariable(conditional[2])
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