plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}
dependencies {
    compileOnly(gradleApi())
}