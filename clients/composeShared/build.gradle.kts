plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
}

group = "me.naotiki"
repositories {
    google()
    mavenCentral()
    mavenLocal()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    android(){
        jvmToolchain(11)
    }
    jvm("desktop")
    js(IR) {
        browser()
        binaries.executable()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                api(project(":ese-core"))
                api(compose.ui)
                api(compose.foundation)
                api(compose.runtime)
                api(compose.material)
                api(compose.materialIconsExtended)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                api(compose.components.resources)
            }
        }


        val desktopMain by getting {

            dependencies {
                api(compose.desktop.currentOs)
                implementation(compose.preview)
                implementation(compose.uiTooling)
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.6.4")
            }
        }
        val androidMain by getting {
            dependencies {
                api("androidx.activity:activity-compose:1.7.0")
                api("androidx.appcompat:appcompat:1.6.1")
                api("androidx.core:core-ktx:1.10.0")
            }
        }
    }
}

android {
    compileSdk = 33
   // sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
   // sourceSets["main"].res.srcDirs("src/androidMain/res")
   // sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = 26
        targetSdk = 33
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    namespace = "me.naotiki.ese.shared"
}