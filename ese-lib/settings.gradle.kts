pluginManagement {
    //includeBuild("../ese-gradle-plugin")
    repositories {
        mavenCentral()
        maven {
            name = "localPluginRepository"
            url = uri("../local-plugin-repository")
        }

    }
}

rootProject.name = "ese-lib"
