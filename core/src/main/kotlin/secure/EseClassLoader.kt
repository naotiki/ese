package secure

import org.objectweb.asm.ClassReader
import java.io.File
import java.io.IOException
import java.net.URL

class EseClassLoader(val jarFile: File, parent: ClassLoader = getSystemClassLoader()) : ClassLoader(parent) {
    @Throws(ClassNotFoundException::class)
    override fun findClass(name: String): Class<*> {
        try {
            val path = name.replace('.', '/') + ".class";
            val jarURL = URL("jar:" + jarFile.toURI().toURL() + "!/" + path).openStream()
            val allClassBytes: ByteArray = jarURL.readAllBytes()
                ?: throw ClassNotFoundException("Error finding and opening class")
            val classReader = ClassReader(allClassBytes)
            val classVisitor = SecureClassChecker()
            // クラスにデバッグ情報がある場合、
            // それを無視する
            classReader.accept(
                classVisitor, ClassReader.SKIP_DEBUG
            )
            if (classVisitor.execute) {
                throw ClassNotFoundException(
                    "Class cannot be loaded - contains native code"
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
}
