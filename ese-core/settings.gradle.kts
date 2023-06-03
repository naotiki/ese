pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        kotlin("multiplatform") version "1.8.20"
        id("com.android.application") version "7.4.1"
        id("org.jetbrains.kotlin.android") version "1.8.0"
    }
}
rootProject.name = "ese-core"

