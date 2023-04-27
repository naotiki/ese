package me.naotiki.ese.core
//クライアントアプリの実装
interface ClientImpl {
    fun getClientName()="Unknown Client"
    suspend fun prompt(promptText:String,value:String="")
    fun exit()

    fun clear()
}

expect interface PlatformImpl
lateinit var platformImpl: PlatformImpl
    private set
fun initializePlatformImpl(impl: PlatformImpl){
    platformImpl=impl
}