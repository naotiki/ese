package me.naotiki.ese.core.export

import kotlinx.serialization.Serializable
import me.naotiki.ese.core.commands.parser.Executable
import me.naotiki.ese.core.user.UID
import me.naotiki.ese.core.vfs.Permission
import kotlin.reflect.KClass


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
        constructor(clazz: KClass<T>) : this(clazz.qualifiedName!!)

        fun createExecutable() = ClassLoader.getSystemClassLoader().loadClass(className)
            .getDeclaredConstructor().newInstance() as Executable<*>
    }

    @Serializable
    data class TextData(val content: String) : ExportableData()

    @Serializable
    data class DirectoryData(val children: List<ExportableFile>) : ExportableData()
}