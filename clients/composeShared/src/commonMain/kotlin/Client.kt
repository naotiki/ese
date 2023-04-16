expect val clientName:String
expect val platformInitMessage:String
enum class ClientPlatform{
    JVM,
    JS,
}
expect val clientPlatform:ClientPlatform

inline fun only(platform: ClientPlatform,block:()->Unit){
    if (clientPlatform==platform) {
        block()
    }
}
