# design-system-foundation Specification

## Purpose

Defines the VerbeClair visual foundation: the light and dark colours, typography, spacing, shapes, elevation, icons and
flag background, all taken from the Figma Foundations and Components pages. Every component and screen draws from
these, so the app looks the same on Android and iOS and in every supported configuration.

## Requirements

### Requirement: Theme follows the system appearance
The app SHALL show the light theme when the device is in light mode and the dark theme when it is in dark mode. It
SHALL switch at once when the system setting changes, without restarting and without losing the current screen
(Notion N-5).

#### Scenario: Device in light mode
- **WHEN** the app is opened on a device set to light mode
- **THEN** every themed surface and text uses the Light values from the Figma Foundations, Colour — Light (8:4)

#### Scenario: Device in dark mode
- **WHEN** the app is opened on a device set to dark mode
- **THEN** every themed surface and text uses the Dark values from the Figma Foundations, Colour — Dark (8:124)

#### Scenario: Appearance changed while the app is open
- **WHEN** the user switches the system between light and dark mode while the app is open
- **THEN** the app redraws in the new theme and stays on the same screen

### Requirement: Colour roles come from Figma
The app SHALL use only the colour roles defined in the Figma Foundations: the brand, verb-ending, surface, content,
line and feedback groups, each with its own Light and Dark value. No colour SHALL be hard-coded outside the theme.
A colour role that Material 3 needs but Figma does not define SHALL NOT get an invented value; it MUST be added in
Figma first.

#### Scenario: Primary colour in each mode
- **WHEN** an element uses the primary colour
- **THEN** it is #2E4BCB in light mode and #B8C4FF in dark mode

#### Scenario: Background in each mode
- **WHEN** a screen draws its background colour
- **THEN** it is #FBFAFF in light mode and #12131A in dark mode

#### Scenario: Role missing from Figma
- **WHEN** a Material 3 colour role has no matching Figma variable
- **THEN** the theme is not considered complete until the variable exists in Figma and its Light and Dark values are
  used

### Requirement: Verb-ending and success colours
The theme SHALL provide three colours beyond Material 3:
- `ending`: #C0391F light, #FFB4A1 dark
- `ending-container`: #FFDAD2 light, #5F1409 dark
- `success`: #2E7D4F light, #8FD8A8 dark

Highlighted verb endings (Notion D-8) and filled favourite stars (Notion L-13) use `ending`. Correct test answers use
`success`.

#### Scenario: Ending colour in light mode
- **WHEN** an element uses the ending colour in light mode
- **THEN** it is drawn in #C0391F

#### Scenario: Ending colour in dark mode
- **WHEN** an element uses the ending colour in dark mode
- **THEN** it is drawn in #FFB4A1

#### Scenario: Ending is never colour-only
- **WHEN** a verb ending is shown with the ending text style
- **THEN** it is both bold and in the ending colour, so colour is not the only cue (Notion N-4)

### Requirement: Typography
The theme SHALL provide the 14 Figma text styles with their font, weight, size, line height and letter spacing:
- Fraunces SemiBold, for verbs and moods:
  - Display/Verb Large 34/40, letter spacing -0.5
  - Headline/Small 24/32
  - Title/Verb 20/26
- DM Sans, for the UI:
  - Title/Large SemiBold 22/28
  - Title/Medium SemiBold 16/24, letter spacing 0.1
  - Title/Small SemiBold 14/20, letter spacing 0.1
  - Label/Overline Bold 12/16, letter spacing 1.2
  - Label/Large Medium 14/20, letter spacing 0.1
  - Label/Medium Medium 12/16, letter spacing 0.4
  - Body/Large Regular 16/24, letter spacing 0.2
  - Body/Medium Regular 14/20, letter spacing 0.2
  - Body/Small Regular 12/16, letter spacing 0.3
  - Conjugation/Form Regular 16/24
  - Conjugation/Ending Bold 16/24

The fonts SHALL ship inside the app and work offline. Text SHALL scale with the system font size (Notion N-4). French
characters (accents, `œ`, `ç`, apostrophes) SHALL render in the bundled fonts (Notion N-3).

#### Scenario: Verb title font
- **WHEN** an infinitive is shown with the Title/Verb style
- **THEN** it renders in Fraunces SemiBold, 20 sp, with a 26 sp line height

#### Scenario: Offline first launch
- **WHEN** the app is opened for the first time with no network
- **THEN** text renders in Fraunces and DM Sans, not in a system fallback font

#### Scenario: Large system font size
- **WHEN** the user sets a larger system font size
- **THEN** every text style grows proportionally

#### Scenario: French characters
- **WHEN** the text "s'appeler, être, cœur, ça" is shown in any style
- **THEN** every character renders in the style's own font

### Requirement: Spacing, shape and elevation tokens
The theme SHALL provide these tokens, and components SHALL use them instead of literal values:
- spacing: 2, 4, 8, 12, 16, 20, 24, 32 and 48 dp (`space/2`…`space/48`)
- corner radii: xs 4, sm 8, md 12, lg 16 and xl 28 dp, plus full (fully rounded)
- elevation level 2: two shadows, 0/2 blur 6 at 12% and 0/8 blur 24 at 10% of #0D0F33, used by bottom sheets and
  snackbars

#### Scenario: Large radius
- **WHEN** a surface uses the large corner radius
- **THEN** its corners are rounded by 16 dp

#### Scenario: Full radius
- **WHEN** a surface uses the full corner radius
- **THEN** its short sides are fully rounded, whatever its height

### Requirement: Icon set
The app SHALL provide the 39 icons of the Figma Components Icons frame (4:2) as vector images made from the Figma SVG
paths: 24×24 with a 2 px stroke. They SHALL be tinted with theme colours, so the same icon works in light and dark
mode. A Material icon SHALL be used only for an icon that is not in Figma.

#### Scenario: Icon matches Figma
- **WHEN** an icon such as `icon/star-filled` is shown at 24 dp
- **THEN** its shape matches the Figma symbol (48:62)

#### Scenario: Icon in dark mode
- **WHEN** an icon is tinted with the on-surface colour
- **THEN** it is #1A1B23 in light mode and #E3E1EC in dark mode

### Requirement: Flag background
The app SHALL provide the faded French flag background from Figma `Background/Flag stripe` (95:217) at 12% opacity in
both themes (Notion N-14):
- **Screens:** the White (cream) stripe fills the screen behind the content, scaled to cover the window, centred, and
  fixed while the content scrolls.
- **Bars and the rail:** the Blue stripe, rotated 90° and scaled to cover, fills bottom bars on top of the background
  colour, so the bars are opaque. It also fills the navigation-rail area.
- The Red stripe is not used.

The flag is decorative: screen readers SHALL ignore it, and text over it SHALL keep WCAG AA contrast (Notion N-4). It
SHALL ship inside the app and work offline.

#### Scenario: Screen background, phone portrait, light
- **WHEN** a screen is shown on a phone in portrait in light mode
- **THEN** the White stripe covers the window at 12% opacity above the background colour and below the content

#### Scenario: Screen background, phone landscape, dark
- **WHEN** a screen is shown on a phone in landscape in dark mode
- **THEN** the White stripe covers the whole wider window, centred, at 12% opacity, with no gaps or stretching

#### Scenario: Screen background, tablet, light and dark
- **WHEN** a screen is shown on a tablet in portrait or landscape, in light or dark mode
- **THEN** the White stripe covers the whole window, centred, at 12% opacity

#### Scenario: Content scrolls
- **WHEN** the content of a screen scrolls
- **THEN** the flag background stays fixed

#### Scenario: Bar background
- **WHEN** a bottom bar is drawn
- **THEN** it shows the background colour plus the Blue stripe rotated 90° at 12% opacity, and content scrolling under
  it does not show through

#### Scenario: Screen reader
- **WHEN** TalkBack or VoiceOver moves through a screen
- **THEN** the flag background is never announced

### Requirement: Theme applies in every supported configuration
The same theme SHALL apply on phone portrait, phone landscape and tablet, in light and in dark mode, on Android and on
iOS.

#### Scenario: Phone portrait, light and dark
- **WHEN** the app root is shown on a phone in portrait in light mode and then in dark mode
- **THEN** it uses the VerbeClair colours, fonts and flag background for that mode

#### Scenario: Phone landscape, light and dark
- **WHEN** the app root is shown on a phone in landscape in light mode and then in dark mode
- **THEN** it uses the VerbeClair colours, fonts and flag background for that mode

#### Scenario: Tablet, light and dark
- **WHEN** the app root is shown on a tablet in light mode and then in dark mode
- **THEN** it uses the VerbeClair colours, fonts and flag background for that mode
