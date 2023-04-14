import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import me.naotiki.ese.core.EseSystem
import me.naotiki.ese.core.VirtualSingletonKey.Companion.key
import me.naotiki.ese.core.VirtualSingletonManager
import me.naotiki.ese.core.user.Group
import me.naotiki.ese.core.user.User
import kotlin.test.Test


class VSMTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test() = runTest {
        println(EseSystem.UserManager.userList.map { it.name })
        User(EseSystem.UserManager,"a", Group(EseSystem.UserManager,"fyguyh"))

        println(EseSystem.UserManager.userList.map { it.name })

        EseSystem.create(1.key)
        println(EseSystem.UserManager.userList.map { it.name })
        EseSystem.switch(VirtualSingletonManager.DefaultKey)
        println(EseSystem.UserManager.userList.map { it.name })
    }

}