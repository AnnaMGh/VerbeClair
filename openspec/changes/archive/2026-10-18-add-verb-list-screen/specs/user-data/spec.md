## Purpose

The user's own data on this device: favourites, test attempts with their line results, and the remembered verb-list
choices. It is kept apart from the replaceable verb catalogue so that it survives every update. Sources: Notion A-7,
T-9, the glossary (*Last test score*), L-7, L-12…L-14.

## ADDED Requirements

### Requirement: Separate user database
Favourites, test attempts and line results SHALL be stored in a user database separate from the verb database. They
are linked to verb data only by `slug`, mood `code`, tense `code` and `person` (null for a tense with no person), never
by internal row IDs or form positions. Installing a bundled verb database SHALL never change the user database. The
user database is never sent anywhere.

#### Scenario: Bundled database replaced
- **WHEN** a new bundled verb database replaces the local one
- **THEN** every favourite and test attempt is still there

### Requirement: Favourites
The user SHALL be able to add a verb to, and remove it from, their favourites. A change SHALL show at once on every
screen that observes favourites. The favourites count SHALL count only favourites whose verb exists in the local verb
database. A favourite of a verb that no longer exists SHALL be hidden, not deleted, and SHALL reappear if the verb
comes back.

#### Scenario: Add a favourite
- **WHEN** the user marks être as a favourite
- **THEN** être is a favourite and the favourites count goes up by one

#### Scenario: Remove a favourite
- **WHEN** the user removes être from the favourites
- **THEN** être is no longer a favourite and the count goes down by one

#### Scenario: Verb no longer available
- **WHEN** a favourite verb is missing from the local verb database
- **THEN** it is left out of the favourites count and the Favourites list, and it is still stored

#### Scenario: Verb comes back
- **WHEN** that verb is present again in the local verb database
- **THEN** it is again counted and listed as a favourite

### Requirement: Test attempts and last test score
The user database SHALL be able to store test attempts: the date, the selected tenses and, for every checked line, the
mood code, tense code, person, typed text and result (correct, correct without accents, failed). A verb's last test
score SHALL be the correct lines ÷ checked lines of its latest attempt, as a whole percentage rounded half up. A verb
with no attempt has no score. Lines correct without accents count as correct. A review session SHALL store, for each verb,
one review attempt holding the cards of that verb checked in the session; the latest attempt of either kind, test or
review, sets the last test score (RV-4). This change only reads attempts; the test and review screens that write them
come in later changes.

#### Scenario: Never tested
- **WHEN** a verb has no stored attempt
- **THEN** it has no last test score

#### Scenario: Latest attempt counts
- **WHEN** terminer has an attempt at 60% followed by a later attempt with 8 of 10 lines correct
- **THEN** its last test score is 80%

#### Scenario: Rounding
- **WHEN** the latest attempt has 2 of 3 lines correct
- **THEN** the last test score is 67%

#### Scenario: A later review session counts
- **WHEN** terminer has a test attempt at 80% followed by a later review session at 50%
- **THEN** its last test score is 50%

#### Scenario: Review session of several tenses
- **WHEN** one review session checks two tenses of avoir with 9 of 12 lines correct in total
- **THEN** avoir's last test score is 75%

#### Scenario: Accent-only answers
- **WHEN** the latest attempt has 9 correct lines, 1 line correct without accents, and no failed line
- **THEN** the last test score is 100%

### Requirement: Remembered list choices
The verb-list filters, grouping, sort and selected tab (All / Favourites) SHALL be kept after the app is closed and
restored when it opens. The search text SHALL NOT be kept. These choices are app settings, not user data, and are
stored outside the user database.

#### Scenario: Sort remembered
- **WHEN** the user picks "Most frequent" and reopens the app
- **THEN** the list is still sorted by frequency

#### Scenario: Tab remembered
- **WHEN** the user selects the Favourites tab and reopens the app
- **THEN** the Favourites tab is selected

#### Scenario: Search not remembered
- **WHEN** the user searches "su" and reopens the app
- **THEN** the search box is empty and the full list is shown
