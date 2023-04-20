expect val clientName:String
expect val platformInitMessage:String
enum class PlatformBackend{
    JVM,
    JS,
}
enum class Platform(val backend: PlatformBackend){
    Desktop(PlatformBackend.JVM),
    Android(PlatformBackend.JVM),
    Web(PlatformBackend.JS)
}
expect val platform:Platform

inline fun only(p: Platform, block:()->Unit){
    if (platform==p) {
        block()
    }
}

inline fun <T> T.alt(p:Platform,block: () -> T):T{
    return if (platform==p) block()
    else this
}
