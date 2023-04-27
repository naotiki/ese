import me.naotiki.ese.dev.plugin.esePlugin

plugins {
    kotlin("jvm") version "1.8.20"

    id("me.naotiki.ese.gradle-plugin") version "0.0.1-dev2"
}

group = "me.naotiki"
version = "1.0-SNAPSHOT"
kotlin{
    jvmToolchain(11)
}
repositories {
    mavenCentral()
    mavenLocal()
}
esePlugin {
    pluginClass.set("Main")
    pluginName.set("Test")
}
dependencies {
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
    jvmArgs("-Djava.security.policy")
}
