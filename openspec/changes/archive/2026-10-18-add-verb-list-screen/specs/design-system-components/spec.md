## MODIFIED Requirements

### Requirement: Empty state
The empty state (Figma 7:135) SHALL be a centred column: a 96 dp circle with a 40 dp icon, a title in Title/Large
`on-surface`, an optional body in Body/Medium `on-surface-variant`, both centred, and an optional Filled action button.
When no body is given, no body line or its spacing is drawn. **Neutral** tone: `primary-container` circle (no result,
no data, nothing selected in a list–detail pane). **Error** tone: `error-container` circle (verb unavailable, page
failed to open). The icon is decoration and SHALL be hidden from screen readers.

#### Scenario: Neutral empty state with action
- **WHEN** the neutral empty state is shown with the "Clear filters" action
- **THEN** it shows the circle on `primary-container`, the title, the body and a Filled "Clear filters" button

#### Scenario: Empty state without action
- **WHEN** the empty state is shown without an action
- **THEN** no button is shown

#### Scenario: Empty state without body
- **WHEN** the empty state is shown with the title "Choose a verb to see its conjugation" and no body
- **THEN** only the circle and the title are shown, with no empty line under the title

#### Scenario: Error empty state
- **WHEN** the error empty state is shown
- **THEN** its circle is on `error-container`

### Requirement: Bottom bar and navigation rail
The bottom bar (Figma 49:128) and the navigation rail (Figma 86:9531) SHALL show the three destinations **Verbs**
(book-open icon), **Tests** (list-checks icon) and **Settings** (settings icon), with exactly one selected. The selected
item has an indicator behind its icon (indigo-gray/80 in light mode, `secondary-container` in dark mode) and a Title/Small `on-surface` label; the others have no
indicator and a Label/Medium `on-surface-variant` label. The bottom bar is 80 dp high with a 1 dp `outline-variant` line
on top, and is opaque: the background colour plus the Blue flag stripe rotated 90° (N-14). The rail's items are in an 80 dp wide column
with a 1 dp `outline-variant` line on its right, items from the top, and the Blue flag stripe behind it (N-14, NV-4);
a window inset on its start side (a display cutout in landscape) widens the rail instead of narrowing that column. Tapping
an item reports that destination. Screen readers SHALL announce each item as a tab with its selected state.

#### Scenario: Bottom bar with Verbs selected
- **WHEN** the bottom bar is shown with Verbs selected
- **THEN** Verbs has the indicator and the `on-surface` label, and Tests and Settings do not

#### Scenario: Bottom bar with Tests or Settings selected
- **WHEN** Tests (or Settings) is selected
- **THEN** only that item has the indicator

#### Scenario: Navigation rail selection
- **WHEN** the rail is shown with any one destination selected
- **THEN** only that item has the indicator, and the rail fills the window height

#### Scenario: Tapping a destination
- **WHEN** the user taps Settings
- **THEN** the Settings destination is reported

#### Scenario: Flag stripe behind the bar
- **WHEN** content scrolls under the bottom bar
- **THEN** it does not show through, and the Blue stripe is visible at 12% opacity

#### Scenario: Navigation rail beside a display cutout
- **WHEN** the rail is shown in landscape with a 40 dp display cutout on its start side
- **THEN** the rail is 120 dp wide and its labels stay on one line
