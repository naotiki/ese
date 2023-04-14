import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import me.naotiki.ese.core.user.UID
import kotlin.test.Test
import kotlin.test.assertTrue


class UnitTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test() = runTest {

        val set = mutableSetOf<UID>()
        repeat(100000) {
            launch {
                val a = UID()
                assertTrue(set.add(a), "Duplicated $a in $it times")
            }
        }

    }
}