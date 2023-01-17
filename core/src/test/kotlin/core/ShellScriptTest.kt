package core

import org.junit.jupiter.api.Test
class ShellScriptTest {
    @Test
    fun parse() {
        ShellScript.parse("""
            # 各分岐の最後の ;; を忘れずに
            case "${'$'}str" in
              "hoge" ) echo "hoge"
                       echo "hoge" ;;
              "fuga" ) echo "" ;;
              * ) echo "unknown" ;;
            esac
        """.trimIndent())
    }
    @Test
    fun a(){
        val strs=listOf("a","b","c","d","e")
        println(strs)
        println(strs.joinToString())
    }
}