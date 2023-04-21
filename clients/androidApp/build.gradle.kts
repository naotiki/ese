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
val textVersion = project.properties.getOrDefault("appVersion", "0.0.1-dev").toString()
val (major,minor,patch)=textVersion.replace("[^0-9.]".toRegex(), "").split(".").map { it.toInt() }
android {
    sourceSets["main"].manifest.srcFile(file("src/main/AndroidManifest.xml"))
    compileSdk = 33
    defaultConfig {
        applicationId = "me.naotiki.ese"
        minSdk =26
        targetSdk = 33
        versionCode = major*10000+minor*100+patch
        versionName = textVersion
    }
    compileOptions {

        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    namespace = "me.naotiki.ese"
}