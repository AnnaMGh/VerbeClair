package com.plcoding.prism.tooling.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

/**
 * Reports a preview that does not call a composable declared in its own file.
 *
 * A preview is documentation of the composable beside it. Kept in a separate
 * `*Previews.kt` file it drifts from what it shows: the composable changes, the
 * file nobody opens does not. The preview must call at least one non-preview
 * composable that this file declares; calling only wrappers and helpers from
 * other files (a theme, a preview frame) does not count, and neither does a
 * private helper that only previews call: that is a preview frame kept in the
 * same file, not the composable being previewed.
 */
class PreviewInSameFileAsComposable(config: Config) :
    Rule(
        config,
        "A preview lives in the file of the composable it previews.",
    ) {
    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        if (!function.isPreview()) return

        val production =
            function.containingKtFile
                .collectDescendantsOfType<KtNamedFunction>()
                .filter { it.isComposable() && !it.isPreview() }
        val calledByProduction =
            production
                .flatMap { it.collectDescendantsOfType<KtCallExpression>() }
                .mapNotNull { it.calleeExpression?.text }
                .toSet()
        val declaredHere =
            production
                .filter { !it.hasModifier(KtTokens.PRIVATE_KEYWORD) || it.name in calledByProduction }
                .mapNotNull { it.name }
                .toSet()
        val called =
            function
                .collectDescendantsOfType<KtCallExpression>()
                .mapNotNull { it.calleeExpression?.text }
        if (called.any { it in declaredHere }) return

        report(
            Finding(
                Entity.from(function),
                "Move '${function.name}' into the file that declares the composable it previews. " +
                    "A preview that calls no composable of its own file drifts from what it shows.",
            ),
        )
    }
}
