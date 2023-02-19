import org.jetbrains.compose.ComposeBuildConfig
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    //id("org.jetbrains.dokka") version "1.7.20"
}

group = "me.naotiki"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}
@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
dependencies {

    implementation(compose.desktop.currentOs)
    implementation(compose.desktop.windows_x64)
    implementation(compose.desktop.components.splitPane)
    implementation(compose.preview)
    implementation(compose.uiTooling)
    implementation(compose.materialIconsExtended)
    implementation(project(":core"))
}

tasks.withType(AbstractJPackageTask::class){

    doLast {

        val artifact=this@withType.outputs.files.singleFile.listFiles()!!.first()
        println(artifact.absolutePath)

    }
}



compose.desktop {

    application {
        mainClass = "MainKt"
        jvmArgs += listOf("-Dfile.encoding=UTF-8")
        nativeDistributions {

            println(this.outputBaseDir.asFile.get().absolutePath)
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "EseLinux"
            description = "Ese Linux"
            packageVersion = project.properties.getOrDefault("appVersion", "1.0.0").toString()
            linux {
                shortcut = true
            }
            windows {
                console = !buildTypes.release.proguard.isEnabled.getOrElse(false)
                menu = true
                shortcut = true
                dirChooser = true
            }
        }
    }
}
/*
subprojects {
    apply(plugin = "org.jetbrains.dokka")
}
*/
