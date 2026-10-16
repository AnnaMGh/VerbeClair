## Context

`:core:designsystem` already provides `VerbeClairTheme` (Material 3 colour scheme, typography, shapes, plus
`extendedColors`, `typography.titleVerb` / `labelOverline`, `spacing`, `radius`, `elevation`, `flagOpacity`),
`VerbeClairIcons` (the 39 Figma icons) and the flag helpers `Modifier.flagScreenBackground()` /
`Modifier.flagBarBackground()`. Previews live in `commonMain` next to the code (`FlagBackgroundPreviews.kt`); Compose UI
tests live in `commonTest` and run on the iOS simulator and as Android device tests. See proposal.md for why the
components are needed; the behaviour contract is `specs/design-system-components/spec.md`.

All values below were read from Figma with `get_design_context` on the component sets (Components page 3:64) and on
their instances in the L-01…L-09, PL-01 and TP-01 frames. Matched form was checked on the L-02 instance (10:199): it is
plain `primary` text after the infinitive, as Notion L-4 says.

## Goals / Non-Goals

**Goals:**
- One Kotlin component per Figma component set used by Screen 1, built on Material 3 where Material 3 has the
  component, and themed only through `VerbeClairTheme` / `MaterialTheme` tokens.
- Stateless components: state in, events out (`value` + `onValueChange`, `selected` + `onClick`), so the screen's
  ViewModel owns all state.
- Previews for every Figma variant in light and dark, and for the three window classes where layout can differ.

**Non-Goals:**
- No screen layout: the top bar arrangement, the active choices row, the list-controls sheet content, list–detail and
  the 2-column landscape list belong to `add-verb-list-screen`.
- No debounce or search logic in `SearchBar` (the 300 ms debounce, L-3, lives in the ViewModel).
- No new design tokens: every value maps to an existing foundation token.

## Decisions

### D1. Package and file layout
`core/designsystem/src/commonMain/kotlin/com/vamvisions/verbeclair/core/designsystem/component/`, one file per
component plus one `…Previews.kt` file per component (the pattern of `flag/`). Names are prefixed `VerbeClair` only
where they would clash with a Material 3 composable (`VerbeClairSearchBar`, `VerbeClairButton`,
`VerbeClairIconButton`, `VerbeClairFilterChip`, `VerbeClairLinearProgress`, `VerbeClairBottomBar`,
`VerbeClairNavigationRail`); the others keep the Figma name.

| Component | Kotlin | Built on |
|---|---|---|
| Search bar 5:56 | `VerbeClairSearchBar(query, onQueryChange, modifier)` | `BasicTextField` in a `Surface` (see D2) |
| Icon button 5:39 | `VerbeClairIconButton(icon, contentDescription, onClick, style, badgeCount: Int?)` | M3 `IconButton` / `FilledTonalIconButton` + `BadgedBox` |
| Button 5:89 | `VerbeClairButton(text, onClick, style, enabled, leadingIcon)` | M3 `Button` / `FilledTonalButton` / `OutlinedButton` / `TextButton` |
| Chip 5:101 | `VerbeClairFilterChip(label, selected, onClick)` | M3 `FilterChip` |
| Radio row 5:113 | `RadioRow(label, selected, onClick)` | `Row` + `Modifier.selectable(role = RadioButton)` + M3 `RadioButton(onClick = null)` |
| List tabs 49:145 | `ListTabs(selected: ListTab, favouritesCount, onSelect)` | M3 `PrimaryTabRow` + `Tab` |
| Verb cell 7:41 | `VerbCell(infinitive, meta, matchedForm, score, isFavourite, selected, onClick, onTestClick, onFavouriteClick)` | `Row` + M3 `IconButton`s (D3) |
| Section header 7:45 | `SectionHeader(title, count)` | `Row` |
| Verb cell skeleton 7:49 | `VerbCellSkeleton()` | `Column` of `Box`es |
| Empty state 7:135 | `EmptyState(icon, title, body, tone, action: (@Composable () -> Unit)?)` | `Column` |
| Progress/Linear 7:151 | `VerbeClairLinearProgress()` | M3 `LinearProgressIndicator` |
| Bottom sheet/Handle 7:154 | `BottomSheetHandle()` | `Box` (M3 `DragHandle` has fixed 22 dp padding; Figma has 16) |
| Bottom nav 49:128 | `VerbeClairBottomBar(selected: TopLevelDestination, onSelect)` | M3 `NavigationBar` + `NavigationBarItem` |
| Navigation rail 86:9531 | `VerbeClairNavigationRail(selected, onSelect)` | M3 `NavigationRail` + `NavigationRailItem` |

`TopLevelDestination` (`Verbs`, `Tests`, `Settings`) and `ListTab` (`All`, `Favourites`) are small enums in the
`component` package; the screen maps them to routes later. `EmptyState.action` is a slot so the screen passes a
`VerbeClairButton`, keeping the button the single implementation.

### D2. Search bar is not M3 `SearchBar`
M3's `SearchBar` is 56 dp, expands into a search view and owns its own results surface. Figma's bar is a 52 dp pill on
`surface-container-high` with a leading icon, a placeholder and a 40 dp clear button, filtering the list in place (L-3).
So it is a `BasicTextField` with a `decorationBox`, single line, `ImeAction.Search`, Body/Large text, `primary` cursor.
Padding: 16 dp start; 16 dp end when empty, 4 dp end when filled (the clear button's own 8 dp inset gives the Figma
look). Alternative considered: M3 `DockedSearchBar` with a collapsed state; rejected because its height and expansion
behaviour don't match Figma and it adds a results surface we don't use.

### D3. Verb cell layout and touch targets
- Row: start 16 dp, end 4 dp, vertical 12 dp, gap 12 dp; text column `weight(1f)` with 2 dp between lines; bottom
  1 dp `outlineVariant` line drawn with `drawBehind` (no `HorizontalDivider`, so the cell owns its line and the
  selected background covers it the same way as Figma).
- **Matched form**: one `Text` with an `AnnotatedString`: the infinitive in `titleVerb` / `onSurface`, a normal space,
  then the form in `labelLarge` / `primary`. Text layout then breaks at the space when the infinitive fills the line,
  which is exactly L-4, and a screen reader reads "être suis" as one phrase. Alternative: `FlowRow` with two `Text`s
  aligned on the baseline (what the Figma auto-layout does); rejected because it needs baseline alignment across two
  fonts and reads as two nodes.
- **Actions**: score `Text` (labelLarge, `onSurfaceVariant`), then two M3 `IconButton`s of 44 dp (Figma: 10 dp padding
  around a 24 dp icon), under `LocalMinimumInteractiveComponentSize provides 44.dp`, so M3 does not grow them to 48 dp
  and change the 93 / 73 dp cell heights. 44 dp is the Figma size and meets the iOS minimum.
- Favourite icon: `VerbeClairIcons.Star` tinted `onSurfaceVariant`, or `VerbeClairIcons.StarFilled` tinted
  `extendedColors.ending`. Test icon: `VerbeClairIcons.Exam`, `onSurfaceVariant`.
- The cell itself is `Modifier.clickable(onClick)`; the icon buttons consume their own taps, so they never trigger the
  cell (L-8).
- `selected = true` paints `secondaryContainer` behind the row (instance override 88:1053).

### D4. Token mapping
Every Figma variable maps to an existing token:

| Figma | Kotlin |
|---|---|
| `color/surface-container-high`, `-container`, `-container-low`, `-container-highest` | `MaterialTheme.colorScheme.surfaceContainerHigh` etc. |
| `color/primary`, `on-primary`, `primary-container`, `secondary-container`, `on-secondary-container` | `colorScheme.*` |
| `color/on-surface`, `on-surface-variant`, `outline`, `outline-variant`, `error`, `on-error`, `error-container` | `colorScheme.*` |
| `color/ending` | `VerbeClairTheme.extendedColors.ending` |
| Title/Verb, Label/Overline | `VerbeClairTheme.typography.titleVerb`, `.labelOverline` |
| Title/Large, Title/Small, Label/Large, Label/Medium, Body/Large, Body/Medium | `MaterialTheme.typography.titleLarge` … `bodyMedium` |
| `space/2…48` | `VerbeClairTheme.spacing.space2…space48` |
| `radius/xs`, `sm`, `full` | `VerbeClairTheme.radius.xs`, `.sm`, `.full` (`MaterialTheme.shapes.extraSmall`, `.small`) |
| `Elevation/Level 2` (sheet) | `VerbeClairTheme.elevation.level2` — only used by the screen's sheet, not here |

Sizes that Figma sets as fixed numbers without a variable (52 dp search bar, 48/40 dp icon button, 44 dp cell
buttons, 40 dp button, 32 dp chip, 18 dp chip check, 20 dp radio, 64 × 3 dp tab indicator, 96 dp empty-state circle
with 40 dp icon, 16 dp badge, 4 dp progress, 32 × 4 dp handle, 80 dp bars) are component dimensions, kept as private
`dp` constants inside each component file, named after the Figma layer.

### D5. Material 3 colour overrides
Material 3 defaults differ from Figma in a few places; each component passes explicit `colors`:
- `FilterChip`: `selectedContainerColor = secondaryContainer`, `selectedLabelColor` / `selectedLeadingIconColor =
  onSecondaryContainer`, `labelColor = onSurfaceVariant`, border `outlineVariant` when not selected and none when
  selected, shape `radius.sm`, leading icon `VerbeClairIcons.Check` at 18 dp only when selected.
- `Badge`: `containerColor = error`, `contentColor = onError`, Label/Medium at 11 sp is Figma's badge text; it is the
  only text style not in the foundation, so it is `labelSmall` (M3 11 sp / 16 sp line) which the foundation already
  maps to DM Sans.
- `PrimaryTabRow`: `containerColor = Color.Transparent` (lists have no background, N-14), `divider` 1 dp
  `outlineVariant`, indicator `TabRowDefaults.PrimaryIndicator(width = 64.dp, height = 3.dp, color = primary)`, tab
  labels Title/Small, selected `primary`, unselected `onSurfaceVariant`.
- `NavigationBar` / `NavigationRail`: `containerColor = Color.Transparent`, `NavigationBarItemDefaults.colors(
  indicatorColor = secondaryContainer, selectedIconColor = onSurface, unselectedIconColor = onSurfaceVariant,
  selectedTextColor = onSurface, unselectedTextColor = onSurfaceVariant)`; label style switches between Title/Small
  (selected) and Label/Medium (unselected), as Figma does. The bar's 1 dp top line and the rail's 1 dp end line are
  drawn with `drawBehind`. Rail: 56 dp top padding, 16 dp between items, items at the top.
- `LinearProgressIndicator`: `color = primary`, `trackColor = primaryContainer`, `gapSize = 0.dp`, no stop indicator,
  `StrokeCap.Round`, height 4 dp.
- Disabled `VerbeClairButton` uses M3's own disabled colours (`onSurface` 12% container, 38% content), which are the
  Figma Disabled values, so there is no separate Disabled style in Kotlin: Disabled is `enabled = false`.

### D6. Navigation rail flag background
The foundation design (D6 of `add-design-system-foundation`) left the rail helper for this change. Add
`Modifier.flagRailBackground()` next to the two existing helpers in `flag/FlagBackground.kt`: the Blue stripe,
**unrotated**, scaled to cover and centred, at `flagOpacity`, with **no** background colour of its own (N-14: "the
navigation rail has no background of its own: the blue stripe … sits behind it"). It reuses the private
`flagBackground` / `drawCover` code with an optional background. This is already covered by the foundation spec
("It also fills the navigation-rail area"), so no foundation requirement changes. The bottom bar uses the existing
`flagBarBackground()`.

### D7. Strings
Component strings go in `core/designsystem/src/commonMain/composeResources/values/strings.xml` (en),
`values-fr/strings.xml` and `values-ro/strings.xml`, read with `stringResource`. "Favourites (n)" is a formatted string
(`list_tab_favourites` with `%1$d`), not a plural: the count is shown in brackets in every language. Fixed component
text: `search_placeholder` "Search a verb…", `search_clear` "Clear search" (Notion L-2), `list_tab_all` "All",
`list_tab_favourites` "Favourites (%1$d)", `nav_verbs` / `nav_tests` / `nav_settings`, `favourite_add` "Add to
favourites", `favourite_remove` "Remove from favourites" (L-13), `test_start` "Start a test" (L-14). French and Romanian
are drafted by the implementer and reviewed by the user in the PR before merge.

### D8. Previews
- One preview per Figma variant and state, each preceded by `// Figma: <variant node URL>`, in light and in dark
  (`VerbeClairTheme(darkTheme = …)`): Search bar Empty / Filled; Icon button Standard / Tonal / Tonal with badge;
  Button Filled / Tonal / Outlined / Text / Disabled; Chip Filter off / Filter on; Radio row selected / not selected;
  List tabs All / Favourites; Verb cell default / matched form / with score / favourite / long infinitive + matched form
  / selected; Section header; Skeleton; Empty state Neutral with action / Neutral without action / Error; Linear
  progress; Sheet handle; Bottom bar Verbs / Tests / Settings; Navigation rail Verbs / Tests / Settings.
- Configuration previews for the components whose layout depends on width: verb cell and list tabs at 393 dp (phone
  portrait, L-01), 386 dp (one landscape column, PL-01) and 360 dp (tablet list pane, TP-01); bottom bar at 393 dp;
  rail at heights 393 dp (landscape) and 1150 dp (tablet). Each with the Figma URL of that frame's instance.
- Verification after implementation with `android-preview-figma-verify`, preview against Figma node.

### D9. Tests
Compose UI tests in `core/designsystem/src/commonTest/.../component/` with `runComposeUiTest`:
- search bar: clear button only when filled; tapping it reports an empty query
- icon button: badge text shown for a count, absent for `null`; content description includes the count
- verb cell: matched form text present; score present only when given; favourite content description switches between
  the two L-13 labels; tapping the star or the test button calls only its callback, never `onClick`
- filter chip: selected semantics; `onClick` called in both states
- radio row: whole row clickable, `Role.RadioButton`, selected semantics
- list tabs: Favourites label contains the count; tapping reports the tab
- bottom bar / rail: exactly one selected item; tapping reports the destination
- empty state: action slot shown only when given
- skeleton and flag backgrounds absent from the semantics tree

## Risks / Trade-offs

- [`LocalMinimumInteractiveComponentSize` = 44 dp in the verb cell is below Android's 48 dp guideline] → it is the Figma
  size and above Apple's 44 pt minimum; the whole cell around the buttons is also tappable. If accessibility review
  asks for 48 dp, only the cell height changes.
- [M3 `PrimaryTabRow` / `NavigationBar` APIs in material3 `1.12.0-alpha03` may change] → wrapped behind our component
  API, so a library update only touches those files.
- [Badge text 11 sp is not a Figma text style] → mapped to `labelSmall`; if Figma adds a style, swap it in one place.
- [Drafted French and Romanian strings may be wrong] → listed in the PR description for the user's review; nothing is
  merged before that.
- [Section header and tablet selected cell rely on overrides, not variants] → recorded in previews with the instance
  URLs (10:339, 88:1053) so verification compares against the real usage.

## Migration Plan

- Additive: new package and string resources; one new flag helper. Nothing uses the components yet.
- Rollback: revert the work branch.
