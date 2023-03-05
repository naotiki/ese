package me.naotiki.ese.core

import me.naotiki.ese.core.api.EsePlugin
import me.naotiki.ese.core.commands.Udon.Companion.fileExtension
import me.naotiki.ese.core.secure.PluginLoader
import me.naotiki.ese.core.user.UserManager
import me.naotiki.ese.core.utils.log
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.KoinTest
import org.koin.test.get
import java.io.File
import java.util.*

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
        val plugin = PluginLoader.loadPluginFromFile(file)
        plugin!!.init(um.uRoot)

    }
    @Test
    fun getAttributeFromJarTest() {
        val um = get<UserManager>()
        dir.listFiles()?.map { it.name }.log()
        val pluginFolder = dir//.resolve("plugins")
        assert(pluginFolder.exists())
        val file = pluginFolder.listFiles { dir, name -> name.endsWith(".$fileExtension") }.orEmpty().first()
        val plugin = PluginLoader.loadPluginFromFile(file)
        plugin!!.init(um.uRoot)

        //pluginClass.getDeclaredMethod("init").invoke(pluginClass.getConstructor().newInstance())
    }
}