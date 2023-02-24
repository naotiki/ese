package core.export

import core.commands.parser.Executable
import core.user.UID
import core.vfs.Permission
import kotlinx.serialization.Serializable


@Serializable
data class ExportableFile(
    val fileName: String,
    val data: ExportableData,
    val userID:UID,
    val groupID:UID,
    val permission: Permission,
    val hidden: Boolean,
)


/**
 * エクスポートされるデータ
 * */
@Serializable
sealed class ExportableData {
    @Serializable
    data class ExeData<T : Executable<*>>(val className: String) : ExportableData() {
        constructor(clazz: Class<T>) : this(clazz.name)

        fun createExecutable() = ClassLoader.getSystemClassLoader().loadClass(className)
            .getDeclaredConstructor().newInstance() as Executable<*>
    }

    @Serializable
    data class TextData(val content: String) : ExportableData()

    @Serializable
    data class DirectoryData(val children: List<ExportableFile>) : ExportableData()
}