rootProject.name = "client-cui"
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        kotlin("jvm").version("1.8.10")
    }
}
includeFlat("ese-core")