plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka") version "1.7.20"
}

group = "me.naotiki.ese"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}
tasks {
    /*run(action = {
        standardInput = System.`in`
    })*/
    jar {
        manifest {
            attributes("Main-Class" to "MainKt")
        }
    }
}
/*application {
    mainClass.set("MainKt")
}*/
dependencies {
    implementation(project(":ese-core"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jline:jline-terminal-jansi:3.23.0")
    implementation("org.jline:jline:3.23.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}