
group="me.naotiki.ese.dev"
version = "0.0.1-dev2"

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

    website.set("https://github.com/naotiki/Ese")
    vcsUrl.set("https://github.com/naotiki/Ese.git")
    plugins {
        create("esePlugin"){
            id="me.naotiki.ese.gradle-plugin"
            displayName = "Gradle Plugin for Ese Plugin Development"
            description = "A Gradle Plugin for Ese Plugin Development"
            implementationClass = "me.naotiki.ese.dev.plugin.EseGradlePlugin"
            tags.set(listOf("ese"))
        }
    }
}
publishing {
    repositories {
        maven {
            name = "localPluginRepository"
            url = uri("../local-plugin-repository")
        }
    }
}