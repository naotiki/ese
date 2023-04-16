plugins {
    kotlin("multiplatform")
    id("com.android.application")
    id("org.jetbrains.compose")
}

kotlin {
    android(){
        jvmToolchain(11)
    }
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(projects.composeShared)
                api("androidx.activity:activity-compose:1.7.0")
                api("androidx.appcompat:appcompat:1.6.1")
                api("androidx.core:core-ktx:1.10.0")
                implementation("androidx.compose.foundation:foundation:1.4.1")
                implementation("androidx.compose.ui:ui:1.4.1")
            }
        }
    }
}

android {
    sourceSets["main"].manifest.srcFile(file("src/main/AndroidManifest.xml"))
    compileSdk = 33
    defaultConfig {
        applicationId = "me.naotiki.ese"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}