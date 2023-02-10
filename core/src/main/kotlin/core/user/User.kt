package core.user

import core.vfs.Directory
import kotlinx.serialization.Serializable
import kotlin.random.Random

interface AccessObject {
    val name: String
    val id: UID
}

@Serializable
@JvmInline
value class UID(
    val id: UInt = (System
        .currentTimeMillis() * Random.nextBits(16).toLong()).toUInt()
) {
    companion object
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

    var isSugoiUser=false
        private set
    fun addUser(user: User) {
        users.add(user)
    }

    fun addGroup(group: Group) {
        groups.add(group)
    }

    var user: User? = null
        private set
    fun setUser(u:User){
        user=u
    }
}
@Serializable
data class User(
    override val name: String, var group: Group, override val id: UID = UID(),var homeDir: Directory?=null
) : AccessObject {

    init {
        homeDir?.run {
            owner=this@User
            ownerGroup=group
        }
        VUM.addUser(this)
    }
    /**
    * ホームディレクトリを設定します。
    * */
    fun setHomeDir(builder:(User,Group)->Directory){
        homeDir=builder(this,group)
    }
}

@Serializable
data class Group(
    override val name: String, override val id: UID = UID()
) : AccessObject {
    init {
        VUM.addGroup(this)
    }
}



