package core

import core.commands.parser.ArgType
import core.commands.parser.Command
import core.vfs.FireTree
import core.vfs.VFS
import kotlinx.coroutines.runBlocking
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
    fun testRunCommand() {
        runBlocking(){
            TestCommand.resolve("-la rdfgv / /home".split(" "))
        }
    }
}

object TestCommand : Command<Unit>(
    "ls", """
    今いる場所のファイルを一覧表示します
""".trimIndent()
) {
    val list by option(ArgType.Boolean, "list", "l", "").default(false)
    val all by option(ArgType.Boolean, "all", "a", "").default(false)
    val directory by argument(ArgType.Dir, "target", "一覧表示するディレクトリ").vararg()
    override suspend fun execute(args: List<String>) {
        val b = directory.ifEmpty { listOf(Vfs.currentDirectory) }
        b.forEach { dir ->
            dir.children.keys.forEach{
                if (list){
                    println(it)
                }else print("$it ")
            }
            println("\n--------")
        }
    }
}
