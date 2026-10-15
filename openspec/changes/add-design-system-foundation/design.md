## Context

- The repository is the KMP wizard template: `:shared` (commonMain, androidMain, iosMain) and `:androidApp`.
  - `App.kt` wraps everything in a default `MaterialTheme`.
  - There are no fonts, icons, tokens or design-system code.
- Libraries already in `gradle/libs.versions.toml`:
  - Compose Multiplatform 1.12.1, compose-material3 1.12.0-alpha03 and compose-components-resources
  - Android previews through `compose-uiTooling`
- Every value below comes from Figma metadata and variables (`get_metadata`, `get_variable_defs`), never from
  screenshots. The motivation is in proposal.md, Why. The behaviour is in `specs/design-system-foundation/spec.md`.

## Goals / Non-Goals

**Goals:**
- One `VerbeClairTheme` that any composable in `:shared` (and later feature modules) wraps itself in. It exposes:
  - the Material 3 `colorScheme`, `typography` and `shapes`
  - VerbeClair-only tokens: extended colours, spacing, radius, elevation and flag opacity
- A single place where each Figma variable maps to Kotlin, so later Figma edits are one-line changes.
- Icons and flag images built from Figma's exported SVG and image assets.

**Non-Goals:**
- No Figma component sets as composables (buttons, chips, verb cell, bottom nav, navigation rail…). They belong to
  later changes and will reuse this theme.
- No screens and no adaptive scaffold. The window-size-class logic of Notion N-15 and NV-4 arrives with navigation.
  This change only makes sure the theme and flag background render correctly at every window size.
- No dynamic colour (Material You). Notion N-5 asks only for light and dark following the system, and the brand
  colours are fixed in Figma.

## Decisions

### D1. New module `:core:designsystem`
- **Plugins:** a KMP library with `com.android.kotlin.multiplatform.library`, `org.jetbrains.compose` and the Compose
  compiler, configured like `:shared`.
- **Namespace:** `com.vamvisions.verbeclair.core.designsystem`. Same `compileSdk`/`minSdk` from the catalog, JVM 11.
- **Targets:** `iosArm64` and `iosSimulatorArm64`. No framework of its own: it is linked into `Shared`.
- **Wiring:**
  - `settings.gradle.kts` includes `:core:designsystem`
  - `:shared` `commonMain` adds `implementation(project(":core:designsystem"))`
- **Dependencies:**
  - `commonMain`: compose runtime, foundation, ui, material3, components-resources and ui-tooling-preview, all from
    the catalog
  - `androidMain`: ui-tooling for previews
  - `commonTest`: kotlin-test
- **Alternatives:**
  - A package inside `:shared`: simpler, but the user chose a separate module so later feature modules can depend on
    the design system without depending on the app.
  - A `build-logic` convention plugin: postponed. With one library module, a plain `build.gradle.kts` is clearer.
    Extract a convention plugin when the second KMP library module appears.

### D2. Theme shape
```
VerbeClairTheme(darkTheme: Boolean = isSystemInDarkTheme(), content)
  +-- MaterialTheme(colorScheme = light/dark, typography = VerbeClairTypography, shapes = VerbeClairShapes)
  +-- CompositionLocalProvider(
        LocalExtendedColors  -> ending, endingContainer, success, onSuccess, successContainer,
                                onSuccessContainer (light/dark)
        LocalSpacing         -> space2 .. space48
        LocalRadius          -> xs, sm, md, lg, xl, full
        LocalElevation       -> level2 (two shadows)
        LocalFlagOpacity     -> 0.12f
      )
object VerbeClairTheme { val extendedColors; val spacing; val radius; val elevation; val flagOpacity }  // @Composable getters
```
- `darkTheme` is a parameter, so previews and tests can force a mode without platform `uiMode` support.
- **Alternative:** a fully custom theme without `MaterialTheme`. Rejected, because Notion section 3.1 says Material 3,
  and M3 components should pick up the colours automatically.

### D3. Figma variable → Kotlin token mapping

**Colour roles** (`Color` collection, Light and Dark modes):

| Figma variable | Kotlin | Light | Dark |
|---|---|---|---|
| color/primary | `colorScheme.primary` | #2E4BCB | #B8C4FF |
| color/on-primary | `colorScheme.onPrimary` | #FFFFFF | #08218A |
| color/primary-container | `colorScheme.primaryContainer` | #DDE1FF | #1A35AD |
| color/on-primary-container | `colorScheme.onPrimaryContainer` | #000E5E | #DDE1FF |
| color/secondary-container | `colorScheme.secondaryContainer` | #E1E1F9 | #444559 |
| color/on-secondary-container | `colorScheme.onSecondaryContainer` | #191A2C | #E1E1F9 |
| color/secondary | `colorScheme.secondary` | #5B5D72 | #C4C5DD |
| color/on-secondary | `colorScheme.onSecondary` | #FFFFFF | #2D2F42 |
| color/tertiary (alias of color/ending) | `colorScheme.tertiary` | #C0391F | #FFB4A1 |
| color/on-tertiary | `colorScheme.onTertiary` | #FFFFFF | #5F1409 |
| color/tertiary-container (alias of color/ending-container) | `colorScheme.tertiaryContainer` | #FFDAD2 | #5F1409 |
| color/on-tertiary-container | `colorScheme.onTertiaryContainer` | #3E0500 | #FFDAD2 |
| color/inverse-primary | `colorScheme.inversePrimary` | #B8C4FF | #2E4BCB |
| color/background | `colorScheme.background` | #FBFAFF | #12131A |
| color/on-background (alias of color/on-surface) | `colorScheme.onBackground` | #1A1B23 | #E3E1EC |
| color/surface | `colorScheme.surface` | #FFFFFF | #12131A |
| color/surface-variant | `colorScheme.surfaceVariant` | #E3E1EC | #464754 |
| color/surface-tint (alias of color/primary) | `colorScheme.surfaceTint` | #2E4BCB | #B8C4FF |
| color/surface-dim | `colorScheme.surfaceDim` | #DBD9E4 | #12131A |
| color/surface-bright | `colorScheme.surfaceBright` | #FFFFFF | #383841 |
| color/surface-container-lowest | `colorScheme.surfaceContainerLowest` | #FFFFFF | #0D0E15 |
| color/surface-container-low | `colorScheme.surfaceContainerLow` | #FBFAFF | #1A1B23 |
| color/surface-container | `colorScheme.surfaceContainer` | #F1F1F9 | #1E1F27 |
| color/surface-container-high | `colorScheme.surfaceContainerHigh` | #E9E9F3 | #292A32 |
| color/surface-container-highest | `colorScheme.surfaceContainerHighest` | #E3E1EC | #33343D |
| color/inverse-surface | `colorScheme.inverseSurface` | #292A32 | #E3E1EC |
| color/inverse-on-surface | `colorScheme.inverseOnSurface` | #F1F1F9 | #292A32 |
| color/scrim | `colorScheme.scrim` | #000000 | #000000 |
| color/on-surface | `colorScheme.onSurface` | #1A1B23 | #E3E1EC |
| color/on-surface-variant | `colorScheme.onSurfaceVariant` | #464754 | #C6C5D6 |
| color/outline | `colorScheme.outline` | #767686 | #908FA0 |
| color/outline-variant | `colorScheme.outlineVariant` | #C6C5D6 | #464754 |
| color/error | `colorScheme.error` | #BA1A1A | #FFB4AB |
| color/on-error | `colorScheme.onError` | #FFFFFF | #690005 |
| color/error-container | `colorScheme.errorContainer` | #FFDAD6 | #93000A |
| color/on-error-container | `colorScheme.onErrorContainer` | #410002 | #FFDAD6 |
| color/primary-fixed | `colorScheme.primaryFixed` | #DDE1FF | #DDE1FF |
| color/primary-fixed-dim | `colorScheme.primaryFixedDim` | #B8C4FF | #B8C4FF |
| color/on-primary-fixed | `colorScheme.onPrimaryFixed` | #000E5E | #000E5E |
| color/on-primary-fixed-variant | `colorScheme.onPrimaryFixedVariant` | #1A35AD | #1A35AD |
| color/secondary-fixed | `colorScheme.secondaryFixed` | #E1E1F9 | #E1E1F9 |
| color/secondary-fixed-dim | `colorScheme.secondaryFixedDim` | #C4C5DD | #C4C5DD |
| color/on-secondary-fixed | `colorScheme.onSecondaryFixed` | #191A2C | #191A2C |
| color/on-secondary-fixed-variant | `colorScheme.onSecondaryFixedVariant` | #444559 | #444559 |
| color/tertiary-fixed | `colorScheme.tertiaryFixed` | #FFDAD2 | #FFDAD2 |
| color/tertiary-fixed-dim | `colorScheme.tertiaryFixedDim` | #FFB4A1 | #FFB4A1 |
| color/on-tertiary-fixed | `colorScheme.onTertiaryFixed` | #3E0500 | #3E0500 |
| color/on-tertiary-fixed-variant | `colorScheme.onTertiaryFixedVariant` | #8D1500 | #8D1500 |
| color/ending | `VerbeClairTheme.extendedColors.ending` | #C0391F | #FFB4A1 |
| color/ending-container | `VerbeClairTheme.extendedColors.endingContainer` | #FFDAD2 | #5F1409 |
| color/success | `VerbeClairTheme.extendedColors.success` | #2E7D4F | #8FD8A8 |
| color/on-success | `VerbeClairTheme.extendedColors.onSuccess` | #FFFFFF | #00391D |
| color/success-container | `VerbeClairTheme.extendedColors.successContainer` | #A4F4BC | #00522C |
| color/on-success-container | `VerbeClairTheme.extendedColors.onSuccessContainer` | #00210F | #A4F4BC |

Every Material 3 role now has a Figma variable, so `ColorScheme` sets no role from Material defaults. Tertiary
reuses the verb-ending coral palette by decision, so `tertiary` and `ending` look the same. In dark mode
`on-tertiary` and `tertiary-container` are both #5F1409; they are never drawn on top of each other. The new primitive
tones (`indigo-gray/20,40,80`, `neutral/4,24,87`, `coral/10,30`, `green/10,20,30,90`) are derived from the existing
palettes' HCT hue and chroma and still need designer sign-off (see Open Questions).

Rule: no role gets a value that is not in Figma; if Figma changes, this table changes with it.

**Typography:**

| Figma style | Kotlin | Font / weight | Size / line / tracking (sp) |
|---|---|---|---|
| Display/Verb Large | `typography.displaySmall` | Fraunces SemiBold | 34 / 40 / -0.5 |
| Headline/Small | `typography.headlineSmall` | Fraunces SemiBold | 24 / 32 / 0 |
| Title/Verb | `VerbeClairTheme.typography.titleVerb` | Fraunces SemiBold | 20 / 26 / 0 |
| Title/Large | `typography.titleLarge` | DM Sans SemiBold | 22 / 28 / 0 |
| Title/Medium | `typography.titleMedium` | DM Sans SemiBold | 16 / 24 / 0.1 |
| Title/Small | `typography.titleSmall` | DM Sans SemiBold | 14 / 20 / 0.1 |
| Label/Overline | `VerbeClairTheme.typography.labelOverline` | DM Sans Bold | 12 / 16 / 1.2 |
| Label/Large | `typography.labelLarge` | DM Sans Medium | 14 / 20 / 0.1 |
| Label/Medium | `typography.labelMedium` | DM Sans Medium | 12 / 16 / 0.4 |
| Body/Large | `typography.bodyLarge` | DM Sans Regular | 16 / 24 / 0.2 |
| Body/Medium | `typography.bodyMedium` | DM Sans Regular | 14 / 20 / 0.2 |
| Body/Small | `typography.bodySmall` | DM Sans Regular | 12 / 16 / 0.3 |
| Conjugation/Form | `VerbeClairTheme.typography.conjugationForm` | DM Sans Regular | 16 / 24 / 0 |
| Conjugation/Ending | `VerbeClairTheme.typography.conjugationEnding` | DM Sans Bold | 16 / 24 / 0 |

- Styles with no M3 slot go into an extended typography local, next to the extended colours.
- M3 slots Figma does not define (`displayLarge/Medium`, `headlineLarge/Medium`, `labelSmall`) keep the M3 defaults,
  but get the DM Sans font family, so no system font appears.
- Every style uses `sp`, so text scales with the system font size.

**Spacing:**
- `space/2, 4, 8, 12, 16, 20, 24, 32, 48` → `VerbeClairTheme.spacing.space2 … space48` (Dp)

**Radius:**
- `radius/xs 4, sm 8, md 12, lg 16, xl 28` and `full` (999 in Figma) → `VerbeClairTheme.radius.*`
- `full` uses `RoundedCornerShape(50)`, not 999 dp
- M3 `shapes` mapping:
  - `extraSmall` = xs
  - `small` = sm
  - `medium` = md
  - `large` = lg
  - `extraLarge` = xl

**Elevation:**
- `Elevation/Level 2` → `VerbeClairTheme.elevation.level2`
- It is two drop shadows on #0D0F33: (0,2) blur 6 at 0x1F alpha, and (0,8) blur 24 at 0x1A alpha
- Drawn with `Modifier.dropShadow` and a `Shadow` spec, available in Compose 1.9 and later
- Not drawn with `shadowElevation`, which cannot reproduce two tinted shadows

**Flag:**
- `flagOpacity` = 0.12f
- Source: Notion N-14. Figma has no variable for it yet, so a matching Figma variable is still to add.

### D4. Fonts
- Static TTFs bundled in `core/designsystem/src/commonMain/composeResources/font/`:
  - Fraunces SemiBold
  - DM Sans Regular, Medium, SemiBold and Bold
- Loaded with `org.jetbrains.compose.resources.Font` in a `@Composable` font-family builder.
- Source: Google Fonts, under the SIL Open Font License 1.1. The licence text goes in the same folder.
- **Alternative:** downloadable Google Fonts. Rejected, because it is Android-only and breaks offline use (Notion N-1).

### D5. Icons
- Each of the 39 symbols in Figma frame 4:2 is exported as SVG through the Figma MCP. It is then converted
  mechanically with the `figma-to-android-vector` skill to a Compose `ImageVector`, in
  `com.vamvisions.verbeclair.core.designsystem.icon.VerbeClairIcons` (e.g. `VerbeClairIcons.StarFilled`).
- `ImageVector` code instead of XML resources, because:
  - it works in `commonMain` previews and tests without resource loading
  - it is tinted with `LocalContentColor` like Material icons
- Paths are never redrawn by hand.

### D6. Flag background
- The Blue (95:211) and White (95:213) stripes are exported from Figma as images, converted to WebP and stored in
  `composeResources/drawable/` as `flag_stripe_white.webp` and `flag_stripe_blue.webp`.
- Two public helpers:
  - `Modifier.flagScreenBackground()`: background colour, then the White stripe with `ContentScale.Crop` and
    `Alignment.Center` at `flagOpacity`
  - `Modifier.flagBarBackground()`: the Blue stripe rotated 90° and cropped to cover
- Both draw in a `drawBehind`/`Box` layer that is not part of the semantics tree, so screen readers skip it.
- The screen helper is applied once at the scaffold root, outside the scrolling content, so the flag stays fixed.

### D7. Previews and verification
- Previews live in `commonMain` with `androidx.compose.ui.tooling.preview.Preview`, as `App.kt` does now.
- Each preview calls `VerbeClairTheme(darkTheme = …)` explicitly and sets `widthDp`/`heightDp` for the three targets:
  - phone portrait 393×852
  - phone landscape 852×393
  - tablet 834×1194 and 1194×834
- Each preview is preceded by `// Figma: <node URL>` for the Figma frame it shows: Colour Light/Dark, Typography,
  Spacing/radius/elevation, Icons, Flag stripe Blue/White.
- Each is checked with the `android-preview-figma-verify` skill after implementation.
- Unit tests in `commonTest` check that every token in the D3 tables equals the Figma value, so a drift fails the
  build.

## Risks / Trade-offs

- [Derived colour tones change after designer review] → the D3 tests compare against the table, so a Figma change
  fails the build until the table and theme are updated together.
- [Fraunces has an optical-size axis; Figma may render a different optical size than the static file] → check
  Display/Verb Large and Title/Verb with `android-preview-figma-verify`. If they differ, choose the matching static
  cut (e.g. 9pt or 72pt).
- [Flag stripe softness when enlarged (Notion open question, Flag stripe resolution)] → export the stripes from Figma
  at the highest available resolution. Check on a real phone and tablet. If soft, request higher-resolution stripe
  exports.
- [Compose `dropShadow` API in a material3 alpha / CMP 1.12] → if it is not available on iOS, fall back to a single
  `shadow()` approximation, and record the difference against Figma.
- [Extra module adds build time] → negligible for one small module; it makes later feature modules possible.

## Migration Plan

- Additive change. `App.kt` swaps `MaterialTheme` for `VerbeClairTheme`; nothing else uses theme values yet.
- Rollback: revert the work branch.

## Open Questions

- **Designer sign-off on derived colour roles (not blocking):** secondary, on-secondary, the tertiary companions,
  surface-variant, surface-dim/bright, surface-container-lowest, the `*Fixed` roles and the success companions were
  added in Figma from tones derived from the existing palettes. The designer confirms or retunes them; any change is
  a value update in the D3 table only.
- **Figma variable for flag opacity:** add `color/opacity/flag` = 12% in Figma to match Notion N-14. Until then, the
  token cites Notion.
- **minSdk mismatch:** `gradle/libs.versions.toml` and `openspec/config.yaml` say minSdk 24, but Notion N-10 says
  Android 8.0 (API 26). Outside this change; resolve before release.
- **Flag stripe resolution:** the Notion open question about stripe sharpness on 3× phones and tablets; check after
  implementation.
