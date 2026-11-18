package com.vamvisions.verbeclair.konsist

/**
 * The package root each Gradle module owns.
 *
 * Written out rather than derived, because the mapping is not mechanical: a
 * module path is not always its package. An application module often owns the
 * bare root package, and a hyphenated directory (`core/design-system`,
 * `core/files-db`) usually maps to a package with the hyphen dropped rather
 * than preserved. Deriving it would be right most of the time, and silently
 * wrong for exactly the modules whose naming is irregular.
 *
 * `StructureRulesTest` asserts this map's keys are exactly the modules declared
 * in `settings.gradle.kts`, so a new module cannot land without an entry here.
 *
 * The two tooling modules are not listed here: `ProjectScope` slices them out of the scope entirely, so no file
 * from them ever reaches a rule that would consult this map, and an entry for a
 * module the scope cannot yield is a line that can only mislead.
 */
internal val MODULE_PACKAGE_ROOTS =
    mapOf(
        "androidApp" to "com.vamvisions.verbeclair",
        "shared" to "com.vamvisions.verbeclair",
        "core/designsystem" to "com.vamvisions.verbeclair.core.designsystem",
        "core/domain" to "com.vamvisions.verbeclair.core.domain",
        "core/database" to "com.vamvisions.verbeclair.core.database",
        "core/data" to "com.vamvisions.verbeclair.core.data",
        "core/presentation" to "com.vamvisions.verbeclair.core.presentation",
        "feature/verblist" to "com.vamvisions.verbeclair.feature.verblist",
    )

/**
 * `build-logic` is an included build, not a module in `settings.gradle.kts`. Its
 * convention plugins are declared in the default package because Gradle resolves
 * `implementationClass` by simple name, so it is outside every package rule here.
 *
 * A PATH PREFIX, NOT A MODULE NAME, and a real install is what settled it. This
 * used to be matched as `file.moduleName == "build-logic"`, which holds only when
 * the included build is a single flat project — the shape of a
 * repository whose build-logic is one project. A repository with several build-logic
 * subprojects gets other module names, the exemption matches none of them, and
 * `module owns its package root` reports every convention plugin, which sit in the
 * default package because Gradle requires it. Measured on a 26-module install: 21 of
 * 206 declarations, none of them application code.
 */
internal const val INCLUDED_BUILD_PATH = "/build-logic/"
