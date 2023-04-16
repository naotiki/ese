import org.jetbrains.kotlin.utils.alwaysTrue

plugins {
    kotlin("jvm") version "1.8.20"
    id("org.jetbrains.dokka") version "1.7.20"
}

group = "me.naotiki"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
    jvmArgs("-Djava.security.policy")
}
interface EseGradlePluginExtension {
    val pluginClass: Property<String>
    val pluginName: Property<String>
}

class EseGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // Add the 'greeting' extension object

        val extension = project.extensions.create<EseGradlePluginExtension>("eseConfig")
        var jarFile: File? = null
        project.tasks {
            jar {
                jarFile = archiveFile.get().asFile
                manifest {
                    attributes("Plugin-Class" to extension.pluginClass.get())
                }
            }
        }
        project.task("createEsePlugin") {
            val outDir = project.file(project.buildDir).resolve("ese")
            group = "build"
            description = "Create Ese Plugin File (Noodle File)"

            outputs.dir(outDir)
            dependsOn("assemble")
           /* doFirst {
                project.delete{
                    delete(outDir)
                }
            }*/
            onlyIf(alwaysTrue<Task>())
            doLast {
               // outDir.mkdir()
                project.copy {
                    from(jarFile!!)

                    into(outDir)
                    rename { "${extension.pluginName.get()}.ndl" }
                }
                project.delete {
                    delete(jarFile!!)
                }
                println(extension.pluginClass.get())
            }

        }

    }

    data class EsePluginConfig(val name: String, val description: String, val version: String)
    companion object {
        inline fun Project.esePlugin(block: () -> Unit) {

        }

    }
}

apply<EseGradlePlugin>()

configure<EseGradlePluginExtension> {
    pluginClass.set("Main")
    pluginName.set("ExamplePlugin")
}