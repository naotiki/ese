pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        kotlin("multiplatform") version "1.9.0"
        id("com.android.application") version "8.0.0"
        id("org.jetbrains.kotlin.android") version "1.9.0"
    }
}
rootProject.name = "ese-core"

