package me.naotiki.ese.core
//クライアントアプリの実装
interface ClientImpl {
    suspend fun prompt(promptText:String,value:String="")
    fun exit()

    fun clear()
}