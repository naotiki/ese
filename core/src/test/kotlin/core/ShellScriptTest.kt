package core

import core.commands.CommandManager
import core.commands.parser.ArgType
import core.commands.parser.Command
import core.vfs.FireTree
import core.vfs.VFS
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ShellScriptTest {
    @BeforeEach
    fun up(){
        Vfs = VFS(FireTree.root)
    }
    @Test
    fun parse() {
        ShellScript.parse(
            """
            # 各分岐の最後の ;; を忘れずに
            case "${'$'}str" in
              "hoge" ) echo "hoge"
                       echo "hoge" ;;
              "fuga" ) echo "" ;;
              * ) echo "unknown" ;;
            esac
        """.trimIndent()
        )
    }

    @Test
    fun a() {
        runBlocking(){
            TestCommandClass.resolve("-la".split(" "))
        }
    }
}

object TestCommandClass : Command<Unit>("ls") {
    val list by option(ArgType.Boolean, "list","l")
    val all by option(ArgType.Boolean, "all", "a")
    val target by argument(ArgType.Dir, name = "target")
    override suspend fun execute(args: List<String>) {
        println(target?.getFullPath())
    }
}