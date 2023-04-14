package me.naotiki.ese.core

import me.naotiki.ese.core.commands.Expression
import me.naotiki.ese.core.user.UserManager
import me.naotiki.ese.core.vfs.FileSystem
import me.naotiki.ese.core.vfs.FileTree

object EseSystem : VirtualSingletonManager() {
    val UserManager by virtualSingle { UserManager() }
    val IO by virtualSingle { IO() }
    val FileTree by virtualSingle { FileTree(inject()) }
    lateinit var ClientImpl:ClientImpl
}

object Shell : VirtualSingletonManager() {
    val Variable by virtualSingle { Variable() }
    val FileSystem by virtualSingle { FileSystem(EseSystem.UserManager.user.dir ?: EseSystem.FileTree.root) }
    val Expression by virtualSingle { Expression() }
}