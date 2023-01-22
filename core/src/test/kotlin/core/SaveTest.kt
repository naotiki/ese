package core

import core.user.UserManager
import core.vfs.FileSystem
import core.vfs.FileTree
import org.junit.jupiter.api.BeforeEach
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.test.KoinTest

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

}