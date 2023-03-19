package me.naotiki.ese.core.commands


import kotlinx.coroutines.Job
import me.naotiki.ese.core.IO
import me.naotiki.ese.core.Variable
import me.naotiki.ese.core.commands.parser.ArgType
import me.naotiki.ese.core.user.User
import me.naotiki.ese.core.user.UserManager
import me.naotiki.ese.core.utils.splitArgs
import me.naotiki.ese.core.vfs.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.collections.set

class Expression : KoinComponent {
    private val fileSystem by inject<FileSystem>()
    private val io by inject<IO>()
    private val fileTree by inject<FileTree>()
    private val um by inject<UserManager>()
    private val variable by inject<Variable>()
    var currentJob: Job? = null

    internal val _commandHistory = mutableListOf<String>()
    val commandHistory get() = _commandHistory.toList()
    fun getExecutables(dir: Directory? = null, includeHidden: Boolean = true): List<ExecutableFile<*>> {
        val target = if (dir != null)
            fileTree.executableEnvPaths.plus(dir)
        else fileTree.executableEnvPaths
        return target.flatMap {
            it.getChildren(um.user,includeHidden)?.values.orEmpty()
                .filterIsInstance(ExecutableFile::class.java)
        }
    }

    fun tryResolve(cmd: String): ExecutableFile<*>? {
        /*fileTree.executableEnvPaths.forEach {
            it.children.entries.firstOrNull { (name, _) -> cmd == name }?.let { (_, f) ->
                if (f is ExecutableFile<*>) {
                    return f.executable
                }
            }
        }*/
        (getExecutables().firstOrNull { cmd == it.name } ?: fileSystem.tryResolve(Path(cmd)) as? ExecutableFile<*>
                )?.let {
                return it
            }
        return null
    }

    fun suggest(user: User, targetText: String): List<String> {
        val (exe, args) = targetText.splitArgs().let {
            tryResolve(it.first()) to it.drop(1)
        }
        if (exe == null && targetText.isNotBlank()) {
            return fileTree.executableEnvPaths.flatMap {
                it.getChildren(um.user)?.keys ?: emptyList()
            }.filter { it.startsWith(targetText) }
        }

        val (type, value) = exe?.argParser?.getNextArgTypeOrNull(args) ?: return emptyList()
        return (when (type) {
            is ArgType.Choice->{
                type.choices.map { it.toString() }
            }
            is ArgType.Executable -> {
                fileTree.executableEnvPaths.flatMap {
                    it.getChildren(um.user)?.keys ?: emptyList()
                }
            }
            is ArgType.File -> {
                fileSystem.currentDirectory.getChildren(um.user)?.keys
            }
            is ArgType.Dir -> {
                fileSystem.currentDirectory.getChildren(um.user)
                    ?.filterValues { it is Directory }?.keys
            }

            else -> {
                emptyList()
            }
        } ?: emptyList()).filter { it.startsWith(value) }

    }

    fun expressionParser(string: String): Boolean {

        val assignment = Regex("^${variable.nameRule}=")
        when {
            string.contains(assignment) -> {

                val a = string.replaceFirst(assignment, "")
                variable.map[assignment.matchAt(string, 0)!!.value.trimEnd('=')] = a
            }

            else -> return false
        }
        println(variable.map)
        return true
    }

    fun cancelJob(): Boolean {
        currentJob?.cancel() ?: return false
        return true
    }
}