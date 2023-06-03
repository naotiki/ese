package me.naotiki.ese.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.properties
import org.gradle.kotlin.dsl.register
import java.io.File

interface EseGradlePluginExtension {
    val pluginClass: Property<String>
    val pluginName: Property<String>
}

class EseGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.dependencies {
            add(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME, "$eseCore:$eseVersion")
        }
        val extension = project.extensions.create("EseConfig", EseGradlePluginExtension::class.java)
        val jarTask = project.tasks.named("jar", Jar::class.java) {
            doFirst {
                manifest {
                    attributes(
                        mapOf(
                            "Ese-Format-Version" to "1",
                            "Ese-Plugin-Class" to extension.pluginClass.get()
                        )
                    )
                }
            }

        }

        val createEsePlugin = project.tasks.create("createEsePlugin") {
            val outDir = project.file(project.buildDir).resolve("ese")
            group = "build"
            description = "Create Ese Plugin File (Noodle File)"
            inputs.file(jarTask.get().archiveFile)
            if (extension.pluginName.orNull!=null){
                outputs.file(outDir.resolve(extension.pluginName.get()+".ndl"))
            }
            dependsOn("assemble")
            /* doFirst {
                 project.delete{
                     delete(outDir)
                 }
             }*/
            doLast {
                // outDir.mkdir()
                project.copy {
                    from(jarTask.get().archiveFile)
                    into(outDir)
                    rename { "${extension.pluginName.get()}.ndl" }
                }
                /*project.delete {
                    delete(jarTask.get().archiveFile)
                }*/
            }

        }
        project.tasks.register<RunEsePluginTask>("runEsePlugin") {
            dependsOn(createEsePlugin)
            inputs.file(createEsePlugin.outputs.files.singleFile)
        }

    }

    data class EsePluginConfig(val name: String, val description: String, val version: String)
}

fun Project.esePlugin(block: EseGradlePluginExtension.() -> Unit) {
    configure<EseGradlePluginExtension>(block)
}