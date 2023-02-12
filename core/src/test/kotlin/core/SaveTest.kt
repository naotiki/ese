package core

import core.user.UserManager
import core.utils.format
import core.vfs.File
import core.vfs.FileSystem
import core.vfs.FileTree
import kotlinx.serialization.PolymorphicSerializer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.math.sin

class SaveTest :KoinTest{
    @BeforeEach
    fun up(){
        startKoin {
            module {
                single { UserManager() }
                single { FileTree(get()) }
                single { FileSystem(get()) }
            }
        }
    }
    @Test
    fun `ディレクトリシリアライズテスト`(){
        val a= format.encodeToString(PolymorphicSerializer(File::class),get())
        println(a)
    }
}