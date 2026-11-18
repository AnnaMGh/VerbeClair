import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.project

/**
 * Wires detekt into whichever project applies this plugin.
 *
 * The gate runs the plugin's plain `detekt` task — syntax-only, no type
 * resolution — and the type-resolving main-source tasks (`detektDebug` on Android
 * application modules, `detektMain` on Kotlin/JVM modules) against the single rule set in the root `detekt.yml`. The plain task skips
 * every rule that needs the Analysis API, so without the type-resolving tasks
 * those rules would be active in `detekt.yml` and never run. Kotlin Multiplatform
 * modules are not covered by the type-resolving tasks yet: their variants also
 * analyse generated KSP and Compose resource code and report findings of their own.
 * No rule is
 * enabled, disabled, or parameterised here: the configuration file is the only
 * surface, mirroring how `.editorconfig` is the only surface for ktlint.
 *
 * The detekt version is pinned exactly in `libs.versions.toml`. While the
 * pinned line is a 2.0 pre-release, a version bump is its own commit, gated by
 * the `:tooling:detekt-rules` suite and a zero-finding `./gradlew detekt check`
 * run, and reverted if either fails.
 *
 * Third-party and project-specific rule sets are added to the `detektPlugins`
 * configuration so that every module picks them up without repeating itself.
 */
class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
            pluginManager.apply("dev.detekt")

            extensions.configure<DetektExtension> {
                config.setFrom(rootProject.layout.projectDirectory.file("detekt.yml"))
                buildUponDefaultConfig.set(true)
                // detekt's default source set is src/{main,test}/{java,kotlin}. A Kotlin
                // Multiplatform module keeps its code in src/commonMain, src/androidMain,
                // src/iosMain, src/commonTest and the like, and an Android module also has
                // src/androidTest, so the default reads none of it and the gate reports
                // NO-SOURCE success over code it never read. Every source set directory
                // that exists is therefore declared rather than inherited.
                source.setFrom(
                    projectDir
                        .resolve("src")
                        .listFiles { entry -> entry.isDirectory }
                        .orEmpty()
                        .sortedBy { entry -> entry.name }
                        .flatMap { entry -> listOf(entry.resolve("java"), entry.resolve("kotlin")) },
                )
                basePath.set(rootProject.layout.projectDirectory)

                // ONE BASELINE PER MODULE, beside that module's build script.
                //
                // This line used to point every module at one file at the
                // repository root, and the comment justifying it said baseline
                // ids embed the file path relative to basePath "so entries from
                // fifteen modules cannot collide". THAT WAS FALSE. An id carries
                // the BARE FILENAME -- `MagicNumber:RunningTracker.kt:...` -- and
                // basePath does not change it. Measured on one 26-module
                // repository's own baseline: 193 ids, 0 containing a path
                // separator, 36 file names appearing more than once. Two modules
                // holding a `RunningTracker.kt` produced the same id, the union
                // deduplicated it, and TWO findings were recorded as ONE entry.
                //
                // The consequence was a false green, which is worse than a false
                // denial: promoting one of those modules could not tell which
                // entries were its own, correctly refused to guess, kept them --
                // and the kept entries were suppressing six real findings in the
                // module being promoted. An entry in this module's file can only
                // ever be read while this module is analysed, so the question
                // does not arise. ktlint has always worked this way.
                //
                // A MISSING file is a complete no-op: detekt emits `--baseline`
                // only when the file exists. An enforcing install that never
                // generated one therefore behaves exactly as it did before this
                // line, which is why no placeholder guards it.
                baseline.set(layout.projectDirectory.file(BASELINE_FILE))
            }

            // Matched by name because the Android, JVM and Kotlin Multiplatform plugins
            // each register their own variants, in any order relative to this plugin.
            // `check` already runs the plain `detekt` task; this adds the ones that
            // resolve types.
            val typeResolving = tasks.matching { task -> task.name in TYPE_RESOLVING_TASKS }
            tasks.matching { task -> task.name == "check" }.configureEach { dependsOn(typeResolving) }

            dependencies.add(DETEKT_PLUGINS_CONFIGURATION, libs.findLibrary("compose-rules-detekt").get())
            dependencies.add(DETEKT_PLUGINS_CONFIGURATION, dependencies.project(RULES_PROJECT))
        }
    }

    private companion object {
        const val DETEKT_PLUGINS_CONFIGURATION = "detektPlugins"
        const val RULES_PROJECT = ":tooling:detekt-rules"
        const val BASELINE_FILE = "detekt.baseline.xml"
        val TYPE_RESOLVING_TASKS = setOf("detektDebug", "detektMain")
    }
}
