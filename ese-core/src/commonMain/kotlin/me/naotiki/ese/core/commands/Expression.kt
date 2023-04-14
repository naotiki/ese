package me.naotiki.ese.core.commands


import kotlinx.coroutines.Job
import me.naotiki.ese.core.EseSystem.FileTree
import me.naotiki.ese.core.EseSystem.UserManager
import me.naotiki.ese.core.Shell.FileSystem
import me.naotiki.ese.core.Shell.Variable
import me.naotiki.ese.core.commands.parser.ArgType
import me.naotiki.ese.core.utils.splitArgs
import me.naotiki.ese.core.vfs.*
import kotlin.collections.set

class Expression  {

    var currentJob: Job? = null
    fun cancelJob(): Boolean {
        currentJob?.cancel() ?: return false
        return true
    }
    private val _commandHistory = mutableListOf<String>()
    fun addHistory(string: String){
        _commandHistory.add(0,string)
    }
    val commandHistory :List<String> get() = _commandHistory
    fun getExecutables(dir: Directory? = null, includeHidden: Boolean = true): List<ExecutableFile<*>> {
        val target = if (dir != null)
            FileTree.executableEnvPaths.plus(dir)
        else FileTree.executableEnvPaths
        return target.flatMap {
            it.getChildren(UserManager.user, includeHidden)?.values.orEmpty()
                .filterIsInstance<ExecutableFile<*>>()
        }
    }

    fun tryResolve(cmd: String): ExecutableFile<*>? {
        (getExecutables().firstOrNull { cmd == it.name } ?: FileTree.tryResolve(Path(cmd)) as? ExecutableFile<*>
                )?.let {
                return it
            }
        return null
    }

    fun suggest(targetText: String): List<String> {
        val (exe, args) = targetText.splitArgs().let {
            tryResolve(it.first()) to it.drop(1)
        }
        if (exe == null && targetText.isNotBlank()) {
            return FileTree.executableEnvPaths.flatMap {
                it.getChildren(UserManager.user)?.keys ?: emptyList()
            }.filter { it.startsWith(targetText) }
        }

        val (type, value) = exe?.argParser?.getNextArgTypeOrNull(args) ?: return emptyList()
        return (when (type) {
            is ArgType.Choice -> {
                type.choices.map { it.toString() }
            }

            is ArgType.Executable -> {
                FileTree.executableEnvPaths.flatMap {
                    it.getChildren(UserManager.user)?.keys ?: emptyList()
                }
            }

            is ArgType.File -> {
                FileSystem.currentDirectory.getChildren(UserManager.user)?.keys
            }

            is ArgType.Dir -> {
                FileSystem.currentDirectory.getChildren(UserManager.user)
                    ?.filterValues { it is Directory }?.keys
            }

            else -> {
                emptyList()
            }
        } ?: emptyList()).filter { it.startsWith(value) }

    }

    fun expressionParser(string: String): Boolean {

        val assignment = Regex("^${Variable.nameRule}=")
        when {
            string.contains(assignment) -> {

                val a = string.replaceFirst(assignment, "")
                Variable.map[assignment.matchAt(string, 0)!!.value.trimEnd('=')] = a
            }

            else -> return false
        }
        return true
    }

}