## ADDED Requirements

### Requirement: Knowing whether any attempt exists
The user data SHALL be able to tell whether at least one test or review attempt is stored, for any verb, and SHALL
report a change as soon as the first attempt is stored or the last one is removed. Attempts that arrive with an imported
file (S-13) count like any other attempt.

#### Scenario: Fresh install
- **WHEN** no attempt is stored
- **THEN** it reports that there is no attempt

#### Scenario: First attempt saved
- **WHEN** a first test or review attempt is stored
- **THEN** it reports that an attempt exists, at once

#### Scenario: Imported attempts
- **WHEN** a file with attempts is imported into an empty database
- **THEN** it reports that an attempt exists

### Requirement: First steps card closed flag
Whether the user closed the first steps card (OB-1) SHALL be kept after the app is closed. It is an app setting, not user
data: it is stored outside the user database, it SHALL NOT be part of any exported file, an import SHALL NOT change it,
and it is cleared when the app is reinstalled.

#### Scenario: Closed card stays closed
- **WHEN** the user closes the card and reopens the app
- **THEN** the card is still closed

#### Scenario: Not exported
- **WHEN** the user's data is exported after closing the card
- **THEN** the file holds no trace of the closed flag

#### Scenario: Import does not reopen it
- **WHEN** the card is closed and a file is imported
- **THEN** the card stays closed

#### Scenario: Reinstall
- **WHEN** the app is uninstalled and installed again
- **THEN** the card has not been closed
