# verb-data Specification

## Purpose

The offline verb catalogue. It covers how the app gets its 7,092 French verbs without a network: the bundled database
built from the pinned dataset release, how it is installed on the device, and the local search, filter, group and sort
rules the verb list relies on. Sources: Notion A-1…A-4, L-1, L-3…L-5, N-1…N-3 and the API page (*Get verb details*,
*How search works*, *Reference*).

## Requirements

### Requirement: Pinned dataset download
The build SHALL download the dataset file `french-verbs-dataset-v<N>.json.gz` from the GitHub release
`french-verbs-dataset-v<N>` of the private VAMVisionsKMP repository. The version `<N>` and its SHA-256 come from two
committed lines in `gradle.properties` (`frenchVerbsDataset.version`, `frenchVerbsDataset.sha256`). The download
SHALL authenticate with a fine-grained GitHub token read from `local.properties` (or from an environment variable in
CI), which SHALL never be committed. A file is downloaded once per version and kept under `build/`. Only the two pinned
lines change when a new version is published; the dataset file itself SHALL never be committed.

#### Scenario: Pinned version with the right checksum
- **WHEN** the build runs with `frenchVerbsDataset.version=3` and the matching SHA-256
- **THEN** it downloads `french-verbs-dataset-v3.json.gz` once and uses it for the bundled database

#### Scenario: Wrong checksum
- **WHEN** the downloaded file's SHA-256 differs from the pinned one
- **THEN** the build fails and says the checksum does not match

#### Scenario: Missing or rejected token
- **WHEN** the token is missing from `local.properties` (and the environment), or GitHub rejects it
- **THEN** the build fails with a message naming the missing property

#### Scenario: Second build of the same version
- **WHEN** the file for the pinned version is already under `build/`
- **THEN** the build does not download it again

### Requirement: Bundled verb database
The app SHALL ship a prebuilt verb database containing every verb, form, translation and synonym of the dataset. The
database SHALL be created by the same database definition and the same mapping code the app uses at runtime. Next to
it, the app SHALL ship a metadata file with the snapshot's `lastUpdatedAt`, the database schema version, a
`bundleBuild` number (raised whenever the mapping code changes), and the licence `attribution`, `datasetLicenseUrl` and
`sources` from the dataset file. Both files are packed into the Android app and the iOS app bundle.

#### Scenario: Built database matches the dataset
- **WHEN** the bundled database is built from dataset v3
- **THEN** it holds 7,092 verbs and its metadata `lastUpdatedAt` is `2026-10-08T09:26:22Z`

#### Scenario: Every API field can be rebuilt
- **WHEN** a verb's data is read back from the bundled database
- **THEN** every field of its *Get verb details* (`lang=en`) record in the dataset file can be rebuilt from it,
  including each form's parts, agreement variants, `spokenText`, the translation, definition and synonyms

### Requirement: Installing the bundled database
At start-up, before the verb database is opened, the app SHALL install the bundled database in place of the local one
when any of these holds: there is no local verb database; the bundled `lastUpdatedAt` is later than the saved
checkpoint; the bundled schema version differs from the local one; the bundled `bundleBuild` is higher than the local
one. Otherwise the local database is kept. The same rule SHALL run on Android and iOS. The copy SHALL go to a temporary
file that is then renamed, so an interrupted copy never leaves a broken database. After an install, the saved
checkpoint is the bundled `lastUpdatedAt`. The user database is never touched. On iOS the verb database SHALL live in
Application Support and be excluded from iCloud backup.

#### Scenario: Fresh install offline
- **WHEN** the app is opened for the first time with no internet connection
- **THEN** the bundled database is installed and the full verb list is available

#### Scenario: Killed during the first copy
- **WHEN** the app is killed while the bundled database is being copied, and opened again
- **THEN** the copy is redone and the app works

#### Scenario: Local data newer than the bundle
- **WHEN** the bundled `lastUpdatedAt` is not later than the saved checkpoint, and the schema version and `bundleBuild`
  are unchanged
- **THEN** the local verb database is kept

#### Scenario: Schema changed
- **WHEN** the bundled schema version differs from the local one
- **THEN** the bundled database replaces the local one, and favourites are unchanged

#### Scenario: Mapping fix only
- **WHEN** the bundled data and schema are the same but `bundleBuild` is higher
- **THEN** the bundled database replaces the local one

#### Scenario: iOS backup
- **WHEN** the verb database is installed on iOS
- **THEN** it is in Application Support and marked as excluded from iCloud backup

### Requirement: Normalised search and sort keys
Next to the original text, the local database SHALL store normalised keys: lowercase, `œ`→`oe`, `æ`→`ae`, accents
removed, typographic apostrophe `’`→`'`. The sort key SHALL also drop a leading `se `, `s'` or `s’`. The same
normalisation SHALL apply to the search text typed by the user. Normalised text SHALL never appear on screen; verbs and
forms are always shown in their original spelling.

#### Scenario: Accents and case
- **WHEN** "Être" is normalised
- **THEN** the key is "etre"

#### Scenario: Reflexive sort key
- **WHEN** the list is sorted A → Z
- **THEN** s'appeler sorts next to appeler

#### Scenario: Typographic apostrophe
- **WHEN** the user searches "s’appeler" typed with `’`
- **THEN** s'appeler is found

### Requirement: Local verb search
Search SHALL run on the local database and behave like the API's `q`. It is a prefix match on the infinitive or on the
default spelling (index 0) of any simple form (simple tenses, Infinitif présent, participles), ignoring case and
accents. Pronouns, auxiliaries, semi-auxiliaries and agreement variants SHALL NOT be matched. A blank query returns the
full list. Each verb appears at most once in the results.

#### Scenario: Conjugated forms count
- **WHEN** the user searches "su"
- **THEN** the results include être, savoir and suivre

#### Scenario: Accent-insensitive
- **WHEN** the user searches "ete"
- **THEN** être is found

#### Scenario: Semi-auxiliaries are not matched
- **WHEN** the user searches "va"
- **THEN** parler is not found (futur proche `vais` is not matched)

#### Scenario: Agreement variants are not matched
- **WHEN** the user searches "parlée"
- **THEN** parler is not found

#### Scenario: Prefix only
- **WHEN** the user searches "arl"
- **THEN** parler is not found

#### Scenario: Blank query
- **WHEN** the search text is empty or only spaces
- **THEN** every verb is returned

### Requirement: Matched form
When a verb matches through a conjugated form and not through its infinitive, the result SHALL carry that form in its
original spelling. When several forms match, a form equal to the search comes first; otherwise the first in display
order (mood, tense, person, as in the API). When the infinitive itself matches, the result carries no matched form.

#### Scenario: Form shown for être
- **WHEN** the user searches "su"
- **THEN** être carries "suis", savoir carries "su", and suivre carries no matched form

#### Scenario: Original spelling
- **WHEN** the user searches "ete"
- **THEN** être carries "été", not "ete"

### Requirement: Filters
The list SHALL be filterable by group (1st, 2nd, 3rd), auxiliary (avoir, être) and reflexive (yes, no). The filters are
multi-select. Values in one category combine with OR; categories combine with AND. A verb with both auxiliaries
(`BOTH`) SHALL match avoir and être. Filters combine with the search.

#### Scenario: Two groups
- **WHEN** the 1st group and 2nd group are selected
- **THEN** the list holds verbs of either group and no 3rd-group verb

#### Scenario: Both auxiliaries
- **WHEN** the auxiliary filter is être
- **THEN** passer (both auxiliaries) is in the list

#### Scenario: Categories combine
- **WHEN** être and non-reflexive are selected
- **THEN** every verb in the list takes être (or both) and is non-reflexive

### Requirement: Group by and sort
The list SHALL support grouping by none, group, auxiliary or reflexive, with sections in this order: 1st → 2nd → 3rd
group; avoir → être → avoir or être; non-reflexive → reflexive. It SHALL support sorting A → Z (default), Z → A and most
frequent first. The sort applies inside each section. Alphabetical order follows the normalised sort key. Ties are
broken by the normalised infinitive A → Z, then by slug.

#### Scenario: Grouped by group, most frequent
- **WHEN** the list is grouped by group and sorted by most frequent
- **THEN** the 1st group section comes first and starts with its most common verb

#### Scenario: Z to A
- **WHEN** the sort is Z → A
- **THEN** the list starts with the verb whose sort key is last in A → Z order

#### Scenario: Ties
- **WHEN** two verbs have the same frequency
- **THEN** they are ordered by normalised infinitive A → Z, then by slug

### Requirement: Paged list
The list SHALL be loaded page by page so that all 7,092 verbs scroll smoothly. Each item is identified by its slug. The
first page SHALL be visible in under 1 s after start-up on a mid-range device, except on a launch that installs the
bundled database. Search results SHALL update within 100 ms after the 300 ms debounce.

#### Scenario: Scrolling to the end
- **WHEN** the user scrolls from the first to the last verb
- **THEN** every verb is shown once, in order, and scrolling stays fluid

#### Scenario: Start-up time
- **WHEN** the app is opened with the verb database already installed
- **THEN** the first verbs are visible in under 1 s
