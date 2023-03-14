package secure

import org.objectweb.asm.ClassReader
import java.io.File
import java.io.IOException
import java.net.URL

class EseClassLoader(private val jarFile: File,private val permissionMap:PermissionMap= defaultPermissions, parent: ClassLoader =
    getSystemClassLoader()) :
    ClassLoader(parent) {
    @Throws(ClassNotFoundException::class)
    override fun findClass(name: String): Class<*> {
        try {
            val path = name.replace('.', '/') + ".class"
            val jarInputStream = URL("jar:" + jarFile.toURI().toURL() + "!/" + path).openStream()
            val allClassBytes: ByteArray = jarInputStream.readAllBytes()
            val classReader = ClassReader(allClassBytes)
            val secureClassChecker = SecureClassChecker(permissionMap)
            // クラスにデバッグ情報がある場合、
            // それを無視する
            classReader.accept(
                secureClassChecker, ClassReader.SKIP_DEBUG
            )

            if (secureClassChecker.requirePermissions.isNotEmpty()) {
                throw ClassNotFoundException(
                    "Class cannot be loaded - contains illegal code"
                )
            } else {
                return defineClass(
                    null, allClassBytes, 0,
                    allClassBytes.size
                )
            }
        } catch (e: IOException) {
            throw ClassNotFoundException(
                "Error finding and opening class", e
            )
        }
    }
    companion object{
     //   val defaultPermission=
    }
}
