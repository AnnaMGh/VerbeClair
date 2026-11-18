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
        namespace = "com.vamvisions.verbeclair.core.designsystem"
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
        // (no Robolectric): ./gradlew :core:designsystem:connectedAndroidDeviceTest
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
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.compose.uiTest)
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
    packageOfResClass = "com.vamvisions.verbeclair.core.designsystem.generated.resources"
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
