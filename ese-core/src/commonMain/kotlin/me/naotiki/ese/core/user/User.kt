package me.naotiki.ese.core.user

import kotlinx.atomicfu.atomic
import kotlinx.datetime.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.naotiki.ese.core.vfs.Directory
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic
import kotlin.random.Random

interface AccessObject {
    val name: String
    val id: UID
}

@Serializable
@JvmInline
value class UID(
    val id: Long = generateUniqueID()
) {
    companion object {
        private val count = atomic(0)
        private val origin = LocalDateTime(2023,Month.JANUARY,1,0,0,0,0).toInstant(TimeZone.currentSystemDefault())
        @JvmStatic
        private fun generateUniqueID(): Long {
            return ((Clock.System.now().toEpochMilliseconds()-origin.toEpochMilliseconds()).shl(10) + Random.nextBits(10)).shl(12)+count.getAndIncrement().toShort()
        }
    }
}

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



