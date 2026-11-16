plugins {
    // Applied directly rather than through a convention plugin: these are plain
    // Kotlin/JVM tooling modules, not app modules.
    `java-library`
    id("org.jetbrains.kotlin.jvm")
    // Coverage is opted into explicitly here, because nothing else applies it.
    id("verbeclair.jacoco")
    id("verbeclair.ktlint")
}

dependencies {
    testImplementation(libs.konsist)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertk.jvm)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test> {
    useJUnitPlatform()
    outputs.upToDateWhen { false }
    outputs.cacheIf { false }
}
