package me.naotiki.ese.core.export

import kotlinx.serialization.Serializable
import me.naotiki.ese.core.user.UID
import me.naotiki.ese.core.vfs.Path

@Serializable
data class ExportUser(
    val name: String,
    val uid: UID,
    val groupId: UID,
    val homeDirPath: Path? = null
)
