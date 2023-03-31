import org.gradle.kotlin.dsl.support.zipTo
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask
import kotlin.reflect.jvm.internal.impl.descriptors.annotations.KotlinTarget

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.dokka") version "1.7.20"
}

val appVersion = project.properties.getOrDefault("appVersion", "0.0.0-dev").toString()
group = "me.naotiki"

version = appVersion
repositories {
    google()
    mavenCentral()
    mavenLocal()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}
kotlin {

    jvm("cui") {
    }
    jvm("gui") {
    }
    js(IR) {
        browser()
        binaries.executable()
    }
    sourceSets {
        val commonMain by getting {

            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation(project(":ese-core"))
                //implementation("me.naotiki:ese-core:0.0-B")
            }
        }

        val composeMain by creating {

            dependsOn(commonMain)
            dependencies {
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.runtime)
                implementation(compose.material)
                implementation(compose.materialIconsExtended)
                implementation("org.jetbrains.compose.components:components-resources:1.3.1")
              /*  @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)*/
            }
        }
        @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
        val guiMain by getting {
            dependsOn(composeMain)
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.preview)
                implementation(compose.uiTooling)
                implementation(compose.desktop.components.splitPane)
            }
        }

        val cuiMain by getting {
            dependencies {
                implementation("org.jline:jline-terminal-jansi:3.23.0")
                implementation("org.jline:jline:3.23.0")
            }
        }
        val jsMain by getting {
            dependsOn(composeMain)
            dependencies {
                implementation(compose.web.core)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.6.4")
            }
        }
    }
}
/*@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
dependencies {
    //project(":core")
    implementation("me.naotiki:ese-core:0.0-A")
    implementation(compose.desktop.currentOs)
    //implementation(compose.desktop.windows_x64)

    implementation(compose.desktop.components.splitPane)
    implementation(compose.preview)
    implementation(compose.uiTooling)
    implementation(compose.materialIconsExtended)
}*/
val os = System.getProperty("os.name").replace(" ", "_")
compose.experimental {
    web.application {}
}
compose.desktop {

    application {
        mainClass = "MainKt"
        jvmArgs += listOf("-Dfile.encoding=UTF-8")
        nativeDistributions {

            println(this.outputBaseDir.asFile.get().absolutePath)
            targetFormats(TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm)
            packageName = "EseLinux"
            description = "Ese Linux"
            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources").apply {
                println(this.asFile.absolutePath)
            })
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
        include("*.jar")
    })
}
tasks.withType(org.gradle.jvm.tasks.Jar::class) {
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
