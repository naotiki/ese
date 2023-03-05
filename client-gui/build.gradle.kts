import org.gradle.kotlin.dsl.support.zipTo
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.dokka") version "1.7.20"
}

val appVersion = project.properties.getOrDefault("appVersion", "0.0.0-dev").toString()
group = "me.naotiki"

version = appVersion
repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
dependencies {
    implementation(project(":core"))
    implementation(compose.desktop.currentOs)
    implementation(compose.desktop.windows_x64)
    implementation(compose.desktop.components.splitPane)
    implementation(compose.preview)
    implementation(compose.uiTooling)
    implementation(compose.materialIconsExtended)
}
val os = System.getProperty("os.name").replace(" ", "_")
compose.desktop {

    application {
        mainClass = "MainKt"
        jvmArgs += listOf("-Dfile.encoding=UTF-8")
        nativeDistributions {

            println(this.outputBaseDir.asFile.get().absolutePath)
            targetFormats(TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm)
            packageName = "EseLinux"
            description = "Ese Linux"
            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))
            linux {
                debPackageVersion = appVersion.trimStart('v')
                rpmPackageVersion = appVersion.replace("-", "_")
                shortcut = true
            }
            windows {
                packageVersion = appVersion.replace("[^0-9.]".toRegex(), "")
                console = !buildTypes.release.proguard.isEnabled.getOrElse(false)
                menu = true
                shortcut = true
                dirChooser = true
            }
        }
    }
}

tasks.withType(AbstractJPackageTask::class) {
    doLast {
        val artifact = this@withType.outputs.files.singleFile.listFiles()!!.single()
        println(artifact.absolutePath)
    }
}


tasks.register<Delete>("removeArchives") {
    delete(fileTree("build/compose/binaries/main-release/app") {
        include("**/*.zip")
    })
    delete(fileTree("build/compose/jars") {
        include ("*.jar")
    })
}
tasks.withType(org.gradle.jvm.tasks.Jar::class){
    mustRunAfter("removeArchives")
}
tasks.register("superReleaseBuild") {

    dependsOn(
        "removeArchives",
        "packageReleaseUberJarForCurrentOS",
        "packageReleaseDistributionForCurrentOS",
        "createReleaseDistributable"
    )
    doLast {
        val app = file("build/compose/binaries/main-release/app")
        val zip = file(app.toPath().resolve("EseLinux-$os-$appVersion.zip"))
        zipTo(zip, app.listFiles()!!.single())
    }
}
