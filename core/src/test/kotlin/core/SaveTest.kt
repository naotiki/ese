package core

import core.export.EseSave
import core.export.ExportableFile
import core.user.UserManager
import core.utils.log
import core.vfs.FileTree
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.encodeToByteArray
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.KoinTest
import org.koin.test.get
import java.io.File

class SaveTest : KoinTest {
    @BeforeEach
    fun up() {
        prepareKoinInjection()
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun a() {
        val um = get<UserManager>()
        val fileTree = get<FileTree>()

        val e = Cbor.encodeToByteArray(fileTree.root.export()).log()
        File("eselinux.ex").outputStream().write(e)


        File("eselinux.exc").outputStream().write(EseSave.compress(e))
    }
    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun `圧縮テスト`(){
        val fileTree = get<FileTree>()

        val e = Cbor.encodeToByteArray(fileTree.root.export()).log()
        Cbor.decodeFromByteArray(ExportableFile.serializer(),EseSave.inflate(EseSave.compress(e))).log()
    }

}