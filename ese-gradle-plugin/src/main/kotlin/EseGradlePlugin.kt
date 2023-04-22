package me.naotiki.ese.dev.plugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import java.io.File

interface EseGradlePluginExtension {
    val pluginClass: Property<String>
    val pluginName: Property<String>
}

class EseGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("EseConfig",EseGradlePluginExtension::class.java)
        val jarTask=project.tasks.named("jar",Jar::class.java) {
            doFirst {
                manifest {
                    attributes(mapOf(
                        "Ese-Format-Version" to "1",
                        "Ese-Plugin-Class" to extension.pluginClass.get())
                    )
                }
            }

            }

        project.tasks.create("createEsePlugin") {
            val outDir = project.file(project.buildDir).resolve("ese")
            group = "build"
            description = "Create Ese Plugin File (Noodle File)"
            inputs.file(jarTask.get().archiveFile)
            outputs.dir(outDir)
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
                println(extension.pluginClass.get())
            }

        }

    }

    data class EsePluginConfig(val name: String, val description: String, val version: String)
}
fun Project.esePlugin(block: EseGradlePluginExtension.() -> Unit){
    configure<EseGradlePluginExtension>(block)
}