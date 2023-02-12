plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.8.0"
}

group = "me.naotiki"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    api("io.insert-koin:koin-core:3.3.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    api("io.insert-koin:koin-test-junit5:3.3.2")
    implementation(kotlin("reflect"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
