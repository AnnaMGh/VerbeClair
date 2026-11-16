package com.vamvisions.verbeclair.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.container.KoScope
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration

/**
 * The single analysis scope every rule class in this module reads.
 *
 * Constructing a scope parses every Kotlin source in the repository, measured at
 * roughly 0.7 s. That cost is per construction and not per query, so the scope is
 * built once here and shared; five rule classes each building their own would be
 * five parses for identical results.
 */
internal object ProjectScope {
    /**
     * The directories whose Kotlin files are not the project's source.
     *
     * `scopeFromProject()` correctly excludes `build/` and the KSP output, but it
     * walks every other directory in the repository, including ones that hold
     * Kotlin belonging to the agent harness rather than to the app. `.claude` is
     * sliced out here once rather than exempted rule by rule.
     */
    val EXCLUDED_ROOTS = listOf(".claude")

    /**
     * The two tooling modules, sliced out for the same reason as `.claude`.
     *
     * `tooling/detekt-rules` and `tooling/konsist` are build tooling, not app
     * code, so grading them against the app's architecture (for example "module
     * owns its package root") would report on code the architecture rules were
     * never meant for.
     */
    val TOOLING_MODULES = listOf("tooling/detekt-rules", "tooling/konsist")

    /**
     * Is this path outside the project's analysed source?
     *
     * A named function rather than a lambda inside `all`, so the predicate can be
     * tested DIRECTLY -- with no scope, no konsist parse and no consumer project.
     * That matters because the tests which assert the scope excludes these
     * directories cannot fail when the predicate matches nothing: they inspect
     * what leaked INTO a list, and a filter matching nothing leaks nothing.
     * ScopePathSpellingTest tests this function instead, on both spellings.
     */
    fun isExcluded(path: String): Boolean {
        val normalised = path.forwardSlashed()
        return EXCLUDED_ROOTS.any { normalised.startsWith("/$it/") } ||
            TOOLING_MODULES.any { normalised.startsWith("/$it/") }
    }

    val all: KoScope by lazy {
        Konsist.scopeFromProject().slice { file -> !isExcluded(file.projectPath) }
    }

    val files: List<KoFileDeclaration> by lazy { all.files }

    val productionFiles: List<KoFileDeclaration> by lazy {
        files.filter { it.sourceSetName == PLAIN_PRODUCTION || it.sourceSetName.endsWith(PRODUCTION_SUFFIX) }
    }

    val testFiles: List<KoFileDeclaration> by lazy {
        files.filter { it.sourceSetName == PLAIN_TEST || it.sourceSetName.endsWith(TEST_SUFFIX) }
    }

    private const val PRODUCTION_SUFFIX = "Main"
    private const val TEST_SUFFIX = "Test"

    // A plain Android or JVM module names its source sets `main` and `test`, with no target prefix.
    private const val PLAIN_PRODUCTION = "main"
    private const val PLAIN_TEST = "test"

    /**
     * The root package this project's own code lives under -- the namespace
     * konsist analyses.
     *
     * Rendered per repository, with no default. A wrong value here does not
     * fail: it selects nothing, and every architecture rule then passes over an
     * empty scope while reporting success. `ScopeIntegrityTest` is the guard
     * against that, and it is the reason this is a required parameter rather
     * than a value with a plausible-looking fallback.
     */
    const val PACKAGE_ROOT = "com.vamvisions.verbeclair"
}
