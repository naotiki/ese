package core.vfs.export

import core.commands.parser.Executable
import core.vfs.ExecutableFile
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule


enum class FileType {
    Text,
    Executable
}



@Serializable
data class ExportableFile(val fileName: String, val data:ExportableData)






@Serializable
sealed class ExportableData{
    @Serializable
    data class ExecutableData<T : Executable<*>>(val className: String): ExportableData() {
        constructor(clazz: Class<T>) : this(clazz.name) {

        }

            fun get()=  ClassLoader.getSystemClassLoader().loadClass(className)
                .getDeclaredConstructor().newInstance() as Executable<*>
    }

    @Serializable
    data class TextData(val name:String):ExportableData(){

    }
}