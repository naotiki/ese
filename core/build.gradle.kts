plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.8.0"
    id("org.jetbrains.dokka")
}

group = "me.naotiki"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.4.1")
    
    implementation("org.ow2.asm:asm:9.4")

    api("io.insert-koin:koin-core:3.3.2")
    testApi("org.junit.jupiter:junit-jupiter:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testApi("io.insert-koin:koin-test-junit5:3.3.2")
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.6.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()

    jvmArgs("-Djava.security.policy")
}
