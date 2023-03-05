package core.export

import core.user.Group
import core.user.UID
import core.vfs.Path
import kotlinx.serialization.Serializable

@Serializable
data class ExportUser(
    val name: String,
    val uid: UID,
    val groupId: UID,
    val homeDirPath: Path? = null
)
