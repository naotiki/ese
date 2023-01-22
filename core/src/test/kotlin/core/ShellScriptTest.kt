
package core
/*
import core.commands.parser.ArgType
import core.commands.parser.Executable
import core.user.UserManager
import core.vfs.FileSystem
import core.vfs.FileTree
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.test.KoinTest

class ShellScriptTest :KoinTest{
    @BeforeEach
    fun up(){
        startKoin {
            module {
                single { UserManager() }
                single { FileTree(get()) }
                single { FileSystem(get()) }
            }
        }
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
        runBlocking {
            TestExecutable.resolve("-la rdfgv / /home".split(" "))
        }
    }
}

object TestExecutable : Executable<Unit>(
    "ls", """
    今いる場所のファイルを一覧表示します
""".trimIndent()
) {
    val fs by inject<FileSystem>()
    val list by option(ArgType.Boolean, "list", "l", "").default(false)
    val all by option(ArgType.Boolean, "all", "a", "").default(false)
    val directory by argument(ArgType.Dir, "target", "一覧表示するディレクトリ").vararg()
    override suspend fun execute(rawArgs: List<String>) {
        val b = directory.ifEmpty { listOf(fs.currentDirectory) }
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
*/
