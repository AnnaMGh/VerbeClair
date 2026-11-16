import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    id("verbeclair.ktlint")
    id("verbeclair.detekt")
    id("verbeclair.jacoco")
}

kotlin {
    iosArm64()
    iosSimulatorArm64()

    android {
        namespace = "com.vamvisions.verbeclair.core.data"
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
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            implementation(project(":core:database"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.components.resources)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.datastore.preferences)
            implementation(libs.okio)
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
            implementation(libs.paging.testing)
        }
        getByName("androidDeviceTest").dependencies {
            implementation(libs.androidx.testRunner)
        }
    }
}

// The bundled verbs.db and verbs-meta.json, built by :core:database (design D3).
val bundledVerbDatabase by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    bundledVerbDatabase(project(path = ":core:database", configuration = "bundledVerbDatabaseElements"))
}

compose.resources {
    publicResClass = false
    packageOfResClass = "com.vamvisions.verbeclair.core.data.generated.resources"
    customDirectory(
        sourceSetName = "commonMain",
        directoryProvider = layout.dir(bundledVerbDatabase.elements.map { it.single().asFile }),
    )
}
