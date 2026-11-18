## ADDED Requirements

### Requirement: First steps card
The first steps card (Figma 151:248) SHALL be a tip card with a `secondary-container` container, radius `lg` (16 dp)
and padding 16 dp (8 dp on the right, beside the close button). From top to bottom it holds:
- a header row: the title in Title/Small `on-secondary-container`, and on its right a close button (the `icon/close`
  icon, 18 dp, in a 8 dp padded target);
- the body in Body/Medium `on-secondary-container`, 7 dp under the title row;
- an actions row, 16 dp under the body, with two buttons 8 dp apart: the first **Filled**, the second **Tonal**
  (the Tonal container has the same colour as the card, so it reads as a label with no visible container).

Title, body, both button labels and the close button's accessible label SHALL be passed in by the screen, so the card
can be reused for other tips; the card itself has no fixed wording. The card fills the width it is given and its height
follows its content. The close button's touch target SHALL be at least 44 dp. Tapping a button or the close button SHALL
report that action and nothing else. The icon is decoration; the close button's label is what a screen reader reads.
The card is aligned the same way in every window size, except that on a wide window (phone landscape) the screen MAY
centre the actions row while title and body stay left-aligned.

#### Scenario: Card in phone portrait
- **WHEN** the card is shown in a 361 dp wide area with the title "New to French verbs?"
- **THEN** it is 142 dp high, with the title, body and two buttons stacked as in Figma 151:248 and the buttons aligned
  to the left

#### Scenario: Card in phone landscape
- **WHEN** the card is shown across a 740 dp wide area with the centred actions option
- **THEN** the title and body stay left-aligned, the close button stays at the right edge, and the buttons are centred
  (Figma PL-07)

#### Scenario: Card in dark mode
- **WHEN** the card is shown with the device in dark mode
- **THEN** it uses the dark values of the same tokens, with no extra styling

#### Scenario: Close button
- **WHEN** the user taps the close button
- **THEN** only the close action is reported, and a screen reader reads the label the screen passed in

#### Scenario: Larger system font
- **WHEN** the system font size is increased
- **THEN** the title, body and button labels grow and wrap, and nothing overlaps the close button

#### Scenario: Different tip
- **WHEN** another screen passes its own title, body and labels
- **THEN** the card shows them with the same layout and styles
