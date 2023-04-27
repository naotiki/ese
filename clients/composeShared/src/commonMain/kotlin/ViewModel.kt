import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.reflect.KProperty

interface ViewModel {

}

@PublishedApi
internal object ViewModelStore {
    @PublishedApi
    internal val store = mutableMapOf<String, ViewModel>()
    inline fun <reified T : ViewModel> getOrCreate(builder: () -> ViewModel): T {
        return store.getOrPut(T::class.simpleName!!, builder) as T
    }
}

class ViewModelInstance<T : ViewModel>(val builder: () -> ViewModel) {
    inline operator fun <reified R : T> getValue(t: R?, property: KProperty<*>): R {
        return ViewModelStore.getOrCreate<R>(builder)
    }
}

inline fun <reified T : ViewModel> viewModel(noinline builder: () -> T): ViewModelInstance<T> {
    return ViewModelInstance(builder)
}


@Composable
inline fun <reified T : ViewModel> rememberViewModel(noinline builder: () -> T) {
    return remember(builder) {
        ViewModelStore.getOrCreate<T>(builder)
    }
}