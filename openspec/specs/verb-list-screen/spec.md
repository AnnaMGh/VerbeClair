# verb-list-screen Specification

## Purpose

Screen 1, the verb list VerbeClair opens on. It covers browsing, searching, filtering, grouping and sorting the 7,092
verbs, the All / Favourites tabs, the favourite and test buttons on each cell, the empty, no-data and loading states,
and the phone portrait, phone landscape and tablet layouts in light and dark mode. Sources: Notion section 6 (L-1…L-15
and the labels table), NV-1, NV-4, N-2, N-4…N-6, N-14, N-15, and Figma Screens L-01…L-09, PL-01, TP-01, TL-01.

## Requirements

### Requirement: Screen built from the design system and Figma
The screen SHALL match its Figma frames (Screens 3:65: L-01…L-10, PL-01, PL-07, TP-01, TP-07, TL-01, TL-07, light and
dark) and SHALL be built only from the existing design-system components and theme tokens: search bar, icon button with
badge, list tabs, verb cell, section header, verb cell skeleton, empty state, filter chip, radio row, button, first
steps card, linear progress, bottom sheet handle, bottom bar and navigation rail. It shows the white flag stripe behind
its content (N-14). Lists, rows and the top bar have no background of their own; the search bar and the sheet keep their
surface colour.

#### Scenario: Light mode
- **WHEN** the screen is shown with the device in light mode
- **THEN** it matches the Light frame of its current state

#### Scenario: Dark mode
- **WHEN** the screen is shown with the device in dark mode
- **THEN** it matches the Dark frame of its current state, with no extra styling

### Requirement: Top bar
The top of the screen SHALL show the search box and, on its right, the list-controls button. There is no settings
button. When the search box has text, a clear (×) button empties it.

#### Scenario: Order of the top bar
- **WHEN** the screen opens
- **THEN** the top bar shows, from left to right, the search box ("Search a verb…") and the list-controls button

#### Scenario: Clearing the search
- **WHEN** the search box holds "su" and the user taps the clear button
- **THEN** the search box is empty and the full list is shown

### Requirement: Verb list
The screen SHALL list every local verb, alphabetical by default, through the paged verb data (verb-data). Each cell
shows the infinitive in its original spelling and, below it, the meta line built from the labels table:
- group: `I` → "1st group", `II` → "2nd group", `III` → "3rd group"
- auxiliary: `AVOIR` → "avoir", `ETRE` → "être", `BOTH` → "avoir or être"
- reflexive: "reflexive" / "non-reflexive"
- frequency, in occurrences per million words: 100 or more → "very common", 10 or more → "common", 1 or more →
  "uncommon", below 1 (including 0) → "rare"

The parts are joined with " · ", and a line too long for the screen wraps. The slug is never shown.

#### Scenario: Default list
- **WHEN** the app opens with no search, no filters and the All tab
- **THEN** the verbs are listed A → Z and aller shows "aller" and "3rd group · être · non-reflexive · very common"
  (Figma L-01)

#### Scenario: Both auxiliaries label
- **WHEN** passer is shown
- **THEN** its meta line contains "avoir or être"

### Requirement: Search with matched forms
Typing in the search box SHALL filter the list after a 300 ms pause in typing, following the local search rules
(verb-data). When a verb matches through a conjugated form, its cell SHALL show that form right after the infinitive
in `primary`, wrapping to the next line when the infinitive is too long. The score and buttons are never moved.

#### Scenario: Search "su"
- **WHEN** the user types "su"
- **THEN** the list shows "être suis", "savoir su" and "suivre" without an extra form (Figma L-02)

#### Scenario: Debounce
- **WHEN** the user types "s", "su", "sui" within 300 ms
- **THEN** the list is filtered once, for "sui"

### Requirement: List controls sheet
The list-controls button SHALL open a bottom sheet titled "List options" with a **Reset** text button and three parts:
- **Filter**: chips for Group (1st group, 2nd group, 3rd group), Auxiliary (avoir, être) and Reflexive (Reflexive,
  Non-reflexive), multi-select.
- **Group by**: chips None, Group, Auxiliary, Reflexive, single choice.
- **Sort**: radio rows A → Z, Z → A, Most frequent.

Every change applies to the list at once. Reset restores no filters, no grouping and A → Z. The sheet keeps its phone
design on every window size, at most 640 dp wide and centred.

#### Scenario: Sheet open
- **WHEN** the user taps the list-controls button
- **THEN** the sheet shows the Filter, Group by and Sort parts with the current choices selected (Figma L-04)

#### Scenario: Reset
- **WHEN** filters, a grouping and a sort are selected and the user taps Reset
- **THEN** no filter is selected, Group by is None, Sort is A → Z, and the active choices row is hidden

#### Scenario: Sheet on a tablet
- **WHEN** the sheet opens in a window wider than 640 dp
- **THEN** it is at most 640 dp wide and centred horizontally

### Requirement: Badge and active choices row
The list-controls button SHALL show a badge with the number of selected filter values, and use the Tonal style when
any filter is selected. When any list choice differs from the default, a row of selected chips SHALL appear between the
tabs and the list. The row shows one chip per selected filter value, then the grouping ("By group", "By auxiliary",
"By reflexive"), then the sort ("Z → A", "Most frequent"). It scrolls horizontally when it doesn't fit. Tapping a chip
removes that choice: a filter chip deselects its value, the grouping chip sets Group by to None, and the sort chip sets
the sort back to A → Z. The badge and list update at once. With the default choices the row is hidden.

#### Scenario: Grouped list with two filters
- **WHEN** être and non-reflexive are selected, grouped by group and sorted by most frequent
- **THEN** the row reads "être", "Non-reflexive", "By group", "Most frequent", the badge shows 2, and the list has sticky
  section headers "1ST GROUP", "2ND GROUP", "3RD GROUP" without counts (Figma L-03)

#### Scenario: Removing a filter chip
- **WHEN** the user taps "être" in the row
- **THEN** the chip disappears, the badge shows 1, and avoir verbs are back in the list

#### Scenario: Removing the sort chip
- **WHEN** the user taps "Most frequent" in the row
- **THEN** the list is sorted A → Z

#### Scenario: Default choices
- **WHEN** no filter is selected, Group by is None and the sort is A → Z
- **THEN** the row is hidden and the list-controls button has no badge

### Requirement: All and Favourites tabs
Below the top bar, the screen SHALL show the tabs **All** and **Favourites (n)**, where n is the favourites count
(user-data). The Favourites tab lists only favourite verbs. Search, filters, grouping and sort apply to both tabs.

#### Scenario: Favourites tab
- **WHEN** there are 3 favourites and the user selects Favourites
- **THEN** the tab reads "Favourites (3)" and the list shows those 3 verbs (Figma L-08)

#### Scenario: Search in Favourites
- **WHEN** the Favourites tab is selected and the user searches "su"
- **THEN** only favourite verbs that match are shown

### Requirement: Favourite button
Each cell SHALL show the favourite star of the verb cell component: outlined when the verb is not a favourite, filled
in `ending` when it is. Tapping it adds or removes the favourite and does not open the verb. The accessible label is
"Add to favourites" / "Remove from favourites".

#### Scenario: Add from the list
- **WHEN** the user taps the outlined star on être
- **THEN** the star is filled, the Favourites tab count goes up by one, and no verb is opened

#### Scenario: Remove in the Favourites tab
- **WHEN** the Favourites tab is selected and the user taps a filled star
- **THEN** that verb leaves the Favourites list

### Requirement: Test button and score
Each cell SHALL show the test button to the left of the star, with the accessible label "Start a test". When the verb
has a last test score (user-data), it is shown in small text before the test button, for example "80%". Tapping the
test button SHALL report the verb's slug to the test-setup navigation callback and SHALL NOT open the verb.

#### Scenario: Tested verb
- **WHEN** terminer's last test score is 80%
- **THEN** its cell shows "80%" before the test button

#### Scenario: Never tested
- **WHEN** a verb has no attempt
- **THEN** its cell shows no score

#### Scenario: Tap the test button
- **WHEN** the user taps the test button on terminer
- **THEN** the test-setup callback receives "terminer" and the verb-details callback is not called

### Requirement: Opening a verb
Tapping a cell SHALL report the verb's slug to the verb-details navigation callback. Coming back to the list SHALL keep
the scroll position, search, filters and tab. Verb details themselves are outside this change.

#### Scenario: Tap a cell
- **WHEN** the user taps the terminer cell
- **THEN** the verb-details callback receives "terminer"

#### Scenario: Back to the list
- **WHEN** the user leaves the list and comes back
- **THEN** the list is at the same scroll position, with the same search, filters and tab

### Requirement: Empty result
When no verb matches the search and filters, the screen SHALL show the Neutral empty state, centred: the title "No verbs
match your search or filters", the body "Try another spelling, or clear the search and filters to see every verb.",
and a **Clear filters** button. Clear filters resets the filters and empties the search. Grouping and sort are kept.

#### Scenario: No match
- **WHEN** the user searches "xyz"
- **THEN** the empty result is shown in the middle of the list area (Figma L-05)

#### Scenario: Clear filters
- **WHEN** the user taps Clear filters
- **THEN** the search is empty, no filter is selected, and the list shows verbs again

#### Scenario: Favourites exist but none match
- **WHEN** the Favourites tab has favourites but none matches the search
- **THEN** the empty result is shown, not the no-favourites message

### Requirement: No data
When the local verb database holds no verb, the screen SHALL show the Neutral empty state with the title "No verbs
yet" and the body "Connect to the internet to download them.", with no button. It SHALL request a data sync. Sync does
not exist yet, so the request does nothing for now. A linear progress bar SHALL show under the tabs while a sync is
running. This state is distinct from the empty result: no filter action is shown.

#### Scenario: Empty database
- **WHEN** the verb database is empty
- **THEN** the no-data message is shown without a button (Figma L-06)

#### Scenario: Sync running
- **WHEN** the no-data state is shown while a sync is running
- **THEN** the linear progress bar is shown under the tabs

### Requirement: No favourites
When the Favourites tab is selected and there are no favourites at all, the screen SHALL show the Neutral empty state
with the title "No favourites yet" and the body "Tap ☆ on a verb to add it.", with no button.

#### Scenario: Last favourite removed
- **WHEN** the user removes the last favourite while the Favourites tab is selected
- **THEN** the no-favourites message is shown (Figma L-09)

### Requirement: Loading skeleton
While a page of the list is loading, the screen SHALL show verb cell skeleton rows in its place. The skeleton SHALL only
appear if the page takes longer than about 150 ms, so fast loads show no flash.

#### Scenario: Slow page
- **WHEN** a page takes longer than about 150 ms to load
- **THEN** skeleton rows are shown until it arrives (Figma L-07)

#### Scenario: Fast page
- **WHEN** a page loads in under 150 ms
- **THEN** no skeleton is shown

### Requirement: Navigation bar and rail
The verb list SHALL be shown with the bottom bar when the window is narrower than 600 dp, and with the navigation rail
on the left at 600 dp or wider. In both, Verbs is selected. The size class is read from the current window, so
rotating or resizing switches between bar and rail and keeps the screen's state. The Tests and Settings destinations
are not built yet.

#### Scenario: Phone portrait
- **WHEN** the window is 393 dp wide
- **THEN** the bottom bar is shown with Verbs selected

#### Scenario: Rotate to landscape
- **WHEN** the phone is rotated to landscape (852 × 393 dp)
- **THEN** the rail replaces the bottom bar, Verbs is still selected, and the search, filters and scroll position are
  kept

### Requirement: Phone landscape layout
When the window is at least 600 dp wide and less than 480 dp high, the screen SHALL show one pane next to the rail. The
search box, the tabs and the list-controls button share one top row. Verb cells flow in 2 columns, with the same cell as
in portrait. In a grouped list, section headers stay full-width and sticky. The active choices row, when shown, sits
under the top row.

#### Scenario: Landscape list
- **WHEN** the verb list is shown at 852 × 393 dp
- **THEN** the top row holds search, tabs and list controls, and the cells flow in 2 columns (Figma PL-01)

#### Scenario: Landscape grouped list
- **WHEN** the list is grouped by group in landscape
- **THEN** each section header spans both columns and stays pinned while its section scrolls

### Requirement: Tablet list–detail layout
When the window is at least 600 dp wide and at least 480 dp high, the screen SHALL show a list–detail layout next to
the rail: the verb list in a 360 dp pane on the left (top bar, tabs, active choices row and list), a divider, and a
right pane. Tapping a cell selects it: the cell background becomes `secondary-container` and the verb-details callback
receives the slug. Verb details are not built in this change, so the right pane shows the Neutral empty state with the
title "Choose a verb to see its conjugation", and no button, whether or not a verb is selected, except that while the
first steps card is shown and no verb is selected it shows the top 10 panel (Top 10 panel on tablets). The first steps
card is the first item of the left pane. On Medium width, back first clears the selection. Rotating or resizing keeps
the selection and scroll position.

#### Scenario: Tablet portrait
- **WHEN** the screen is shown at 834 × 1194 dp
- **THEN** the rail, the 360 dp list pane and the right pane are shown, with the list pane matching Figma TP-01

#### Scenario: Tablet landscape
- **WHEN** the screen is shown at 1194 × 834 dp
- **THEN** the list pane matches Figma TL-01

#### Scenario: Selecting a verb on a tablet
- **WHEN** the user taps the terminer cell on a tablet
- **THEN** the terminer cell shows the selected background and the verb-details callback receives "terminer"

#### Scenario: Nothing selected
- **WHEN** no verb is selected on a tablet and the first steps card is not shown
- **THEN** the right pane shows "Choose a verb to see its conjugation"

#### Scenario: Nothing selected with the card shown
- **WHEN** no verb is selected on a tablet and the first steps card is shown
- **THEN** the right pane shows the top 10 panel and the left pane starts with the card (Figma TP-07, TL-07)

### Requirement: Accessibility and languages
Every control on the screen SHALL have an accessible label, and text SHALL grow with the system font size without
being cut or overlapping the cell buttons. Colour is never the only cue. The screen's own text (sheet titles and labels,
chip labels, active choice labels, empty-state copy, meta-line labels) SHALL come from string resources in English,
French and Romanian, following the device language. Verb data (infinitives, forms) is always shown in French.

#### Scenario: Device in French
- **WHEN** the device language is French
- **THEN** the sheet, chips, meta labels and empty states are in French, and infinitives are unchanged

#### Scenario: Largest font size
- **WHEN** the system font size is at its largest
- **THEN** the infinitive and meta line wrap, and nothing overlaps the score, test button or star

### Requirement: Failed favourite change message
When adding or removing a favourite fails, the verb list SHALL show a snackbar with the message "Couldn’t update favourites.
Try again." (translated in French and Romanian), with the alert icon and no action, and SHALL leave the star as it was.
The snackbar sits at the bottom of the screen content: above the bottom bar on a phone, and clear of the navigation rail
on phone landscape and tablet. It is shown in light and dark mode.

#### Scenario: Add fails
- **WHEN** the user taps the star of a verb that is not a favourite and the write fails
- **THEN** the snackbar is shown and the star is still empty

#### Scenario: Remove fails
- **WHEN** the user taps the star of a favourite and the write fails
- **THEN** the snackbar is shown and the star is still filled

#### Scenario: Change succeeds
- **WHEN** the user taps a star and the write succeeds
- **THEN** no snackbar is shown

#### Scenario: Phone portrait, phone landscape and tablet
- **WHEN** the snackbar is shown on a phone in portrait, a phone in landscape or a tablet
- **THEN** it is at most 361 dp wide, centred at the bottom of the content, and does not cover the bottom bar or the rail

### Requirement: First steps card
In the All tab, with an empty search and no active choice (no filter, grouping or sort), the screen SHALL show the first steps card
(OB-1) as the first item of the list, scrolling with it, while the user has no saved test or review attempt (user-data)
and has not closed the card. The card reads "New to French verbs? Start with the most common ones." and has two buttons
and a close button:
- **Test être** reports the slug "etre" to the test-setup navigation callback, as the test button of a cell does.
- **Most common first** sets the sort to Most frequent, so the sort chip appears in the active choices row and the
  card is hidden, because a sort is an active choice.
- **Close** (accessible label "Close tip") hides the card for good (user-data).

The card is also hidden in the Favourites tab, while the search box has text, while any filter, grouping or sort is
active (any chip in the active choices row), in the empty result, no-data and no-favourites states, and while the first
page of the list is still loading (skeleton). Removing the last chip (or Reset or Clear filters) shows it again. Once any attempt is saved, including one that arrives with an imported file (S-13), the card
is hidden for good. The card text, the button labels and the close label SHALL come from string resources in English,
French (formal) and Romanian (informal).

On phone portrait and on a tablet the card is as wide as the list and its buttons are left-aligned. On phone landscape
it spans both columns above the verb cells, its title and body are left-aligned and its buttons are centred; it scrolls
with the list and is not a sticky header.

#### Scenario: Fresh install
- **WHEN** the app opens for the first time with the All tab, no search and no filter
- **THEN** the card is the first item, above "abaisser" (Figma L-10)

#### Scenario: Most common first
- **WHEN** the user taps "Most common first"
- **THEN** the list is sorted by frequency, the "Most frequent" chip is shown, and the card is hidden

#### Scenario: Removing the last chip brings the card back
- **GIVEN** the card was hidden by the "Most frequent" chip
- **WHEN** the user removes the chip
- **THEN** the card is the first item again, as long as there is no saved attempt and it was not closed

#### Scenario: Test être
- **WHEN** the user taps "Test être"
- **THEN** the test-setup callback receives "etre" and the verb-details callback is not called

#### Scenario: Close
- **WHEN** the user taps the close button
- **THEN** the card disappears, and it does not come back after the app is reopened

#### Scenario: First attempt saved
- **WHEN** a test or review attempt is saved
- **THEN** the card is gone and never returns

#### Scenario: Imported attempts
- **WHEN** a file that holds attempts is imported while the card is shown
- **THEN** the card disappears

#### Scenario: Searching
- **WHEN** the user types in the search box
- **THEN** the card is hidden, and it is shown again when the search is empty

#### Scenario: Filter selected
- **WHEN** a filter is selected
- **THEN** the card is hidden

#### Scenario: Grouped list
- **WHEN** the list is grouped
- **THEN** the card is hidden, and it is shown again when the grouping is removed

#### Scenario: Other states
- **WHEN** the Favourites tab is selected, or the empty result, no-data, no-favourites or loading skeleton is shown
- **THEN** the card is not shown

#### Scenario: Scrolling
- **WHEN** the user scrolls the list down
- **THEN** the card scrolls away with the cells and does not stay pinned

#### Scenario: Phone portrait
- **WHEN** the card is shown at 393 × 852 dp
- **THEN** it matches Figma L-10, light and dark, with the buttons left-aligned

#### Scenario: Phone landscape
- **WHEN** the card is shown at 852 × 393 dp
- **THEN** it spans both columns above the cells, with title and body left-aligned and the buttons centred, light and
  dark (Figma PL-07)

#### Scenario: Device in French or Romanian
- **WHEN** the device language is French or Romanian
- **THEN** the card reads "Vous débutez avec les verbes français ? Commencez par les plus courants." with the buttons
  "Tester être" and "Les plus courants d'abord" and the close label "Fermer le conseil", or "Abia ai început să înveți
  verbele în franceză? Începe cu cele mai frecvente." with "Testează être", "Cele mai frecvente primele" and
  "Închide sfatul"

### Requirement: Top 10 panel on tablets
On a tablet (list–detail layout), while the first steps card is shown and no verb is selected, the right pane SHALL show
the top 10 panel instead of the empty state: the title "Does any of these catch your eye?" and a plain list of the 10
most frequent non-reflexive verbs (the verb data's most-frequent sort with the Non-reflexive filter, 10 items), from the
most to the least frequent: être, avoir, aller, faire, dire, pouvoir, vouloir, savoir, voir, devoir. Each row shows the
infinitive only, with no score, rank, button or chevron. The panel title is translated in French (formal) and Romanian
(informal): "L'un de ces verbes vous attire-t-il ?" and "Îți atrage atenția vreunul dintre aceste verbe?". Tapping a verb
in the panel behaves like tapping its cell in the list. When the card is closed or hidden, or a verb is selected, the
panel is not shown and the right pane behaves as in the tablet list–detail layout. If the selection is cleared while the
card is still shown, the panel comes back.

#### Scenario: Card shown, nothing selected
- **WHEN** the card is shown on a tablet and no verb is selected
- **THEN** the right pane shows the title and the 10 verbs from être to devoir, with no reflexive verb such as "se
  pouvoir" (Figma TP-07 and TL-07)

#### Scenario: Tap a verb in the panel
- **WHEN** the user taps "aller" in the panel
- **THEN** the aller cell in the list shows the selected background, the verb-details callback receives "aller", and the
  panel is replaced by the right pane as for any selected verb

#### Scenario: Tap a verb in the list
- **WHEN** the card is shown and the user taps the terminer cell
- **THEN** the panel is replaced by the right pane as for any selected verb

#### Scenario: Selection cleared
- **WHEN** the card is still shown and the selection is cleared (back on Medium width)
- **THEN** the panel is shown again

#### Scenario: Card closed
- **WHEN** the user closes the card on a tablet
- **THEN** the right pane shows "Choose a verb to see its conjugation"

#### Scenario: Card hidden by a search
- **WHEN** the card is hidden because the search box has text
- **THEN** the right pane shows "Choose a verb to see its conjugation"

#### Scenario: Dark mode
- **WHEN** the panel is shown with the device in dark mode
- **THEN** it matches the dark frames TP-07 and TL-07
