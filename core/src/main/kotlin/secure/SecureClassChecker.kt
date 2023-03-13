package secure

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.ACC_NATIVE
import org.objectweb.asm.Type
import java.io.File

class SecureClassChecker : ClassVisitor(Opcodes.ASM5) {
    var fileAccess = false
        private set
    var nativeCall = false
        private set
    var execute = false
        private set
    var reflection = false
        private set

    override fun visitMethod(
        access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?
    ): MethodVisitor {
        //ネイティブ呼び出し
        if (access and ACC_NATIVE > 0) {
            nativeCall = true
        }
        return object : MethodVisitor(Opcodes.ASM5) {
            override fun visitMethodInsn(
                opcode: Int, owner: String, name: String?, descriptor: String?, isInterface: Boolean
            ) {
                if (owner == Type.getInternalName(File::class.java)) {
                    fileAccess = true
                }
                if (owner.startsWith("kotlin/reflect") || owner.startsWith("java/lang/reflect")) {
                    reflection = true
                }
                if (owner == Type.getInternalName(Runtime::class.java)&& name == "exec") {
                    execute = true
                }
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
            }
        }
    }
}