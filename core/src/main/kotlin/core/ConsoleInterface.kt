package core

interface ConsoleInterface {
    fun prompt(promptText:String,value:String="")
    fun exit()

    fun clear()
}