package core
//クライアントアプリの実装
interface ClientImpl {
    fun prompt(promptText:String,value:String="")
    fun exit()

    fun clear()
}