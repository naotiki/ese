import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileCommon
import java.awt.SystemColor.text

plugins {
    kotlin("multiplatform") //version "1.8.10"
    kotlin("plugin.serialization") version "1.8.10"
}

group = "me.naotiki"
version = "0.0-B"

repositories {
    mavenCentral()
}
val textVersion = project.properties.getOrDefault("appVersion", "0.0.1-dev").toString().trimStart('v')
val generatedDir=buildDir.resolve("generated")
val generateSource: Task by tasks.creating{
    inputs.property("version",textVersion)
    outputs.dir(generatedDir)
    doLast {
        val s=generatedDir.resolve("AppVersion.kt")
        s.createNewFile()
        s.outputStream().writer().append("""
            package me.naotiki.ese.core
            const val appVersion:String = "$textVersion"
        """.trimIndent()).flush()
    }
}

afterEvaluate {
    buildDir.mkdir()
    generatedDir.mkdir()
    val s=generatedDir.resolve("AppVersion.kt")

    s.createNewFile()
    s.outputStream().writer().append("""
            package me.naotiki.ese.core
            const val appVersion:String = "$textVersion"
        """.trimIndent()).flush()
}
/*tasks{
    withType(JavaCompile::class).all{
        dependsOn(generateSource)
    }
    withType(KotlinCompile::class).all{
        dependsOn(generateSource)
    }
}*/


kotlin {
    jvm {
        jvmToolchain(11)
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
    }
    //ktx-serialization ktx-coroutine のWASMサポートを待つ
    /*@OptIn(org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl::class)
    wasm{
        binaries.executable()
        browser {
        }
        applyBinaryen()
    }*/
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    /* val nativeTarget = when {
         hostOs == "Mac OS X" -> macosX64("native")
         hostOs == "Linux" -> linuxX64("native")
         isMingwX64 -> mingwX64("native")
         else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
     }*/


    sourceSets {
        val generated by creating{
            kotlin{
                srcDir(buildDir.resolve("generated"))
            }
        }
        val commonMain by getting {
            dependsOn(generated)
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.4.1")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                implementation("org.jetbrains.kotlinx:atomicfu:0.20.1")
                implementation(kotlin("reflect"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("org.ow2.asm:asm:9.4")
            }
        }
        val jvmTest by getting
        val jsMain by getting
        val jsTest by getting
        /*    val nativeMain by getting
            val nativeTest by getting*/
    }
}
