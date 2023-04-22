import me.naotiki.ese.dev.plugin.esePlugin

plugins {
    kotlin("jvm") version "1.8.20"
    id("me.naotiki.ese.dev.plugin")
}

group = "me.naotiki"
version = "1.0-SNAPSHOT"
kotlin{
    jvmToolchain(11)
}
repositories {
    mavenCentral()
}
esePlugin {
    pluginClass.set("Main")
    pluginName.set("ExamplePlugin")
}
dependencies {
    implementation(project(":ese-core"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
    jvmArgs("-Djava.security.policy")
}