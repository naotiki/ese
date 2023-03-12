package core

import core.utils.SugoiString
import core.utils.SugoiString.splitPair
import core.utils.log
import core.vfs.Permission.Companion.parser
import org.junit.jupiter.api.Test

class PermissionTest {
    @Test
    fun splitTest(){
        "+rwx".splitPair('u','g','o','a').log()

    }

    @Test
    fun parsersssss(){
        parser("ugoa+rwx")
    }
}