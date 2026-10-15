Figma base URL used below: `F = https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=`. In the code, every `// Figma:` comment MUST hold the full URL, e.g. `F8-4` → `...?node-id=8-4`.

## 1. Module setup

- [ ] 1.1 Create `core/designsystem/build.gradle.kts` (KMP library as in design.md D1; namespace `com.vamvisions.verbeclair.core.designsystem`; iOS arm64 and simulator; compose and resources dependencies from the catalog). Include `:core:designsystem` in `settings.gradle.kts`. Verify: `./gradlew :core:designsystem:assemble` succeeds.
- [ ] 1.2 Add `implementation(project(":core:designsystem"))` to `:shared` `commonMain`. Verify: `./gradlew :shared:assemble` succeeds.

## 2. Fonts and typography

- [ ] 2.1 Add the static TTFs and their OFL licence to `core/designsystem/src/commonMain/composeResources/font/`: Fraunces SemiBold; DM Sans Regular, Medium, SemiBold and Bold (D4). Verify: the generated `Res.font.*` accessors compile.
- [ ] 2.2 Build the font families, the M3 `Typography` and the extended typography (Title/Verb, Label/Overline, Conjugation/Form, Conjugation/Ending) from the D3 typography table. Verify: a `commonTest` test asserts every style's size, line height, letter spacing and weight against the Figma values.

## 3. Colours

- [ ] 3.1 Define the light and dark extended colours (`ending`, `ending-container`, `success`, `on-success`, `success-container`, `on-success-container`) from D3. Verify: a `commonTest` test asserts each hex value in each mode.
- [ ] 3.2 Re-read the `Color` collection with `get_variable_defs` and confirm it matches the D3 table, then build the light and dark `ColorScheme`s from Figma values only. Verify: a `commonTest` test asserts every role in each mode against the D3 table.

## 4. Spacing, radius, elevation, flag opacity

- [ ] 4.1 Define the spacing (`space2`…`space48`), radius (`xs`…`xl`, `full`) and M3 `Shapes` mapping from D3. Verify: a `commonTest` test asserts every value.
- [ ] 4.2 Define `elevation.level2` (two shadows on #0D0F33) and `flagOpacity = 0.12f` (source: Notion N-14). Verify: a `commonTest` test asserts the shadow offsets, blurs and alphas, and the opacity value.

## 5. Theme

- [ ] 5.1 Implement `VerbeClairTheme(darkTheme = isSystemInDarkTheme())`: `MaterialTheme`, plus the providers for extended colours, extended typography, spacing, radius, elevation and flag opacity, plus the `VerbeClairTheme` accessor object (D2). Verify: a `commonTest` Compose test reads each local inside the theme, in light and in dark mode.
- [ ] 5.2 Add theme catalogue previews (colour swatches, type ramp, spacing/radius/elevation), one per Figma frame and mode, each preceded by its Figma comment. Verify: each preview renders in Android Studio and matches its frame with `android-preview-figma-verify`:
  - `// Figma: F8-4` (Colour Light)
  - `// Figma: F8-124` (Colour Dark)
  - `// Figma: F8-244` (Typography, light and dark)
  - `// Figma: F8-288` (Spacing, radius, elevation, light and dark)

## 6. Icons

- [ ] 6.1 Export the 39 icon symbols of Figma frame 4:2 as SVG through the Figma MCP. Convert each with the `figma-to-android-vector` skill into `VerbeClairIcons` `ImageVector`s (D5); no hand-drawn paths. Verify: every Figma icon name has a matching `VerbeClairIcons` entry; a `commonTest` test asserts the count is 39 and each is 24×24.
- [ ] 6.2 Add an icon-grid preview in light and dark, preceded by `// Figma: F4-2`. Verify: it matches the Icons frame with `android-preview-figma-verify`.

## 7. Flag background

- [ ] 7.1 Export the Blue (95:211) and White (95:213) stripes from Figma at the highest available resolution. Convert them to WebP in `composeResources/drawable/` (D6). Verify: both resources resolve through `Res.drawable.*`.
- [ ] 7.2 Implement `Modifier.flagScreenBackground()` and `Modifier.flagBarBackground()`: cover-scaled, centred, at `flagOpacity`, outside the semantics tree. Verify: a Compose UI test asserts that no semantics node is exposed for the flag.
- [ ] 7.3 Add previews, each preceded by its Figma comment. Verify: each matches with `android-preview-figma-verify`:
  - `// Figma: F95-213`: White stripe screen background, light and dark
  - `// Figma: F95-211`: Blue stripe bar background (80 dp high bar, rotated stripe), light and dark

## 8. App integration and configuration checks

- [ ] 8.1 Replace `MaterialTheme` with `VerbeClairTheme` in `shared/src/commonMain/kotlin/com/vamvisions/verbeclair/App.kt`, and apply `flagScreenBackground()` at the root. Verify: the Android app launches with the VerbeClair background, and the iOS simulator build runs.
- [ ] 8.2 Add previews of the themed app root with the flag background, one per configuration: phone portrait 393×852, phone landscape 852×393, tablet portrait 834×1194, tablet landscape 1194×834, each in light and dark. Precede each with `// Figma: F3-65` (Screens page; no screen is implemented yet). Verify: every preview renders with no gaps or stretching in the flag, and the right colours for its mode.
- [ ] 8.3 Check the theme and flag background on a phone (portrait and landscape) and a tablet, in light and dark mode, and with the largest system font size. Verify: matches the spec scenarios in `specs/design-system-foundation/spec.md`, and text scales.

## 9. Done gate

- [ ] 9.1 Run all tests and `./gradlew build` from the repository root, which covers `:core:designsystem`, `:shared`, `:androidApp` and the iOS targets. Verify: both succeed, with the command output recorded.
