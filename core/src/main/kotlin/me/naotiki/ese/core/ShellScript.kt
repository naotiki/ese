package me.naotiki.ese.core

object ShellScript {
    fun parse(string: String){
        val a=string.lines().filter { !it.startsWith("#") }

    }
}

/*TODO Migrate Koin DI
//一行のみ
//解析成功でT,失敗でF
fun expressionParser(string: String):Boolean{

    val assignment=Regex("^${Variable.nameRule}=")


    when{
        string.contains(assignment)->{

            val a=string.replaceFirst(assignment,"")
            Variable.map[assignment.matchAt(string,0)!!.value.trimEnd('=')]=a
        }
        else->return false
    }
    println(Variable.map)
    return true
}
*/

