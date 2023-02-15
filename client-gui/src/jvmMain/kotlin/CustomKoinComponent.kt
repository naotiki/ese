import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent

abstract class CustomKoinComponent : KoinComponent {
    // Override default Koin instance, initially target on GlobalContext to yours
    override fun getKoin(): Koin = MyKoinContext.koinApp.koin
}
object MyKoinContext {
    lateinit var koinApp: KoinApplication
}