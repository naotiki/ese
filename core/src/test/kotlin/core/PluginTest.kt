package core

import core.api.EsePlugin
import core.commands.Udon.Companion.fileExtension
import core.user.UserManager
import core.utils.log
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.KoinTest
import org.koin.test.get
import secure.EseClassLoader
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.security.CodeSource
import java.security.Permission
import java.security.PermissionCollection
import java.util.*
import java.util.jar.JarFile

private val dir = File(System.getProperty("compose.application.resources.dir") ?: "../ese-lib/build/ese")

class PluginTest : KoinTest {

    @BeforeEach
    fun up() {
        prepareKoinInjection()
    }

    @Test
    fun a(){
        ServiceLoader.load(EsePlugin::class.java)
        val um = get<UserManager>()
        val file = dir.listFiles { dir, name -> name.endsWith(".$fileExtension") }.orEmpty().first()
        EsePlugin::class.java.getMethod("")
        val plugin = EseClassLoader(file).loadClass("Main").getConstructor().newInstance() as EsePlugin
        plugin.init(um.uRoot)
    }
    @Test
    fun getAttributeFromJarTest() {
        val um = get<UserManager>()
        dir.listFiles()?.map { it.name }.log()
        val pluginFolder = dir//.resolve("plugins")
        assert(pluginFolder.exists())
        val file = pluginFolder.listFiles { dir, name -> name.endsWith(".$fileExtension") }.orEmpty().first()

        val jarFile = JarFile(file)
        val targetClassName = jarFile.manifest.mainAttributes.getValue("Plugin-Class")

        val allDeny = object : PermissionCollection() {
            override fun add(permission: Permission?) {}

            override fun implies(permission: Permission?): Boolean = false
            override fun elements(): Enumeration<Permission> =
                object : Enumeration<Permission> {
                    override fun hasMoreElements(): Boolean = false
                    override fun nextElement(): Permission = throw NoSuchElementException()
                }
        }

        class SuperLoader(urls: Array<out URL>?) : URLClassLoader(urls) {
            override fun getPermissions(codesource: CodeSource?): PermissionCollection {
               return allDeny
            }
        }

        val child = SuperLoader(
            arrayOf<URL>(file.toURI().toURL()),
        )

        val pluginClass = child.loadClass(targetClassName)
        val plugin = pluginClass.getConstructor().newInstance() as EsePlugin
        plugin.init(um.uRoot)

        //pluginClass.getDeclaredMethod("init").invoke(pluginClass.getConstructor().newInstance())
    }
}