## ADDED Requirements

### Requirement: Snackbar
The design system SHALL provide a Snackbar built from Figma `Snackbar` (node 7:144): a 20 dp icon and a message in
Body/Medium on `inverse-surface`, at least 48 dp high, with `radius/sm` corners and the Elevation/Level 2 shadow. Colours,
spacing and radius come from the theme tokens. The optional action of the Figma component is not built until a screen
needs one.

#### Scenario: Light and dark mode
- **WHEN** the snackbar is shown in light or in dark mode
- **THEN** it uses the `inverse-surface`, `inverse-on-surface` colours of that mode, as in Figma

#### Scenario: Previews
- **WHEN** the component previews are opened
- **THEN** the snackbar has a light and a dark preview, each preceded by the Figma URL of node 7:144
