plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    compileOnly(libs.gradlePlugin.android)
    compileOnly(libs.gradlePlugin.kotlin)
    compileOnly(libs.gradlePlugin.kotlinCompose)
    compileOnly(libs.gradlePlugin.ksp)
    compileOnly(libs.gradlePlugin.hilt)

    testImplementation(libs.junit)
}
