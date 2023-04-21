package me.naotiki.ese.core.user

/**
 * Virtual User Manager
 * */
class UserManager {
    private val users = mutableListOf<User>()
    val userList get() = users.toList()

    private val groups = mutableListOf<Group>()
    val groupList get() = groups.toList()

    val rootGroup = Group(this, "root")
    val uRoot = User(this, "root", rootGroup)


    private val nullGroup = Group(this, "null")
    private val uNull = User(this, "null", nullGroup)

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