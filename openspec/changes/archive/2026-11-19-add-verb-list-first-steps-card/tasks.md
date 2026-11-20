## 1. Data and domain

- [x] 1.1 Add `FirstStepsPreferences` (`closed: Flow<Boolean>`, `close()`) to `core/domain`, implement it on its own DataStore file in `core/data` that backup leaves out (not in `ListPreferences`), register it in Koin, and verify a data test shows closed survives a new DataStore instance and defaults to false
- [x] 1.2 Add `TestScoreRepository.observeHasAnyAttempt()` with an `EXISTS` query on the user database, and verify a Room test: false when empty, true after one test attempt, true after one review attempt, true for attempts inserted by an import, and it reacts to the first insert
- [x] 1.3 Make sure the closed flag is not stored in the user database and is not read by any export path, and verify with a test (or a Konsist rule if one fits) that `FirstStepsPreferences` is not referenced from the user-data module
- [x] 1.4 Provide the top 10 verbs (most frequent, non-reflexive, 10 items) from `VerbCatalog` and verify a test against the seeded test database returns the expected order and no reflexive verb

## 2. Design system

- [x] 2.1 Read the card again from Figma (`get_design_context`, `get_variable_defs` for `151:248`) and create `FirstStepsCard` in `core/designsystem/component` from the existing button and `icon/close`, with `actionsAlignment` Start or Center and a 44 dp close target, and verify a component test for the layout, the three callbacks and the close label
- [x] 2.2 Add a `@Preview` per state of the card in `core/designsystem`, each preceded by its `// Figma:` URL: portrait light (`152-15348`) and dark (`154-16242`), landscape light (`195-17561`) and dark (`195-17859`), and the component (`151-248`); verify the previews compile
- [x] 2.3 Add the card to the `design-system-components` list of components in the code docs or catalogue where the others are listed, and verify nothing else lists components without it

## 3. Verb list state and logic

- [x] 3.1 Add `showFirstSteps` to `VerbListState` and the three actions (`OnFirstStepsClose`, `OnFirstStepsTestEtre`, `OnFirstStepsMostCommon`) in `VerbListAction`; compute the flag in `VerbListViewModel` from tab, search, filters, empty database, closed flag and any-attempt flag, false until both flows have emitted; verify ViewModel tests for each scenario in the verb-list-screen spec (fresh install, search, filter, Favourites, closed, first attempt, imported attempts, grouping and sort hide it until their chip is removed)
- [x] 3.2 Handle the actions: Close calls `close()`, Test être raises the test-setup navigation event with slug `etre`, Most common first sets the sort to Most frequent; verify ViewModel tests, including that the card hides after the sort, the chip appears and removing it brings the card back
- [x] 3.3 Add the strings (card title, body, two buttons, close label, panel title) to the verb-list string resources in English, French (formal) and Romanian (informal) as in the proposal table, and verify a test that every key exists in all three languages

## 4. Verb list screen

- [x] 4.1 Draw the card as a full-span grid item before the paged rows, only when `showFirstSteps` and the first page is loaded and not empty (not in skeleton, empty-result, no-data or no-favourites), centred actions on phone landscape; verify UI tests for phone portrait, phone landscape and tablet, scrolling away, grouped or sorted list hiding it, and each hidden state
- [x] 4.2 Build the top 10 panel (title plus plain infinitive rows, no score, rank or chevron) and switch the tablet right pane: panel when the card is shown and no verb is selected, the Neutral empty state otherwise; verify UI tests: panel shown, tap a panel verb selects it and calls the verb-details callback, selection cleared brings the panel back, closing the card shows "Choose a verb to see its conjugation"
- [x] 4.3 Add the verb-list previews, each preceded by its `// Figma:` URL: L-10 light (`152-15348`) and dark (`154-16242`), PL-07 light (`195-17561`) and dark (`195-17859`), TP-07 light (`195-18161`) and dark (`195-18557`), TL-07 light (`195-18953`) and dark (`195-19327`); verify the previews compile
- [x] 4.4 Check French and Romanian: verify UI tests for the card strings and the panel title in both languages, and a large-font test where the card text wraps without covering the close button

## 5. Done gate

- [x] 5.1 Compare every changed screen with its Figma frame (L-10, PL-07, TP-07, TL-07, light and dark), run all tests, and run `DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew build` from the repository root so every affected module (`core/domain`, `core/data`, `core/database`, `core/designsystem`, `feature/verblist`, `shared`, `androidApp`) passes
