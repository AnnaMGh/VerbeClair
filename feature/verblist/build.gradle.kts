import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    id("verbeclair.ktlint")
    id("verbeclair.detekt")
    id("verbeclair.jacoco")
}

kotlin {
    iosArm64()
    iosSimulatorArm64()

    android {
        namespace = "com.vamvisions.verbeclair.feature.verblist"
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        // commonTest holds Compose UI tests, so on Android it runs as an instrumented test
        // (no Robolectric): ./gradlew :feature:verblist:connectedAndroidDeviceTest
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiTooling)
        }
        commonMain.dependencies {
            implementation(project(":core:domain"))
            implementation(project(":core:presentation"))
            implementation(project(":core:designsystem"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.bundles.koin.compose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.paging.compose)
            implementation(libs.navigationevent.compose)
        }
        commonTest.dependencies {
            // The real catalogue over an in-memory verb database, for the Root integration test (task 7.5).
            implementation(project(":core:data"))
            implementation(project(":core:database"))
            implementation(libs.kotlin.test)
            implementation(libs.compose.uiTest)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
            implementation(libs.paging.testing)
        }
        getByName("androidDeviceTest").dependencies {
            implementation(libs.androidx.testRunner)
            implementation(libs.androidx.compose.uiTestManifest)
            implementation(libs.androidx.espresso.core)
        }
    }
}

compose.resources {
    publicResClass = false
    packageOfResClass = "com.vamvisions.verbeclair.feature.verblist.generated.resources"
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
