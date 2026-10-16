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
