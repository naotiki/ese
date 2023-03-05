plugins {
    kotlin("jvm") version "1.8.0"
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
}
interface EseGradlePluginExtension {
    val pluginClass: Property<String>
}
class EseGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // Add the 'greeting' extension object
        val extension = project.extensions.create<EseGradlePluginExtension>("eseConfig")
        //extension.pluginClass.convention("Hello from GreetingPlugin")
        var jarFile:File?=null
        project.tasks{
            jar{
                jarFile=archiveFile.get().asFile
                manifest {
                    attributes("Plugin-Class" to extension.pluginClass.get())
                }
            }
        }
            project.task("createEsePlugin") {
                group="build"
                description="Create Ese Plugin File (Noodle File)"
                dependsOn("jar")
                doFirst {
                }
                doLast {
                    project.copy {
                        from(jarFile!!)
                        into(jarFile!!.parentFile)
                        rename { it.replaceAfterLast(".","ndl") }
                    }
                    project.delete {
                        delete(jarFile!!)
                    }
                    println(extension.pluginClass.get())
                }

        }

    }
    data class EsePluginConfig(val name:String,val description:String,val version:String)
    companion object{
        inline fun Project.esePlugin(block:()->Unit){

        }

    }
}

apply<EseGradlePlugin>()

configure<EseGradlePluginExtension> {
    pluginClass.set("Main")
}