import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.math.BigDecimal

/**
 * Sets up JaCoCo coverage for whichever module applies this plugin:
 *
 *  - applies the `jacoco` plugin and pins its tool version from the version catalog
 *    (`[versions] jacoco`), so reports use a known JaCoCo release;
 *  - for Android library/application modules, enables unit-test AND
 *    instrumentation-test coverage on the `debug` build type. Those flags are what
 *    make AGP instrument the bytecode and generate the coverage report tasks
 *    (`createDebugUnitTestCoverageReport`, the androidTest coverage report, etc.).
 *
 * Pure Kotlin/JVM modules just get the `jacoco` plugin (its default `test` +
 * `jacocoTestReport` wiring), since the AGP build-type flags don't apply to them.
 *
 * Kotlin Multiplatform modules also get `jacocoXmlReport`, which runs the module's JVM or
 * Android host tests and writes one JaCoCo XML report to `build/reports/jacoco/coverage.xml`.
 * That one path is what a coverage gate reads for every module.
 *
 * Every measured module also gets `jacocoCoverageVerification`, which fails when line coverage is below
 * the module's floor. The floor is set in the module's build file (`coverageFloor { linePercent = 58 }`)
 * and only goes up: lowering it shows in the diff and needs a reason in the pull request. The
 * target percent is the same for every module. Modules in the exempt list have nothing to measure yet.
 */
abstract class CoverageFloorExtension {
    /** Minimum line coverage, in whole percent, of the measured variant. */
    abstract val linePercent: Property<Int>
}

class JacocoConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            pluginManager.apply("jacoco")
            pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") { registerReports() }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
            extensions.configure<JacocoPluginExtension> {
                toolVersion = libs.findVersion("jacoco").get().requiredVersion
            }

            pluginManager.withPlugin("com.android.library") {
                extensions.configure<LibraryExtension> {
                    buildTypes.getByName("debug") {
                        enableUnitTestCoverage = true
                        enableAndroidTestCoverage = true
                    }
                }
            }
            pluginManager.withPlugin("com.android.application") {
                extensions.configure<ApplicationExtension> {
                    buildTypes.getByName("debug") {
                        enableUnitTestCoverage = true
                        enableAndroidTestCoverage = true
                    }
                }
            }
        }

    private fun Project.registerReports() {
        val coverageDir = layout.buildDirectory.dir("jacoco")

        // A module with both a JVM and an Android target compiles commonMain twice, to two class
        // trees with the same class names. JaCoCo refuses two classes of one name, and merging them
        // would report whichever compilation the tests did not run on as uncovered. So the report
        // measures one variant: the first whose tests wrote execution data.
        val variant =
            provider {
                VARIANTS.firstOrNull { candidate ->
                    coverageDir
                        .get()
                        .file(candidate.execFile)
                        .asFile.isFile
                }
            }

        // Names, not task references: the Android host-test task is registered after this plugin
        // is applied, and a module only has the tasks of the targets it declares.
        val testTasks = provider { TEST_TASKS.filter { name -> tasks.names.contains(name) } }
        // Empty, not absent, for a module with no tests: JaCoCo then skips the task and writes
        // no report, which is how a coverage gate learns the module has nothing to measure.
        val classDirs =
            provider<Any> {
                variant.orNull?.let { chosen ->
                    fileTree(layout.buildDirectory.dir(chosen.classesDir)) { exclude(COVERAGE_EXCLUDES) }
                } ?: emptyList<Any>()
            }
        val execData =
            provider<Any> {
                variant.orNull?.let { chosen -> coverageDir.get().file(chosen.execFile).asFile }
                    ?: emptyList<Any>()
            }

        val floor = extensions.create("coverageFloor", CoverageFloorExtension::class.java)
        if (path !in EXEMPT) registerVerification(floor, testTasks, classDirs, execData)

        tasks.register("jacocoXmlReport", JacocoReport::class.java) {
            group = "verification"
            description = "Runs the unit tests and writes a JaCoCo XML report for the measured variant."

            dependsOn(testTasks)
            sourceDirectories.setFrom(files(SOURCE_DIRS))
            classDirectories.setFrom(classDirs)
            executionData.setFrom(execData)

            reports {
                xml.required.set(true)
                html.required.set(false)
                csv.required.set(false)
                xml.outputLocation.set(layout.buildDirectory.file(REPORT_FILE))
            }
        }
    }

    private fun Project.registerVerification(
        floor: CoverageFloorExtension,
        testTasks: Provider<List<String>>,
        classDirs: Provider<Any>,
        execData: Provider<Any>,
    ) {
        tasks.register("jacocoCoverageVerification", JacocoCoverageVerification::class.java) {
            group = "verification"
            description =
                "Fails when line coverage of the measured variant is below the module's floor (target $TARGET_PERCENT %)."

            dependsOn(testTasks)
            classDirectories.setFrom(classDirs)
            executionData.setFrom(execData)

            violationRules {
                rule {
                    limit {
                        counter = "LINE"
                        value = "COVEREDRATIO"
                        minimum = BigDecimal(floor.linePercent.getOrElse(0)).movePointLeft(2)
                    }
                }
            }
        }
    }

    private class Variant(val execFile: String, val classesDir: String)

    private companion object {
        /** The coverage every module is working towards; floors rise to it and never fall. */
        const val TARGET_PERCENT = 80

        /** Modules without a JVM or Android host test target, so there is no unit-test coverage to measure. */
        val EXEMPT =
            mapOf(
                ":core:designsystem" to "Compose UI only; tested as device tests, no jvm() or host test target",
                ":core:data" to
                    "wraps Room and DataStore for the platforms; tested as device tests, no jvm() or host test target",
                ":core:presentation" to "only commonMain and commonTest; no jvm() or host test target",
                ":feature:verblist" to
                    "only commonMain, commonTest and device tests; no jvm() or host test target",
                ":tooling:prism-rules" to
                    "kotlin.jvm build tooling; this plugin measures multiplatform modules only, so it is not wired yet",
                ":tooling:konsist" to
                    "kotlin.jvm build tooling; this plugin measures multiplatform modules only, so it is not wired yet",
            )

        const val REPORT_FILE = "reports/jacoco/coverage.xml"

        val VARIANTS =
            listOf(
                Variant(execFile = "jvmTest.exec", classesDir = "classes/kotlin/jvm/main"),
                Variant(execFile = "testAndroidHostTest.exec", classesDir = "classes/kotlin/android/main"),
            )

        val TEST_TASKS = listOf("jvmTest", "testAndroidHostTest")

        val SOURCE_DIRS = listOf("src/commonMain/kotlin", "src/jvmMain/kotlin", "src/androidMain/kotlin")

        // Generated code, so the numbers match what a person would call their own code.
        val COVERAGE_EXCLUDES =
            listOf(
                "**/R.class",
                "**/R$*.class",
                "**/BuildConfig.*",
                "**/generated/**",
                "**/*ComposableSingletons*.*",
                "**/*\$\$serializer.*",
                "**/*_Impl.*",
                "**/*_Impl\$*.*",
            )
    }
}
