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
            }
        }
    }
}

android {
    sourceSets["main"].manifest.srcFile(file("src/main/AndroidManifest.xml"))
    compileSdk = 33
    defaultConfig {
        applicationId = "me.naotiki.ese"
        minSdk =26
        targetSdk = 33
        versionCode = 2
        versionName = "1.02"
    }
    compileOptions {

        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    namespace = "me.naotiki.ese"
}