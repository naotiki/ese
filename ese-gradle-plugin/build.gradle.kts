group="me.naotiki.ese.dev"
version = "1.0.0-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.8.10"
    `java-gradle-plugin`
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version "1.1.0"
}

repositories {
    mavenCentral()
}

gradlePlugin{
    plugins {
        create("esePlugin"){
            id="$group.plugin"
            implementationClass = "me.naotiki.ese.dev.plugin.EseGradlePlugin"
        }
    }
}
