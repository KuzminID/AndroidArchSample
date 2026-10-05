plugins {
    id("androidarchsample.jvm.library")
    `java-test-fixtures`
}

dependencies {
    // api: AppResult and Flow are part of the repository contracts
    api(project(":core:common"))

    testFixturesApi(project(":core:common"))

    testImplementation(project(":core:testing"))
    testImplementation(libs.turbine)
}
