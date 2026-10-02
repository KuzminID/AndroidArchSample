plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
    implementation(libs.kotlinx.coroutines.core)
}
