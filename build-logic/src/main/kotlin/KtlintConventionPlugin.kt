import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.attributes.Bundling
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import java.io.File

/**
 * Wires ktlint into whichever project applies this plugin.
 *
 * The CLI is resolved into a dedicated `ktlint` configuration from the version
 * catalog, and two tasks run it over the project's own Kotlin sources:
 *
 *  - `ktlintCheck` — read-only, fails the build on any violation. This is what
 *    the module's `check` depends on, which makes `build` fail on a formatting violation.
 *  - `ktlintFormat` — the same scan with `--format`, autocorrecting in place.
 *  - `ktlintToolingClasspath` — writes the resolved CLI classpath to
 *    `build/ktlint/cli-classpath.txt`, so the per-edit format hook can run ktlint
 *    directly instead of paying for a Gradle invocation per edited file. The
 *    shadowed variant is a single self-contained jar, which keeps that file to one line.
 *
 * Neither task passes a rule id or a rule-configuration flag: the whole rule
 * set lives in the root `.editorconfig` so that Gradle, the per-edit hook, and
 * the IDE all see the identical configuration.
 *
 * A BASELINE IS PASSED, AND IT IS PER MODULE, so findings `ktlintFormat` cannot
 * autocorrect stay suppressed while a new violation still fails the build.
 *
 * It is per module rather than one root file because ktlint records baseline
 * paths relative to its working directory, and every module runs with
 * `workingDir = projectDir`. A single root file would collide
 * `src/main/kotlin/Foo.kt` from every module onto one entry.
 *
 * The baseline is passed ONLY when it already exists. `--baseline` pointed at
 * a missing file makes ktlint CREATE it and report success, so passing it
 * unconditionally would turn the first `ktlintCheck` on a dirty repository
 * into a silent, permanent amnesty. A baseline is created deliberately, with
 * `ktlint --baseline=ktlint.baseline.xml` from the module directory.
 */
class KtlintConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
            configurations.resolvable(KTLINT_CONFIGURATION) {
                // ktlint-cli publishes both a plain and a shadowed runtime variant.
                // The shadowed one is a single self-contained jar.
                attributes {
                    attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.SHADOWED))
                }
            }
            dependencies.add(KTLINT_CONFIGURATION, libs.findLibrary("ktlint-cli").get())

            val baselineFile = File(projectDir, BASELINE_FILE)

            val ktlintCheck =
                tasks.register<JavaExec>("ktlintCheck") {
                    group = "verification"
                    description = "Checks Kotlin sources in this project against the root .editorconfig rule set."
                    classpath = configurations.getByName(KTLINT_CONFIGURATION)
                    mainClass.set(KTLINT_MAIN_CLASS)
                    workingDir = projectDir
                    args = SOURCE_PATTERNS + REPORTING_ARGS
                    // Resolved at EXECUTION time, not configuration time.
                    argumentProviders.add {
                        if (baselineFile.isFile) listOf("--baseline=$BASELINE_FILE") else emptyList()
                    }
                }

            // `check` -- and so `build` -- runs ktlint, as detekt's plugin already does
            // for detekt. `check` is registered by whichever base, JVM or Kotlin
            // Multiplatform plugin the module applies, in any order relative to this
            // one, so it is matched by name rather than looked up.
            tasks.matching { task -> task.name == "check" }.configureEach { dependsOn(ktlintCheck) }

            val ktlintToolingClasspath =
                tasks.register("ktlintToolingClasspath") {
                    group = "verification"
                    description = "Writes the resolved ktlint CLI classpath so tooling can run it without Gradle."
                    val classpath: FileCollection = configurations.getByName(KTLINT_CONFIGURATION)
                    val classpathFile = layout.buildDirectory.file(CLASSPATH_FILE)
                    inputs.files(classpath)
                    outputs.file(classpathFile)
                    doLast {
                        val output = classpathFile.get().asFile
                        output.parentFile.mkdirs()
                        output.writeText(classpath.joinToString(File.pathSeparator) { it.absolutePath })
                    }
                }

            // build-logic is an included build, so its own sources are invisible to the
            // projects loop of every gate. The root project's ktlintCheck therefore also
            // runs the included build's, or the convention plugins that define the gates
            // would be the one thing the gates never read.
            if (this == rootProject) {
                gradle.includedBuilds.firstOrNull { it.name == "build-logic" }?.let { buildLogic ->
                    ktlintCheck.configure { dependsOn(buildLogic.task(":ktlintCheck")) }
                }
            }

            // The per-edit format hook reads this file, so every gate that runs ktlint also
            // writes it; otherwise it is missing after a clean or on a fresh clone.
            ktlintCheck.configure { dependsOn(ktlintToolingClasspath) }

            tasks.register<JavaExec>("ktlintFormat") {
                group = "formatting"
                description = "Autocorrects Kotlin sources in this project and reports what could not be fixed."
                classpath = configurations.getByName(KTLINT_CONFIGURATION)
                mainClass.set(KTLINT_MAIN_CLASS)
                workingDir = projectDir
                args = SOURCE_PATTERNS + REPORTING_ARGS + "--format"
                // Formatting must not stop at the first project with a violation that
                // has no autocorrect; ktlintCheck is the gate, this task only repairs.
                isIgnoreExitValue = true
            }
        }
    }

    private companion object {
        const val KTLINT_CONFIGURATION = "ktlint"
        const val KTLINT_MAIN_CLASS = "com.pinterest.ktlint.Main"
        const val BASELINE_FILE = "ktlint.baseline.xml"
        const val CLASSPATH_FILE = "ktlint/cli-classpath.txt"

        val SOURCE_PATTERNS =
            listOf(
                "src/**/*.kt",
                "src/**/*.kts",
                "*.kts",
                "!**/build/**",
                "!**/generated/**",
            )

        val REPORTING_ARGS =
            listOf(
                "--relative",
                "--reporter=plain",
            )
    }
}
