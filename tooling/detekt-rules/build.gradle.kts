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
    compileOnly(libs.detekt.api)

    testImplementation(libs.detekt.test)
    testImplementation(libs.detekt.test.utils)
    testImplementation(libs.detekt.test.junit)
    // On the test runtime classpath so the Analysis API can resolve
    // kotlinx.coroutines symbols in compiled-and-resolved test snippets.
    testImplementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.detekt.api)
    // The glob tests prove each rule's excludes/includes do what they claim by
    // applying the same PathFilters predicate the gate's Analyzer applies.
    testImplementation(libs.detekt.utils)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertk.jvm)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
