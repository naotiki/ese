package me.naotiki.ese.core.api

import me.naotiki.ese.core.user.User

interface EsePlugin{
    suspend fun init(user: User)
}