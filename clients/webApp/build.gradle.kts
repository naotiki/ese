import org.gradle.internal.impldep.org.bouncycastle.asn1.crmf.SinglePubInfo.web
import org.jetbrains.compose.ComposePlugin.CommonComponentsDependencies.resources
import org.jetbrains.kotlin.js.translate.context.Namer.kotlin

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

kotlin {
    js(IR) {
        browser(){
        }
        binaries.executable()
    }
    sourceSets {
        val jsMain by getting  {
            dependencies {
                implementation(projects.composeShared)
            }
            projects.composeShared.dependencyProject.kotlin.sourceSets.commonMain.get().resources.destinationDirectory.set(resources.destinationDirectory)
        }
    }
}

compose.experimental {
    web.application {}
}

