plugins {
    id("androidarchsample.android.library")
    id("androidarchsample.android.compose")
    id("androidarchsample.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "ru.marwinka.androidarchsample.feature.characters.presentation"

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":design-system"))
    implementation(project(":feature:characters:domain"))

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.compose.material.icons.core)

    implementation(libs.coil.compose)

    implementation(libs.hilt.navigation.compose)

    testImplementation(project(":core:testing"))
    testImplementation(libs.turbine)
    testImplementation(testFixtures(project(":feature:characters:domain")))
}
