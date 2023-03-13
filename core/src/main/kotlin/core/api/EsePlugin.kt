package core.api

import core.user.User
import org.koin.core.component.KoinComponent

interface EsePlugin:KoinComponent{

    fun init(user: User,)
}