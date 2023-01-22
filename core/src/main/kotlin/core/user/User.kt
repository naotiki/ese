package core.user

import core.vfs.Directory
import kotlin.random.Random

interface AccessObject {
    val name: String
    val id: UID
}


@JvmInline
value class UID(
    val id: UInt = (System
        .currentTimeMillis() * Random.nextBits(16).toLong()).toUInt()
) {
    companion object {


    }
}


/**
 * Virtual User Manager
 * */
object VUM {
    private val users = mutableListOf<User>()
    val userList get() = users.toList()

    private val groups = mutableListOf<Group>()
    val groupList get() = groups.toList()

    val rootGroup = Group("root")
    val uRoot = User("root", rootGroup)

    val naotikiGroup = Group("naotiki")
    val uNaotiki = User("naotiki", naotikiGroup)

    fun addUser(user: User) {
        users.add(user)
    }

    fun addGroup(group: Group) {
        groups.add(group)
    }
}

data class User(
    override val name: String, var group: Group,var homeDir:Directory?=null, override val id: UID = UID()
) : AccessObject {
    init {
        homeDir?.run {
            owner=this@User
            ownerGroup=group
        }
        VUM.addUser(this)
    }
}

data class Group(
    override val name: String, override val id: UID = UID()
) : AccessObject {
    init {
        VUM.addGroup(this)
    }
}



