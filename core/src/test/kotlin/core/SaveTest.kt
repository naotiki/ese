package core

import core.commands.Cat
import core.user.User
import core.user.UserManager
import core.utils.log
import core.vfs.ExecutableFile
import core.vfs.FileSystem
import core.vfs.FileTree
import core.vfs.Permission
import core.vfs.export.ExportableData
import core.vfs.export.ExportableFile
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.component.get
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

class SaveTest :KoinTest{
    @BeforeEach
    fun up(){
        prepareKoinInjection()


    }
    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun a() {
        val um =get<UserManager>()
        val e=Json.decodeFromString(ExportableFile.serializer(),Json.encodeToString(ExecutableFile(Cat(), parent = null,
            owner =um
            .uRoot, group = um
            .rootGroup,
            permission
        = Permission(0),
            hidden = false ).exportable()).log())
        when(e.data){
            is ExportableData.ExecutableData<*> -> {
                (e.data as ExportableData.ExecutableData<*>).get().generateHelpText().log()
            }
            is ExportableData.TextData -> {

            }
        }
    }

}