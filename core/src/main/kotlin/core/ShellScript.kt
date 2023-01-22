package core

import kotlin.math.cbrt

object ShellScript {
    fun parse(string: String){
        val a=string.lines().filter { !it.startsWith("#") }
        println(a)
    }
}

//一行のみ
fun expressionParser(string: String){

    val assignment=Regex("^${Variable.nameRule}=")


    when{
        string.contains(assignment)->{

            val a=string.replaceFirst(assignment,"")
            Variable.map[assignment.matchAt(string,0)!!.value.trimEnd('=')]=a
        }
    }
    println(Variable.map)
}

