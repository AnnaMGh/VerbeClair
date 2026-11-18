## Context

See `proposal.md` for the why and the scope. Current state that shapes the approach:

- The verb list (`feature/verblist`) draws its paged rows in a `LazyVerticalGrid` (2 columns on phone landscape, 1 otherwise). Section headers are sticky and span the full width. On tablets `ListPane` (360 dp) sits next to `DetailPlaceholder`, which shows the Neutral empty state.
- Paging comes from `VerbCatalog.pagedList(ListQuery)`. `TestScoreRepository` only has `observeScores(slugs)`. `ListPreferences` holds the list choices and the tab in the app-settings DataStore.
- Verb details do not exist yet, so a selected verb only calls the verb-details callback; the right pane stays the empty state.
- The card is in Figma as the component "First steps card" (`151:248`) and in the frames listed in the proposal.

## Goals / Non-Goals

**Goals:**
- Show the card and the tablet top-10 panel exactly by the rules in the specs, in the three layouts and both themes.
- Make the card a reusable design-system component with no first-steps wording inside it.

**Non-Goals:**
- No verb-details screen, no change to test setup, no sync.
- No new export or import code. This change only has to keep the closed flag out of future exports and read imported attempts.
- No analytics or A/B behaviour.

## Decisions

### D1. The card is a grid item outside paging
The card is `item(key = "first-steps", span = full width)` before the paged rows, not a `PagingData` item. Paging stays a pure list of verbs; inserting a separator into `PagingData` would complicate counts and scroll restoration. A full-span item covers phone landscape (both columns) with no extra code. It is not a `stickyHeader`, so it scrolls away.
*Alternative:* a fixed card under the tabs. Rejected: the product owner wants it to scroll with the list, and a fixed card would take list height on landscape.

### D2. Visibility is computed in the ViewModel, not in the composable
`VerbListState` gets `showFirstSteps: Boolean`. It is true when: tab is All, search is empty, no filter is selected, the verb database is not empty, the card is not closed and no attempt exists. The first-page skeleton and empty-result conditions depend on paging load state, which only the screen sees. So the screen draws the card only when `state.showFirstSteps` **and** the paged list has content (first page loaded, not empty). The tablet panel uses the same combined value, so both always agree.
*Alternative:* put all conditions in the screen. Rejected: harder to unit test; the ViewModel part is testable with fakes.

### D3. Two small sources feed the rule
- **Closed flag:** a new small interface in `core/domain` (`FirstStepsPreferences`: `closed: Flow<Boolean>`, `close()`), backed by the existing app-settings DataStore in `core/data` as a separate key. It is not part of `ListPreferences`, because it is not a list choice, and it is never read by export code.
- **Any attempt:** `TestScoreRepository.observeHasAnyAttempt(): Flow<Boolean>`, backed by a Room query (`EXISTS`) on the attempts table in the user database. It reads imported attempts the same way as any other, so S-13 needs no special case.
Both flows are combined with the list state in the ViewModel.

### D4. Actions
New actions: `OnFirstStepsClose`, `OnFirstStepsTestEtre`, `OnFirstStepsMostCommon`. `OnFirstStepsMostCommon` reuses the existing sort action with Most frequent, so the sort chip and remembered choice follow the normal path. `OnFirstStepsTestEtre` raises the same navigation event as the cell test button, with slug `etre` (checked in the bundled dataset). Closing calls `FirstStepsPreferences.close()`; the state follows from the flow, with no local flag.

### D5. The top 10 verbs use the existing catalogue query
The panel reads `VerbCatalog.pagedList(ListQuery(sort = MOST_FREQUENT, reflexive = NON_REFLEXIVE))` limited to the first 10 items, or a small `top(limit)` method on `VerbCatalog` if reading from `PagingData` proves awkward. No hard-coded list: the dataset can change on sync. Computed against the bundled dataset (v3) the result is être, avoir, aller, faire, dire, pouvoir, vouloir, savoir, voir, devoir.

### D6. Detail pane switching
`DetailPlaceholder` takes the combined `showFirstSteps` and `selectedSlug`: panel when `showFirstSteps && selectedSlug == null`, the Neutral empty state otherwise. A tap on a panel row sends the existing `OnSelectVerb`, so selection, the callback and the "back clears the selection" rule need no new code.

### D7. The card is a design-system component
`FirstStepsCard` goes in `core/designsystem/component`, taking `title`, `body`, `primaryLabel`, `secondaryLabel`, `closeLabel`, the three callbacks and an `actionsAlignment` (Start by default, Center for phone landscape). Existing components are reused: the Filled and Tonal buttons from the button component, the `icon/close` icon from the icon set. The strings live in the feature module, because the card has no fixed wording.
*Alternative:* put the card in `feature/verblist`. Rejected: the product owner wants it reusable.

### D8. Tokens and measures (from Figma `get_design_context` and `get_variable_defs`)
- Container `color/secondary-container`, radius `radius/lg` (16), padding `space/16` with `space/8` on the right, 361 dp wide in the component, 142 dp high.
- Title: Title/Small (DM Sans SemiBold 14/20, tracking 0.1), `color/on-secondary-container`. Body: Body/Medium (DM Sans Regular 14/20, tracking 0.2), same colour, 7 dp under the title row. Buttons: Label/Large (Medium 14/20), 40 dp high, radius `radius/full`, horizontal padding `space/24`, gap `space/8`, 16 dp above the row. First button `color/primary` with `color/on-primary`; second `color/secondary-container` with `color/on-secondary-container`.
- Close: `icon/close` 18 dp in an 8 dp padded box (34 dp). The touch target is raised to 44 dp with the minimum-interactive-size modifier so the visual size matches Figma and the target meets the project rule.
- Phone landscape card is 740 dp wide in the frame; the header's left padding is balanced against the close button so the text stays left-aligned and only the actions row is centred (Figma PL-07).
- Dark mode uses the same tokens; no hard-coded colours.

### D9. Layout by window
Phone portrait and tablet use `columns = 1`, so the card is simply the first grid item. Phone landscape uses `GridItemSpan(maxLineSpan)` with `actionsAlignment = Center`. On a tablet the card is the first item of the 360 dp pane; the panel is a separate composable in the right pane with the title in Title/Large and plain rows of the infinitive in the verb-infinitive text style, matching TP-07 and TL-07. The panel does not scroll with the list.

## Risks / Trade-offs

- [The card could flash for a frame on startup before the closed flag and the attempt flag arrive] → keep `showFirstSteps` false until both flows have emitted their first value.
- [The second button has the same colour as the card, so it looks like plain text; a user might not see it as a button] → it follows the Figma design; the 40 dp height and the 44 dp target keep it tappable, and the Tonal style is kept in code so it follows the theme.
- [Reading the top 10 through paging may emit loading states] → the panel shows nothing until the 10 rows are loaded, and D5 allows a small catalogue method if needed.
- [Hiding the card on "first page loading" depends on paging load state] → covered by a UI test with a slow fake page.
- [Extra DataStore key] → one boolean; no migration. Reinstall clears it, as required.

## Migration Plan

None. No schema change, no data migration. The new DataStore key defaults to "not closed". Rolling back means removing the card; the unused key is harmless.
