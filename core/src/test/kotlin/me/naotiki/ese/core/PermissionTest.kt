package me.naotiki.ese.core

import me.naotiki.ese.core.utils.SugoiString.splitPair
import me.naotiki.ese.core.utils.log
import me.naotiki.ese.core.vfs.Permission.Companion.parser
import org.junit.jupiter.api.Test

class PermissionTest {
    @Test
    fun splitTest(){
        "+rwx".splitPair('u','g','o','a')

    }

    @Test
    fun parsersssss(){
        parser("ugoa+rwx")
    }
}