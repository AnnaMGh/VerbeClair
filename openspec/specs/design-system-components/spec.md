# design-system-components Specification

## Purpose

Defines the shared, themed UI components from the Figma Components page (3:64) that the verb list and later screens
are built from: their visible states, what the user can do with them, their accessibility labels and how they look in
light and dark mode and on every supported window size.

## Requirements

### Requirement: Components come from the Figma Components page and the theme
Every component in this capability SHALL match its component set on the Figma Components page (3:64): layout, sizes,
spacing, radius, typography and colour roles. Colours, text styles, spacing and radii SHALL come only from the
VerbeClair theme (design-system-foundation); no value SHALL be hard-coded. Icons SHALL be the VerbeClair icon set.
Screens SHALL use these components instead of drawing their own versions.

#### Scenario: A component in light mode
- **WHEN** any component is shown with the device in light mode
- **THEN** every colour it draws is the Light value of the Figma variable bound to that layer

#### Scenario: A component in dark mode
- **WHEN** any component is shown with the device in dark mode
- **THEN** every colour it draws is the Dark value of the Figma variable bound to that layer, with no extra styling

### Requirement: Search bar
The search bar (Figma 5:56) SHALL be a fully rounded field on `surface-container-high`, 52 dp high, with the search
icon on the left. **Empty**: the placeholder "Search a verb…" in `on-surface-variant`. **Filled**: the typed text in
`on-surface` and a clear (×) button on the right. Typing reports each change of the text; the clear button empties the
text. The clear button SHALL have the accessible label "Clear search".

#### Scenario: Empty search bar
- **WHEN** the search text is empty
- **THEN** the bar shows the search icon and "Search a verb…", and no clear button

#### Scenario: Filled search bar
- **WHEN** the user has typed "su"
- **THEN** the bar shows "su" and a clear button

#### Scenario: Clearing the search
- **WHEN** the user taps the clear button
- **THEN** the text becomes empty and the bar returns to the Empty state

### Requirement: Icon button with badge
The icon button (Figma 5:39) SHALL have a 48 dp touch target and a 40 dp circular state layer. **Standard**: a
transparent state layer and an `on-surface-variant` icon. **Tonal**: a `secondary-container` state layer. When a count
is given, a badge in `error` with the number in `on-error` sits at the top right of the icon. The button SHALL have an
accessible label given by the caller; when a badge is shown, the label SHALL include the count.

#### Scenario: Standard icon button without badge
- **WHEN** the list-controls button is shown with no active filter
- **THEN** it uses the Standard style and shows no badge

#### Scenario: Tonal icon button with badge
- **WHEN** the list-controls button is shown with 2 active filter values
- **THEN** it uses the Tonal style and the badge shows "2"

#### Scenario: Badge read by a screen reader
- **WHEN** a screen reader focuses the list-controls button with 2 active filter values
- **THEN** it reads the button label together with the count 2

### Requirement: Buttons
The button (Figma 5:89) SHALL be 40 dp high and fully rounded, with a Label/Large label and an optional 18 dp leading
icon, in five styles: **Filled** (`primary` / `on-primary`), **Tonal** (`secondary-container` /
`on-secondary-container`), **Outlined** (`outline` border, `primary` label), **Text** (`primary` label, no container)
and **Disabled** (`on-surface` at 12% behind an `on-surface` label at 38%). A disabled button SHALL not react to taps.

#### Scenario: Filled button
- **WHEN** the "Clear filters" action of an empty state is shown
- **THEN** it is a Filled button with an `on-primary` label on `primary`

#### Scenario: Text button
- **WHEN** the Reset action of the list-controls sheet is shown
- **THEN** it is a Text button with a `primary` label and no container

#### Scenario: Tonal and Outlined buttons
- **WHEN** a Tonal or an Outlined button is shown
- **THEN** the Tonal one has a `secondary-container` container and the Outlined one a 1 dp `outline` border

#### Scenario: Disabled button
- **WHEN** a button is disabled and the user taps it
- **THEN** it shows the Disabled style and nothing happens

### Requirement: Filter chips
The filter chip (Figma 5:101) SHALL be 32 dp high with radius `sm`. **Filter off**: a 1 dp `outline-variant` border and
an `on-surface-variant` label. **Filter on**: a `secondary-container` container, an 18 dp check icon and an
`on-secondary-container` label. A chip SHALL react to taps in both states, so a screen can use it to toggle a value (the
list-controls sheet, L-5) or to remove an active choice (the active choices row, L-6). A screen reader SHALL announce
whether the chip is selected.

#### Scenario: Chip not selected
- **WHEN** the "1st group" filter is not selected
- **THEN** its chip shows the Filter off style without a check icon

#### Scenario: Chip selected
- **WHEN** the "être" filter is selected
- **THEN** its chip shows the Filter on style with a check icon

#### Scenario: Tapping a chip
- **WHEN** the user taps a chip
- **THEN** the tap is reported to the screen that shows it, in either state

### Requirement: Radio row
The radio row (Figma 5:113) SHALL be a 48 dp single-choice row: a 20 dp radio on the left and a Body/Large label in
`on-surface`. The whole row SHALL be the tap target, and a screen reader SHALL announce it as a radio button with its
selected state.

#### Scenario: Selected radio row
- **WHEN** "A → Z" is the current sort
- **THEN** its row shows a selected radio

#### Scenario: Unselected radio row tapped
- **WHEN** the user taps anywhere on the "Most frequent" row
- **THEN** the selection is reported for that row

### Requirement: List tabs
The list tabs (Figma 49:145) SHALL show two equal-width tabs, **All** and **Favourites (n)**, with a 1 dp
`outline-variant` line under them. The selected tab's label is `primary` with a 64 × 3 dp `primary` indicator under it;
the other label is `on-surface-variant`. The Favourites label SHALL contain the favourites count given by the caller.

#### Scenario: All selected
- **WHEN** the All tab is selected and there are 12 favourites
- **THEN** "All" is in `primary` with the indicator, and "Favourites (12)" is in `on-surface-variant`

#### Scenario: Favourites selected
- **WHEN** the user taps "Favourites (12)"
- **THEN** the tab selection is reported, and once selected the indicator is under "Favourites (12)"

### Requirement: Verb cell
The verb cell (Figma 7:41) SHALL show one verb: the infinitive in Title/Verb (`on-surface`) and, under it, the meta
line in Body/Medium (`on-surface-variant`). Both wrap rather than being cut. On the right, at the cell edge: the last
test score in Label/Large (`on-surface-variant`) when there is one, the test button (exam icon) and the favourite
button, each with a 44 dp touch target (L-8, L-14). The cell has a 1 dp `outline-variant` line at the bottom.
- **Matched form** (L-4): when given, the form is shown on the infinitive's line, right after it with one space, in
  Label/Large `primary`, with no chip or background; when the line is too short, the form wraps to the next line.
  The score and buttons are never moved or overlapped.
- **Favourite** (L-13): an outlined star in `on-surface-variant` when the verb is not a favourite, a filled star in
  `ending` when it is. Accessible labels: "Add to favourites" / "Remove from favourites".
- **Test button**: accessible label "Start a test".
- **Selected** (tablet list–detail, N-15): the cell background is `secondary-container`.
- Tapping the cell, the test button and the favourite button are reported separately; tapping a button never counts
  as tapping the cell.

#### Scenario: Default cell
- **WHEN** the cell shows aller with no score, not a favourite
- **THEN** it shows "aller", "3rd group · être · non-reflexive · very common", the test button and an outlined star, and
  no score

#### Scenario: Cell with matched form
- **WHEN** the cell shows être with the matched form "suis"
- **THEN** "suis" appears in `primary` right after "être" on the same line

#### Scenario: Long infinitive with matched form
- **WHEN** the infinitive fills the line and a matched form is given
- **THEN** the form wraps to the next line and the score and buttons stay in place

#### Scenario: Cell with score
- **WHEN** terminer was last tested at 80%
- **THEN** "80%" is shown before the test button

#### Scenario: Favourite cell
- **WHEN** the verb is a favourite
- **THEN** the star is filled in `ending` and a screen reader reads "Remove from favourites"

#### Scenario: Tapping the favourite button
- **WHEN** the user taps the star
- **THEN** a favourite toggle is reported and no cell tap is reported

#### Scenario: Tapping the test button
- **WHEN** the user taps the test icon
- **THEN** a test tap is reported and no cell tap is reported

#### Scenario: Selected cell on a tablet
- **WHEN** the cell is the selected verb in the tablet list–detail layout
- **THEN** its background is `secondary-container`

### Requirement: Section header
The section header (Figma 7:45) SHALL be a row on `surface-container` with the section title on the left in
Label/Overline `primary`, uppercase. It MAY show a count on the right in Label/Medium `on-surface-variant` (for example
"5,684 verbs"); the grouped verb list does not show one (L-03, decided by the user on 2026-10-16). It SHALL be usable
as a sticky header.

#### Scenario: Section header in a grouped list
- **WHEN** a section header is shown for the 1st group in the grouped verb list
- **THEN** it shows "1ST GROUP" on the left and no count

#### Scenario: Section header with a count
- **WHEN** a section header is given the count "5,684 verbs"
- **THEN** it shows "1ST GROUP" on the left and "5,684 verbs" on the right

### Requirement: Verb cell skeleton
The verb cell skeleton (Figma 7:49) SHALL show two rounded placeholder bars in `surface-container-highest`
(120 × 18 dp and 210 × 12 dp, radius `xs`) with the verb cell's bottom line. It SHALL be hidden from screen readers.

#### Scenario: Skeleton row
- **WHEN** a skeleton row is shown
- **THEN** it shows the two placeholder bars and a screen reader skips it

### Requirement: Empty state
The empty state (Figma 7:135) SHALL be a centred column: a 96 dp circle with a 40 dp icon, a title in Title/Large
`on-surface`, a body in Body/Medium `on-surface-variant`, both centred, and an optional Filled action button. **Neutral**
tone: `primary-container` circle (no result, no data). **Error** tone: `error-container` circle (verb unavailable,
page failed to open). The icon is decoration and SHALL be hidden from screen readers.

#### Scenario: Neutral empty state with action
- **WHEN** the neutral empty state is shown with the "Clear filters" action
- **THEN** it shows the circle on `primary-container`, the title, the body and a Filled "Clear filters" button

#### Scenario: Empty state without action
- **WHEN** the empty state is shown without an action
- **THEN** no button is shown

#### Scenario: Error empty state
- **WHEN** the error empty state is shown
- **THEN** its circle is on `error-container`

### Requirement: Linear progress and sheet handle
The linear progress (Figma 7:151) SHALL be an indeterminate 4 dp bar: a `primary` indicator on a `primary-container`
track. The bottom sheet handle (Figma 7:154) SHALL be a centred 32 × 4 dp fully rounded bar in `on-surface-variant` at
40%, with 16 dp above and below.

#### Scenario: Progress shown
- **WHEN** the linear progress is shown
- **THEN** a `primary` indicator moves along a `primary-container` track

#### Scenario: Sheet handle
- **WHEN** a bottom sheet opens
- **THEN** its handle is drawn centred at the top

### Requirement: Bottom bar and navigation rail
The bottom bar (Figma 49:128) and the navigation rail (Figma 86:9531) SHALL show the three destinations **Verbs**
(book-open icon), **Tests** (list-checks icon) and **Settings** (settings icon), with exactly one selected. The selected
item has an indicator behind its icon (indigo-gray/80 in light mode, `secondary-container` in dark mode) and a Title/Small `on-surface` label; the others have no
indicator and a Label/Medium `on-surface-variant` label. The bottom bar is 80 dp high with a 1 dp `outline-variant` line
on top, and is opaque: the background colour plus the Blue flag stripe rotated 90° (N-14). The rail is 80 dp wide with
a 1 dp `outline-variant` line on its right, items from the top, and the Blue flag stripe behind it (N-14, NV-4). Tapping
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

### Requirement: Component text is localised
The fixed text of the components ("Search a verb…", "All", "Favourites (n)", "Verbs", "Tests", "Settings", "Clear
search", "Add to favourites", "Remove from favourites", "Start a test") SHALL come from string resources in English,
French and Romanian, following the device language (N-6). Text passed in by a screen (verb data, labels, counts) is
shown as given.

#### Scenario: Device in French
- **WHEN** the device language is French
- **THEN** the components show their French text

#### Scenario: Unsupported device language
- **WHEN** the device language is neither French nor Romanian
- **THEN** the components show their English text

### Requirement: Components adapt to every supported configuration
Every component SHALL keep its design in phone portrait, phone landscape and tablet windows, in light and dark mode:
widths follow the space the screen gives them (no fixed screen width), heights follow the content, text follows the
system font size, and touch targets stay at least 44 dp.

#### Scenario: Phone portrait
- **WHEN** a verb cell is shown full width in a 393 dp wide window
- **THEN** it matches the Figma phone frames (L-01)

#### Scenario: Phone landscape
- **WHEN** verb cells are shown in two columns of a 772 dp wide pane
- **THEN** each cell fills its column and matches the Figma landscape frame (PL-01)

#### Scenario: Tablet
- **WHEN** the verb list is shown in the 360 dp list pane of a tablet
- **THEN** cells, tabs and the search bar fill the pane and match the Figma tablet frames (TP-01, TL-01)

#### Scenario: Larger system font
- **WHEN** the system font size is increased
- **THEN** text grows and wraps, and no text is cut or overlaps the buttons
