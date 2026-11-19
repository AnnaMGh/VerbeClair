## ADDED Requirements

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
- **THEN** the card is still the first item, above the first sticky section header

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

## MODIFIED Requirements

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
