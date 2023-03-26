package me.naotiki.ese.core.secure

import me.naotiki.ese.core.api.EsePlugin
import org.objectweb.asm.ClassReader
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.jar.JarFile

object PluginLoader {
    fun loadPluginFromFile(file: File): EsePlugin? {
        val jarFile = JarFile(file)
        val className = jarFile.manifest.mainAttributes.getValue("Plugin-Class")
        return EseClassLoader(file).loadClass(className).getConstructor().newInstance() as? EsePlugin
    }
}

private class EseClassLoader(
    private val pluginFile: File,
    permissionMap: PermissionMap = defaultPermissions,
    parent: ClassLoader = getSystemClassLoader()
) :
    ClassLoader(parent) {


    private val secureClassChecker = SecureClassChecker(permissionMap)

    @Throws(ClassNotFoundException::class)
    override fun findClass(name: String): Class<*> {
        try {
            val path = name.replace('.', '/') + ".class"
            val jarInputStream = URL("jar:" + pluginFile.toURI().toURL() + "!/" + path).openStream()
            val allClassBytes: ByteArray = jarInputStream.readAllBytes()
            val classReader = ClassReader(allClassBytes)

            // クラスにデバッグ情報がある場合、
            // それを無視する
            classReader.accept(
                secureClassChecker, ClassReader.SKIP_DEBUG
            )

            if (secureClassChecker.requirePermissions.isNotEmpty()) {
                throw ClassNotFoundException(
                    "許可されていない権限:${secureClassChecker.requirePermissions}"
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

    companion object {
        //   val defaultPermission=
    }
}
