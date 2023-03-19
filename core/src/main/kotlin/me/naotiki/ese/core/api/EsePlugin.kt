package me.naotiki.ese.core.api

import me.naotiki.ese.core.user.User
import org.koin.core.component.KoinComponent

interface EsePlugin:KoinComponent{

    fun init(user: User)
}