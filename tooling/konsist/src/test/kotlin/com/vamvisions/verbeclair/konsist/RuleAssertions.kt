package com.vamvisions.verbeclair.konsist

import com.lemonappdev.konsist.api.provider.KoBaseProvider
import com.lemonappdev.konsist.api.provider.KoLocationProvider
import com.lemonappdev.konsist.api.provider.KoNameProvider
import com.lemonappdev.konsist.api.provider.KoPathProvider
import org.junit.jupiter.api.Assumptions.abort

/**
 * A path spelled the way every filter in this module compares.
 *
 * Konsist reports `projectPath` with the platform's separator, so on Windows it
 * is `\app\src\main\kotlin\Foo.kt` while every prefix and `contains` check
 * here is written against `/`. The comparisons then match NOTHING, and that
 * failure is silent in the dangerous direction twice over:
 *
 *   - ProjectScope's slice stops excluding anything, so the scope widens to the
 *     tooling modules' rule sources and build-logic. On a
 *     26-module install, 308 of 314 declarations in scope were tooling
 *     sources rather than the app's;
 *   - and the two ScopeIntegrityTest cases written to catch exactly that still
 *     PASSED, because a filter that matches nothing also leaks nothing into the
 *     list they inspect.
 *
 * ProjectScope's own KDoc describes this failure -- "the scope *widens* to
 * include the tooling modules' rule sources" -- and
 * expected it to arrive through a moved directory. It arrived through a path
 * separator instead.
 *
 * Normalising rather than branching on the platform: there is one spelling in
 * this module, and it is this one.
 */
internal fun String.forwardSlashed(): String = replace('\\', '/')

/**
 * Asserts [predicate] holds for every declaration in this subject set, naming each
 * offender with its file path.
 *
 * An empty subject set is itself a failure. Eighteen of this suite's rules find
 * nothing on conformant code, so a rule whose selector has quietly stopped matching
 * is indistinguishable from a rule that is passing — unless matching nothing is
 * loud. See the "rules that find nothing can rot unnoticed" risk in the design.
 *
 * Use this for a rule whose subject MUST exist in any repository the scope covers:
 * every module owns its package root, every module is mapped. Matching nothing
 * there means the scope is broken, and reporting that as a pass would hide it.
 *
 * For a rule about a construct a project may simply not have — DTOs, Room
 * entities, ViewModels — use [assertAllWhereApplicable] instead.
 */
internal fun <T : KoBaseProvider> List<T>.assertAll(
    rule: String,
    requirement: String,
    predicate: (T) -> Boolean,
) {
    assertMatchesSomething(rule)

    val blocking = filterNot(predicate)
    if (blocking.isEmpty()) return

    throw AssertionError(
        buildString {
            appendLine("$rule: ${blocking.size} of $size declarations violate this rule.")
            appendLine("Requirement: $requirement")
            blocking.forEach { appendLine("  - ${describe(it)}") }
        },
    )
}

/**
 * The same rule, for a construct a repository is allowed not to have.
 *
 * The distinction this draws is the difference between two facts that
 * [assertAll] was treating as one:
 *
 *   NOT APPLICABLE — this project has no DTOs, so "every DTO lives in a dto
 *                    package" is vacuously true, and correctly so.
 *   MISCONFIGURED  — this project HAS DTOs and the selector found none.
 *
 * Measured on a greenfield single-module app: sixteen of twenty-five rules
 * failed, thirteen of them purely because the project had no ViewModels, no
 * DTOs and no Room entities. Not one of those failures was about the code. A
 * framework that cannot be installed into a new project until that project
 * already has the architecture it checks is not a verification framework, it is
 * a migration.
 *
 * The empty case ABORTS rather than passes. JUnit reports an aborted test as
 * skipped, with this reason attached, so it appears in the report as something
 * that did not run — never as a green check. A silent vacuous pass is the exact
 * failure mode `assertAll`'s empty-set rule was written to prevent, and this
 * must not reintroduce it through the back door.
 */
internal fun <T : KoBaseProvider> List<T>.assertAllWhereApplicable(
    rule: String,
    requirement: String,
    subject: String,
    predicate: (T) -> Boolean,
) {
    if (isEmpty()) {
        abort<Unit>(
            "$rule did not run: this project has no $subject. The rule is not " +
                "applicable here rather than satisfied — if you DO have $subject, " +
                "the selector or the analysis scope is wrong, and that is a defect.",
        )
    }

    val blocking = filterNot(predicate)
    if (blocking.isEmpty()) return

    throw AssertionError(
        buildString {
            appendLine("$rule: ${blocking.size} of $size declarations violate this rule.")
            appendLine("Requirement: $requirement")
            blocking.forEach { appendLine("  - ${describe(it)}") }
        },
    )
}

/** Fails when the selector behind [rule] matches nothing, rather than passing vacuously. */
internal fun <T : KoBaseProvider> List<T>.assertMatchesSomething(rule: String) {
    if (isNotEmpty()) return

    throw AssertionError(
        "$rule matched zero declarations. A rule with an empty subject set passes " +
            "without checking anything, so it is treated as a failure: either the " +
            "selector is stale, or the analysis scope no longer covers the sources " +
            "the rule is about.",
    )
}

private fun describe(declaration: KoBaseProvider): String {
    val name = (declaration as? KoNameProvider)?.name ?: declaration.toString()
    val location =
        (declaration as? KoLocationProvider)?.location
            ?: (declaration as? KoPathProvider)?.projectPath
    return if (location == null) name else "$name  ($location)"
}
