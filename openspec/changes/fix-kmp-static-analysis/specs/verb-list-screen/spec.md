## ADDED Requirements

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
