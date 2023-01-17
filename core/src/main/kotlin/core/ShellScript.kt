package core

object ShellScript {


    fun parse(string: String){
        val a=string.lines().filter { !it.startsWith("#") }
        println(a)
    }
}


fun expressionParser(string: String){

}