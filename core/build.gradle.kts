plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-test-fixtures`
}

kotlin { jvmToolchain(17) }

dependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.androidx.datastore.preferences.core)
    testFixturesApi(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}

// Lets the repo-wide gate (`./gradlew testDebugUnitTest ...`) run the player core's JVM tests too.
tasks.register("testDebugUnitTest") { dependsOn(tasks.named("test")) }
