plugins {
    kotlin("jvm") version "1.8.10"
    id("org.jetbrains.dokka") version "1.7.20"
    application
}

group = "me.naotiki.ese"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}
tasks{
    run(action = {
        standardInput = System.`in`
    })
    jar{

        manifest {
        attributes("Main-Class" to "MainKt")

        }
    }
}
application{
    mainClass.set("MainKt")
}
dependencies {
    implementation(project(":core"))
    implementation("org.jline:jline:3.21.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}