import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    id("prism.ktlint")
    id("prism.detekt")
    id("prism.jacoco")
}

coverageFloor {
    linePercent = 58
}

kotlin {
    iosArm64()
    iosSimulatorArm64()
    // Only for the build machine: buildBundledVerbDatabase runs the real Room classes on the JVM (design D3).
    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    android {
        namespace = "com.vamvisions.verbeclair.core.database"
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
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            api(libs.room.runtime)
            api(libs.room.paging)
            api(libs.sqlite.bundled)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.paging.testing)
        }
        getByName("androidDeviceTest").dependencies {
            implementation(libs.androidx.testRunner)
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
    add("kspJvm", libs.room.compiler)
}

// ── Bundled verb dataset (A-2, design D3) ───────────────────────────────────────────────────────

private val datasetRepository = "AnnaMGh/VAMVisionsKMP"

val pinnedDatasetVersion = providers.gradleProperty("frenchVerbsDataset.version")
val datasetToken: Provider<String> =
    providers
        .fileContents(rootProject.layout.projectDirectory.file("local.properties"))
        .asText
        .map { text ->
            Properties()
                .apply {
                    load(
                        text.reader(),
                    )
                }.getProperty(DownloadFrenchVerbsDatasetTask.DATASET_TOKEN_PROPERTY)
                .orEmpty()
        }.filter { it.isNotBlank() }
        .orElse(providers.environmentVariable(DownloadFrenchVerbsDatasetTask.DATASET_TOKEN_VARIABLE))

val downloadFrenchVerbsDataset by tasks.registering(DownloadFrenchVerbsDatasetTask::class) {
    group = "verbeclair"
    description = "Downloads and checks the pinned French verbs dataset release (A-2)."
    datasetVersion.set(pinnedDatasetVersion)
    sha256.set(providers.gradleProperty("frenchVerbsDataset.sha256"))
    repository.set(datasetRepository)
    token.set(datasetToken)
    datasetFile.set(
        layout.buildDirectory.zip(pinnedDatasetVersion) { dir, version ->
            dir.file("dataset/french-verbs-dataset-v$version.json.gz")
        },
    )
}

// Output folder of buildBundledVerbDatabase: a Compose resources root, so the files land in `files/`.
val bundledVerbDatabaseDir = layout.buildDirectory.dir("bundledVerbDatabase")

val buildBundledVerbDatabase by tasks.registering(JavaExec::class) {
    group = "verbeclair"
    description = "Builds the bundled verbs.db and verbs-meta.json from the pinned dataset (A-1, design D3)."
    val jvmMain = kotlin.jvm().compilations.getByName("main")
    classpath(jvmMain.output.allOutputs, jvmMain.runtimeDependencyFiles)
    mainClass.set("com.vamvisions.verbeclair.core.database.bundle.BundledDatabaseBuilderKt")
    maxHeapSize = "1g"
    val dataset = downloadFrenchVerbsDataset.flatMap { it.datasetFile }
    val filesDir = bundledVerbDatabaseDir.map { it.dir("files") }
    inputs.file(dataset).withPropertyName("dataset").withPathSensitivity(PathSensitivity.NONE)
    outputs.dir(bundledVerbDatabaseDir).withPropertyName("bundledVerbDatabase")
    argumentProviders +=
        CommandLineArgumentProvider {
            listOf(dataset.get().asFile.absolutePath, filesDir.get().asFile.absolutePath)
        }
}

// Consumed by :core:data, which packs the files as Compose resources (design D3).
val bundledVerbDatabaseElements by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}
artifacts.add(bundledVerbDatabaseElements.name, bundledVerbDatabaseDir) {
    builtBy(buildBundledVerbDatabase)
}

tasks.named<Test>("jvmTest") {
    inputs
        .files(
            buildBundledVerbDatabase,
        ).withPropertyName("bundledVerbDatabase")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    val filesDir = bundledVerbDatabaseDir.map { it.dir("files").asFile.absolutePath }
    jvmArgumentProviders += CommandLineArgumentProvider { listOf("-DbundledVerbDatabaseDir=${filesDir.get()}") }
}
