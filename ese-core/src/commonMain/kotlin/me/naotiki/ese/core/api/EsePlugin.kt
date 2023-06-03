package me.naotiki.ese.core.api

import me.naotiki.ese.core.user.User

/**
 * Interface of Ese Plugin
 */
interface EsePlugin{
    /**
     * Called when plugin is installing.
     * In this function, you can do some installation process by using [user].
     *
     * ## Warning
     *
     * You cannot execute some operation affecting the host PC, if without granted by [user].
     * For example, java.io.File, java.lang.ProcessBuilder, Calling native function.
     *
     *  Unsafe operations would be blocked by classloader.
     * @param user The user who is installing plugin
     */
    suspend fun init(user: User)
}