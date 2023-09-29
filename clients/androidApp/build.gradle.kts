plugins {
    kotlin("multiplatform")
    id("com.android.application")
    id("org.jetbrains.compose")
}

kotlin {
    androidTarget() {
        jvmToolchain(11)
    }
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(projects.composeShared)
                val nav_version = "2.5.3"
                implementation("androidx.navigation:navigation-compose:$nav_version")
            }
        }
    }
}
val textVersion = project.properties.getOrDefault("appVersion", "0.0.1-dev").toString().trimStart('v')
val (major, minor, patch) = textVersion.replace("[^0-9.]".toRegex(), "").split(".").map { it.toInt() }
android {
    sourceSets["main"].manifest.srcFile(file("src/main/AndroidManifest.xml"))
    compileSdk = 33
    defaultConfig {
        applicationId = "me.naotiki.ese"
        minSdk = 26
        targetSdk = 33
        versionCode = major * 10000 + minor * 100 + patch
        versionName = textVersion
    }
    compileOptions {

        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    val debugSign by signingConfigs.creating() {
        storeFile = file("keys/debug.keystore")
        storePassword = "android"
        keyAlias = "androiddebugkey"
        keyPassword = "android"
    }
    buildTypes{
        release {
            signingConfig=debugSign
        }
    }

    namespace = "me.naotiki.ese"
}