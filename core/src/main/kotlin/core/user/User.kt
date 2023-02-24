package core.user

import core.export.ExportUser
import core.vfs.Directory
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.random.Random

interface AccessObject {
    val name: String
    val id: UID
}

@Serializable
@JvmInline
value class UID(
    val id: UInt = (System.currentTimeMillis() *
            Random.nextBits(16).toLong()).toUInt()
) {
}

val rootUID = UID()

/**
 * Virtual User Manager
 * */
class UserManager {
    private val users = mutableListOf<User>()
    val userList get() = users.toList()

    private val groups = mutableListOf<Group>()
    val groupList get() = groups.toList()

    val rootGroup = Group(this, "root")
    val uRoot = User(this, "root", rootGroup, rootUID)


    val nullGroup = Group(this, "null")
    val uNull = User(this, "null", nullGroup)

    val naotikiGroup = Group(this, "naotiki")
    val uNaotiki = User(this, "naotiki", naotikiGroup)

    fun addUser(user: User) {
        users.add(user)
    }

    fun addGroup(group: Group) {
        groups.add(group)
    }

    var user: User = uNull
        private set

    fun setUser(u: User) {
        user = u
    }
}

/**
 * すごいのかすごくないのか
 * */
fun isSugoi(user: User) = (user.id == rootUID).also { println(user.id.toString() + "==" + rootUID) }


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

    fun export() = ExportUser(name, id, group.id, dir?.getFullPath())
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



