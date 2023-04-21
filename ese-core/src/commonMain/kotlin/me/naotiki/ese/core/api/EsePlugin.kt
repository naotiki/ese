package me.naotiki.ese.core.api

import me.naotiki.ese.core.user.User

interface EsePlugin{
    fun init(user: User)
}