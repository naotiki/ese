package me.naotiki.ese.core.secure

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.json.Json
import me.naotiki.ese.core.secure.Permissions.*
import org.objectweb.asm.Opcodes.ACC_NATIVE
import org.objectweb.asm.Type
import java.io.File
import java.nio.file.FileSystem
import java.nio.file.FileVisitor
import java.nio.file.Files

@Serializable
sealed interface InspectValue {
    @Serializable
    class AccessFlag(val flag: Int) : InspectValue

    @Serializable
    class SuperClass(val internalName: String) : InspectValue

    @Serializable
    class Owner(val internalName: String) : InspectValue

    @Serializable
    class OwnerPackage(val pacName: String) : InspectValue

    @Serializable
    class FuncCall(val owner: Owner, val funcName: String, val descriptor: String?) : InspectValue

    @Serializable
    class Field(val owner: Owner, val name: String, val putOnly: Boolean, val descriptor: String?) : InspectValue
}

@Serializable
enum class Permissions {
    FileAccess,
    Reflection,
    ClassLoad,
    ExternalExecute,
    NativeCall,
}

internal val defaultPermissions = permissions {
    permission(FileAccess) {
        inspectOwnerPackage("java.nio.file")
        inspectOwner(File::class.java)
        inspectOwner(Files::class.java)
        inspectOwner(FileVisitor::class.java)
        inspectOwner(FileSystem::class.java)
    }
    permission(Reflection) {
        inspectOwnerPackage("kotlin.reflect")
        inspectOwnerPackage("java.lang.reflect")
    }
    permission(ClassLoad) {
        inspectSuperClass(ClassLoader::class.java)
        inspectOwner(ClassLoader::class.java)
    }
    permission(ExternalExecute) {
        inspectOwner(ProcessBuilder::class.java)
        inspectFuncCall(Runtime::class.java, "exec")
    }
    permission(NativeCall) {
        inspectAccFlag(ACC_NATIVE)
    }
}

private fun main() {
    val map = defaultPermissions
    json.encodeToString(inspectSerializer, map)
}


@OptIn(ExperimentalSerializationApi::class)
val json = Json {
    prettyPrint = true
    prettyPrintIndent = "  "
}
typealias PermissionMap = Map<Permissions, List<InspectValue>>

val inspectSerializer = MapSerializer(Permissions.serializer(), ListSerializer(InspectValue.serializer()))

@PermissionDslMarker
fun permissions(block: PermissionDSL.() -> Unit): PermissionMap {
    val p = PermissionDSL()
    p.block()
    return p.createMap()
}

@PermissionDslMarker
class PermissionContext @PublishedApi internal constructor(val list: MutableList<InspectValue>) {

    fun inspectAccFlag(opcode: Int) {
        list += InspectValue.AccessFlag(opcode)
    }

    fun inspectOwner(clazz: Class<*>) {
        list += InspectValue.Owner(Type.getInternalName(clazz))
    }

    fun inspectOwnerPackage(packageName: String) {
        list += InspectValue.OwnerPackage(packageName.replace(".", "/"))
    }

    fun inspectFuncCall(clazz: Class<*>, name: String, descriptor: String? = null) {
        list += InspectValue.FuncCall(InspectValue.Owner(Type.getInternalName(clazz)), name, descriptor)
    }

    fun inspectSuperClass(clazz: Class<*>) {
        list += InspectValue.SuperClass(Type.getInternalName(clazz))
    }

    /**
     * @param writeOnly 格納のみを制限する場合はtrue
     */
    fun inspectField(
        clazz: Class<*>,
        name: String,
        writeOnly: Boolean = false,
        descriptor: String? = null
    ) {
        list += InspectValue.Field(InspectValue.Owner(Type.getInternalName(clazz)), name, writeOnly, descriptor)
    }
}

@PermissionDslMarker
class PermissionDSL @PublishedApi internal constructor() {
    @PublishedApi internal val maps: Map<Permissions, MutableList<InspectValue>> = Permissions.values().associateWith {
        mutableListOf()
    }

    inline fun permission(permission: Permissions, block: PermissionContext.() -> Unit) {
        PermissionContext(maps.getValue(permission)).block()
    }

    fun createMap(): PermissionMap = maps
}


@DslMarker
internal annotation class PermissionDslMarker