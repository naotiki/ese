import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.support.zipTo
import org.jetbrains.dokka.utilities.cast

plugins {
    kotlin("multiplatform")
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
    distribution
    id("org.beryx.runtime") version "1.12.7"
    id("org.jetbrains.compose")
    id("org.jetbrains.dokka") version "1.7.20"
}

val textVersion = project.properties.getOrDefault("appVersion", "0.0.1-dev").toString()
group = "me.naotiki"

version = textVersion
repositories {
    google()
    mavenCentral()
    mavenLocal()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {


    jvm() {
        withJava()
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
        val jvmMain by getting {

            dependsOn(composeMain)
            dependencies {
                implementation(project(":client-cui"))
                implementation(compose.desktop.currentOs)
                implementation(compose.preview)
                implementation(compose.uiTooling)
                implementation(compose.desktop.components.splitPane)
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

application {
    mainClass.set("MainKt")
    applicationName = "EseLinux"
}

//System.getProperty("jpackage.app-path");
runtime {
    jpackage {

        mainClass = "MainKt"
        imageName = "EseLinux"
        installerName = "EseLinux"
        installerOptions.addAll(listOf("--vendor", "Naotiki"))

        val currentOs = OperatingSystem.current()
        val platformDirName: String
        when {
            currentOs.isWindows -> {
                installerType = "exe"
                resourceDir = file("$rootDir/res/windows")
                outputDir = "jpackage/windows"
                platformDirName = "windows"
                appVersion = textVersion.replace("[^0-9.]".toRegex(), "")
                installerOptions.addAll(
                    listOf(
                        "--win-shortcut",
                        "--win-menu",
                        "--win-dir-chooser",
                        "--win-per-user-install"
                    )
                )
            }

            currentOs.isLinux -> {
                resourceDir = file("$rootDir/res/linux")
                val list = textVersion.split("-")
                appVersion = list.first().replace("[^0-9.]".toRegex(), "")
                installerOptions.addAll(listOf("--linux-app-release", list.drop(1).joinToString("_")))
                // installerType = "deb"
                outputDir = "jpackage/linux"
                platformDirName = "linux"
            }

            else -> throw GradleException("This platform isn't supported.")
        }
        //     installerOutputDir=buildDir.resolve("jpackage/$platformDirName")
        //   imageOutputDir=buildDir.resolve("images/$platformDirName")
    }
}



tasks {

    val createEseCui by creating(CreateStartScripts::class) {
        val scriptTempDir = projectDir.resolve("startScriptTemplate")
        windowsStartScriptGenerator.cast<TemplateBasedScriptGenerator>().template =
            resources.text.fromFile(scriptTempDir.resolve("windows.bat.tpl"))
        unixStartScriptGenerator.cast<TemplateBasedScriptGenerator>().template =
            resources.text.fromFile(scriptTempDir.resolve("unix.sh.tpl"))
        outputDir = buildDir.resolve("ese-cui-script")

        applicationName = "ese-cui"
        mainClass.set("cui.MainKt")


    }

    jpackageImage {


        dependsOn(createEseCui)
        doLast {
            println("Copy CUI Scripts")
            /* build\scriptsShadow            : startShadowScripts.get().outputDir?.path
             build\install\client-gui-shadow: distDir.asFile.path
             build\distributions            : distsDirectory.get().asFile.path
             build\jpackage\windows         : outputs.files.asPath*/
            val appImageDir = jpackageData.imageOutputDirOrDefault.resolve(jpackageData.imageNameOrDefault)
            check(appImageDir.exists())
            copy {
                from(createEseCui.outputDir)
                into(appImageDir.resolve(createEseCui.executableDir))
            }
        }
    }

    shadowJar {
        //Kotlin MultiplatformではこれがないとShadowJarのJar名を間違える
        archiveFileName.set(jar.get().archiveFile.get().asFile.nameWithoutExtension + "-all.jar")

    }

}
compose.experimental {
    web.application {}
}

compose.desktop {
/*    application {
        disableDefaultConfiguration()

        val shadowJarTask=tasks.named("shadowJar", ShadowJar::class).get()
        fromFiles(shadowJarTask.archiveFile.get(),*createEseCui.outputDir?.listFiles()!!)
        mainJar.set(shadowJarTask.archiveFile.get())
        dependsOn(shadowJarTask)
        buildTypes.release.proguard {
            obfuscate.set(true)
        }
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
                debPackageVersion = textVersion.trimStart('v')
                rpmPackageVersion = textVersion.replace("-", "_")
                shortcut = true
            }
            windows {
                packageVersion = textVersion.replace("[^0-9.]".toRegex(), "")
                console = !buildTypes.release.proguard.isEnabled.getOrElse(false)
                menu = true
                shortcut = true
                dirChooser = true
            }
        }
    }*/
}
val os = System.getProperty("os.name").replace(" ", "_")


/*tasks.withType(AbstractJPackageTask::class) {
    this.freeArgs
    doLast {
        val artifact = this@withType.outputs.files.singleFile.listFiles()!!.single()
        println(artifact.absolutePath)
    }
}*/


tasks.register<Delete>("removeArchives") {
    delete(fileTree("build/compose/binaries/main-release/app") {
        include("**/*.zip")
    })
    delete(fileTree("build/compose/jars") {
        include("*.jar")
    })
}

tasks.register("superReleaseBuild") {
    dependsOn(
        "jpackage"
    )
    doLast {
        val osFamilyName = OperatingSystem.current().familyName

        val app =
            file(runtime.jpackageData.get().imageOutputDirOrDefault.resolve(runtime.jpackageData.get().imageNameOrDefault))
        val zip = file(app.parentFile.resolve("EseLinux-$osFamilyName-$textVersion.zip"))
        zipTo(zip, app)
    }
}
