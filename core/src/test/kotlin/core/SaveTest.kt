package core

import core.utils.DirectoryAsStringSerializer
import core.utils.format
import core.vfs.Directory
import core.vfs.File
import core.vfs.FireTree
import core.vfs.VFS
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SaveTest {
    @BeforeEach
    fun up(){
        Vfs = VFS(FireTree.root)
    }
    @Test
    fun `ディレクトリシリアライズテスト`(){
        val a= format.encodeToString(PolymorphicSerializer(File::class),FireTree.root)
        println(a)
    }
}