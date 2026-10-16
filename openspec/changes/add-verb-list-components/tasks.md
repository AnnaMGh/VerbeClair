Figma base URL used below: `F = https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=`. In the code, every `// Figma:` comment MUST hold the full URL, e.g. `F5-45` → `...?node-id=5-45`.

Before each component: re-read its node with `get_design_context` (figma-design-to-code skill loaded) and `get_variable_defs`; never implement from a screenshot. Every preview is in light and dark (`VerbeClairTheme(darkTheme = false / true)`) and is checked with `android-preview-figma-verify` against the node in its `// Figma:` comment.

## 1. Setup

- [ ] 1.1 Create the `component` package and the string resources `composeResources/values/strings.xml` (en), `values-fr/strings.xml`, `values-ro/strings.xml` with the keys in design.md D7 (fr/ro drafted, flagged for user review). Verify: the generated `Res.string.*` accessors compile with `./gradlew :core:designsystem:assemble`.
- [ ] 1.2 Add `Modifier.flagRailBackground()` to `flag/FlagBackground.kt` (D6: Blue stripe unrotated, cover-scaled, centred, `flagOpacity`, no background colour), with previews `// Figma: F95-211` at 80×393 and 80×1150, light and dark. Verify: a Compose UI test asserts no semantics node is exposed; the previews match the rail areas of PL-01 (`F87-9531`) and TP-01 (`F88-744`).

## 2. Inputs and controls

- [ ] 2.1 Implement `VerbeClairSearchBar` (D2). Previews: Empty `// Figma: F5-45`, Filled "su" `// Figma: F5-55`. Verify: UI tests — clear button only when the query is not empty, tapping it reports `""`, clear button labelled "Clear search".
- [ ] 2.2 Implement `VerbeClairIconButton` with Standard / Tonal styles and the optional badge (D1, D5). Previews: Standard (tune) `// Figma: F5-23`, Tonal (tune) `// Figma: F5-38`, Tonal with badge 2 (`// Figma: F10-18`, the L-01 list-controls instance). Verify: UI tests — badge text "2" shown for `badgeCount = 2`, no badge for `null`, content description includes the count.
- [ ] 2.3 Implement `VerbeClairButton` (Filled, Tonal, Outlined, Text; Disabled = `enabled = false`, D5). Previews: Filled `// Figma: F5-64`, Tonal `// Figma: F5-72`, Outlined `// Figma: F5-80`, Text `// Figma: F5-88`, Disabled `// Figma: F49-211`. Verify: UI test — a disabled button does not call `onClick`.
- [ ] 2.4 Implement `VerbeClairFilterChip` (D5). Previews: Filter off "1st group" `// Figma: F5-92`, Filter on "être" `// Figma: F5-97`. Verify: UI tests — selected semantics reflect `selected`; `onClick` is called in both states.
- [ ] 2.5 Implement `RadioRow`. Previews: selected "A → Z" `// Figma: F5-107`, not selected "Z → A" `// Figma: F5-112`. Verify: UI tests — tapping the label calls `onClick`, the row has `Role.RadioButton` and selected semantics.
- [ ] 2.6 Implement `ListTabs` with `ListTab` (D5). Previews: All selected, 12 favourites `// Figma: F49-131`; Favourites selected `// Figma: F49-138`; at 360 dp (tablet list pane) `// Figma: F88-808`; in the landscape top bar at 272 dp `// Figma: F87-9563`. Verify: UI tests — the Favourites tab text contains the count; tapping a tab reports it.

## 3. List content

- [ ] 3.1 Implement `VerbCell` (D3). Previews: default "aller" `// Figma: F7-33`; matched form "être suis" `// Figma: F7-40`; L-02 instance with score 80% and favourite `// Figma: F10-199`; long infinitive + matched form wrapping (`// Figma: F7-40`); selected on tablet `// Figma: F88-1053`; landscape column 386 dp `// Figma: F87-9584`; tablet pane 360 dp `// Figma: F88-1032`. Verify: UI tests — matched form text present; score only when given; favourite content description switches between "Add to favourites" and "Remove from favourites"; tapping the star or the test button calls only its own callback and never `onClick`; test button labelled "Start a test".
- [ ] 3.2 Implement `SectionHeader`. Previews: "1st group" / "5,684 verbs" `// Figma: F7-45`, and the L-03 instance `// Figma: F10-339`. Verify: UI test — title and count shown; title rendered uppercase.
- [ ] 3.3 Implement `VerbCellSkeleton`. Preview `// Figma: F7-49`. Verify: UI test — no semantics node exposed.
- [ ] 3.4 Implement `EmptyState` with Neutral / Error tones and the action slot. Previews: Neutral with "Clear filters" `// Figma: F7-120`, Neutral without action (L-06 instance) `// Figma: F10-742`, Error `// Figma: F7-134`. Verify: UI tests — action shown only when the slot is given; the icon is not in the semantics tree.
- [ ] 3.5 Implement `VerbeClairLinearProgress` and `BottomSheetHandle` (D1, D5). Previews: progress `// Figma: F7-151`, handle `// Figma: F7-154`. Verify: previews match; progress exposes progress semantics.

## 4. Navigation

- [ ] 4.1 Implement `VerbeClairBottomBar` with `TopLevelDestination` on `flagBarBackground()` (D5). Previews: Verbs `// Figma: F49-62`, Tests `// Figma: F49-84`, Settings `// Figma: F49-106`. Verify: UI tests — exactly one item selected; tapping Settings reports `Settings`.
- [ ] 4.2 Implement `VerbeClairNavigationRail` on `flagRailBackground()` (D5, D6). Previews: Verbs `// Figma: F86-9492`, Tests `// Figma: F86-9505`, Settings `// Figma: F86-9518`, landscape height 393 `// Figma: F87-9534`, tablet height 1150 `// Figma: F88-759`. Verify: UI tests — exactly one item selected; tapping reports the destination.

## 5. Configuration and accessibility checks

- [ ] 5.1 Check every component preview in light and dark, at the phone portrait, landscape and tablet widths above, with `android-preview-figma-verify`. Verify: no BLOCKER/MAJOR differences left; remaining minor differences recorded in the PR.
- [ ] 5.2 Check the components with the largest system font size and with TalkBack labels (preview with `fontScale = 2f` for verb cell, tabs and bottom bar). Verify: text wraps without overlapping the cell buttons; labels match the spec.
- [ ] 5.3 Check the French and Romanian strings render without truncation (previews with `locale = "fr"` and `"ro"` for list tabs and bottom bar). Verify: no cut text; list the drafted fr/ro strings in the PR description for the user's review.

## 6. Done gate

- [ ] 6.1 Run `DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew build` from the repository root (covers `:core:designsystem`, `:shared`, `:androidApp` and the iOS targets) and `./gradlew connectedAndroidDeviceTest` on a running emulator. Verify: both succeed, with the command output recorded.
