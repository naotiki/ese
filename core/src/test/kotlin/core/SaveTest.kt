package core

import org.junit.jupiter.api.BeforeEach
import org.koin.test.KoinTest

class SaveTest : KoinTest {
    @BeforeEach
    fun up() {
        prepareKoinInjection()
    }

 /*   @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun a() {
        val fileTree = get<FileTree>()

        val e = Cbor.encodeToByteArray(fileTree.root.export()).log()
        File("eselinux.ex").outputStream().write(e)


        File("eselinux.exc").outputStream().write(EseSave.compress(e))
    }*/
    /*@OptIn(ExperimentalSerializationApi::class)
    @Test
    fun `圧縮テスト`(){
        val fileTree = get<FileTree>()

        val e = Cbor.encodeToByteArray(fileTree.root.export()).log()
        Cbor.decodeFromByteArray(ExportableFile.serializer(),EseSave.inflate(EseSave.compress(e))).log()
    }*/

}