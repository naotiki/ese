package me.naotiki.ese.core

import me.naotiki.ese.core.commands.Expression
import me.naotiki.ese.core.user.UserManager
import me.naotiki.ese.core.vfs.FileSystem
import me.naotiki.ese.core.vfs.FileTree

/**
 * Ese System Instances.
 */
object EseSystem : VirtualSingletonManager() {
    val UserManager by virtualSingle { UserManager() }
    val IO by virtualSingle { IO() }
    val FileTree by virtualSingle { FileTree() }
    lateinit var ClientImpl:ClientImpl
        internal set
}

object Shell : VirtualSingletonManager() {
    val Variable by virtualSingle { Variable() }
    val FileSystem by virtualSingle { FileSystem(EseSystem.UserManager.user.dir ?: EseSystem.FileTree.root) }
    val Expression by virtualSingle { Expression() }
}