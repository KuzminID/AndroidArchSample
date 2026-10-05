plugins {
    id("androidarchsample.jvm.library")
}

dependencies {
    api(project(":core:common"))
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
}
