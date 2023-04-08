package me.naotiki.ese.core.user

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.naotiki.ese.core.vfs.Directory
import kotlin.jvm.JvmInline
import kotlin.random.Random

interface AccessObject {
    val name: String
    val id: UID
}

@Serializable
@JvmInline
value class UID(
    val id: UInt = (Clock.System.now().epochSeconds *
            Random.nextBits(16).toLong()).toUInt()
)

val rootUID = UID()

/**
 * すごいのかすごくないのか
 * */
fun isSugoi(user: User) = (user.id == rootUID)


class User internal constructor(
    override val name: String, var group: Group, override val id: UID = UID(),
    @Transient
    var dir: Directory? = null
) : AccessObject {
    constructor(
        userManager: UserManager, name: String, group: Group, id: UID = UID(), dir:
        Directory? = null
    ) : this(name, group, id, dir) {
        userManager.addUser(this)
    }

    /**
     * ホームディレクトリを設定します。
     * */
    fun setHomeDir(builder: (User, Group) -> Directory) {
        dir = builder(this, group)
    }


}

@Serializable
data class Group internal constructor(
    override val name: String, override val id: UID = UID()
) : AccessObject {
    constructor(
        userManager: UserManager, name: String, id: UID = UID(),
    ) : this(name, id) {
        userManager.addGroup(this)
    }


}



