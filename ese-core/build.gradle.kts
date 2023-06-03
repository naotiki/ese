plugins {
    kotlin("multiplatform") //version "1.8.10"
    kotlin("plugin.serialization") version "1.8.10"
    id("org.jetbrains.dokka") version "1.8.10"
    `maven-publish`
    signing
}

group = "me.naotiki.ese"
version = "0.0.1-dev2"

repositories {
    mavenCentral()
}
val textVersion = project.properties.getOrDefault("appVersion", "0.0.1-dev").toString().trimStart('v')
val generatedDir = buildDir.resolve("generated")

afterEvaluate {
    buildDir.mkdir()
    generatedDir.mkdir()
    val s = generatedDir.resolve("AppVersion.kt")

    s.createNewFile()
    s.outputStream().writer().append(
        """
            package me.naotiki.ese.core
            const val appVersion:String = "$textVersion"
        """.trimIndent()
    ).flush()
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
        val commonMain by getting {
            kotlin {
                srcDir(buildDir.resolve("generated"))
            }
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
tasks.dokkaHtml{
    outputDirectory.set(buildDir.resolve("dokkaHtml"))
}
val dokkaHtmlJar by tasks.registering(Jar::class) {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

val sonatypeUsername: String? by project
val sonatypePassword: String? by project
publishing {
    repositories {
        maven {
            name = "sonatype"
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = sonatypeUsername
                password = sonatypePassword
            }
        }
    }
    publications {
        withType<MavenPublication>() {
            artifact(dokkaHtmlJar.get())
            pom {
                name.set("Ese Core library")
                description.set("Ese Core Multiplatform library")
                url.set("https://github.com/naotiki/Ese")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("naotiki")
                        name.set("Naotiki")
                        email.set("contact@naotiki.me")
                    }
                }
                scm {
                    url.set("https://github.com/naotiki/Ese")
                }
            }

        }
        /*create<MavenPublication>("maven") {
            groupId = "me.naotiki.ese"
            from(components["java"])
            artifactId="core"

        }*/
    }
}
signing {
    sign(publishing.publications)
}