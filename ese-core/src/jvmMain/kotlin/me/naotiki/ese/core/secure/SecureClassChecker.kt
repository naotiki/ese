package me.naotiki.ese.core.secure

import me.naotiki.ese.core.secure.InspectValue.*
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*

class SecureClassChecker(map: PermissionMap) : ClassVisitor(ASM5) {
    private val acc: MutableMap<AccessFlag, Permissions> = mutableMapOf()
    private val fie: MutableMap<Field, Permissions> = mutableMapOf()
    private val func: MutableMap<FuncCall, Permissions> = mutableMapOf()
    private val own: MutableMap<Owner, Permissions> = mutableMapOf()
    private val pac: MutableMap<OwnerPackage, Permissions> = mutableMapOf()
    private val sup: MutableMap<SuperClass, Permissions> = mutableMapOf()

    private val _requirePermissions= mutableSetOf<Permissions>()
    val requirePermissions:Set<Permissions> get() = _requirePermissions
    init {
        map.forEach { (k, v) ->
            v.forEach {
                when (it) {
                    is AccessFlag -> acc[it] = k
                    is Field -> fie[it] = k
                    is FuncCall -> func[it] = k
                    is Owner -> own[it] = k
                    is OwnerPackage -> pac[it] = k
                    is SuperClass -> sup[it] = k
                }
            }
        }
    }

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        sup.forEach { (t, u) ->
            if (t.internalName==superName){
                println("$name:$superName")
                _requirePermissions.add(u)
            }
        }
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(
        access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?
    ): MethodVisitor {
        acc.forEach { (t, u) ->
            if (access and t.flag > 0) {
                _requirePermissions.add(u)
                println("$name:$descriptor")
            }
        }
        return object : MethodVisitor(ASM5) {
            override fun visitFieldInsn(opcode: Int, owner: String, name: String?, descriptor: String?) {

                fie.forEach { (t, u) ->
                    if (t.owner.internalName == owner && t.name == name &&
                        t.descriptor?.equals(descriptor) != false
                        && (opcode and PUTSTATIC > 0 || opcode and PUTFIELD > 0 ||
                                (opcode and GETSTATIC > 0 || opcode and GETFIELD > 0) && !t.putOnly)
                    ) {
                        println("$owner.$name:$descriptor")
                        _requirePermissions.add(u)
                    }
                }
                own.forEach { (t, u) ->
                    if (t.internalName == owner) {
                        println("$owner.$name:$descriptor")
                        _requirePermissions.add(u)
                    }
                }
                pac.forEach {(t,u)->
                    if (owner.startsWith(t.pacName)) {
                        println("$owner.$name:$descriptor")
                        _requirePermissions.add(u)
                    }
                }
                super.visitFieldInsn(opcode, owner, name, descriptor)
            }

            override fun visitMethodInsn(
                opcode: Int, owner: String, name: String?, descriptor: String?, isInterface: Boolean
            ) {
                func.forEach {(t,u)->
                    if (t.owner.internalName==owner&&t.funcName==name&&t.descriptor?.equals(descriptor)!=false){
                        println("$owner.$name:$descriptor")
                        _requirePermissions.add(u)
                    }
                }
                own.forEach { (t, u) ->
                    if (t.internalName == owner) {
                        println("$owner.$name:$descriptor")
                        _requirePermissions.add(u)
                    }
                }
                pac.forEach {(t,u)->
                    if (owner.startsWith(t.pacName)) {
                        println("$owner.$name:$descriptor")
                        _requirePermissions.add(u)
                    }
                }
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
            }
        }
    }
}