## Why

VerbeClair has no design system yet: the app still uses the KMP template's bare `MaterialTheme`. Every screen in the
[Figma Screens](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=3-65)
page is built from the tokens in Foundations and the icons and flag background in Components, so these have to exist in
code, themed for light and dark mode, before any component or screen can be implemented.

## What Changes

- Add a new Kotlin Multiplatform module `:core:designsystem`, which `:shared` depends on.
- Add the VerbeClair app theme, built from the Figma Foundations and switching between light and dark with the system
  setting:
  - the Figma colour roles for light and dark, mapped onto Material 3 roles
  - the brand-specific `ending`, `ending-container` and `success` colours as extended theme colours
  - the 14 text styles: Fraunces for verbs and moods, DM Sans for the UI, including `Conjugation/Form` and
    `Conjugation/Ending`
  - spacing (`space/2`…`space/48`), radius (`radius/xs`…`radius/full`) and `Elevation/Level 2` tokens
  - the flag-background opacity token (12%)
- Bundle the Fraunces and DM Sans fonts in the app, so text renders correctly offline on Android and iOS.
- Add the icon set: the 39 line icons in the Figma Components page, converted from Figma SVG to vector images.
- Add the flag background:
  - the White stripe behind screen content
  - the Blue stripe behind bars and the navigation rail
  - decorative only, hidden from screen readers
- Replace the template `MaterialTheme` in `App` with the VerbeClair theme.
- Not in this change: the ~30 Figma component sets (buttons, chips, verb cell, answer row, bottom nav, navigation
  rail…) and every screen. They are separate, later changes built on this foundation.

## Requirements and design sources

- Notion: [VerbeClair requirements](https://app.notion.com/p/VerbeClair-requirements-3f1d02620bc1804180a7f241233fdacb):
  - N-4: accessibility (colour never the only cue; endings in bold; WCAG AA contrast)
  - N-5: light and dark themes following the system
  - N-14: flag background
  - N-15: adaptive layouts
  - D-8 and L-13: the `color/ending` highlight colour
  - NV-4: the navigation rail background
- Figma:
  - [Foundations (3:63)](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=3-63):
    - [Colour Light (8:4)](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=8-4)
    - [Colour Dark (8:124)](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=8-124)
    - [Typography (8:244)](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=8-244)
    - [Spacing, radius, elevation (8:288)](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=8-288)
  - [Components (3:64)](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=3-64):
    - [Icons (4:2)](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=4-2)
    - [Background/Flag stripe (95:217)](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=95-217)
  - [Screens (3:65)](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=3-65):
    the only affected screen is the app root, which now uses the theme. No screen is built in this change.

## Capabilities

### New Capabilities
- `design-system-foundation`: the VerbeClair theme (light and dark colours, typography, spacing, radius, elevation),
  the icon set and the flag background that every component and screen is built on.

### Modified Capabilities
- None.

## Impact

- **Modules:**
  - new `:core:designsystem`, registered in `settings.gradle.kts`
  - `:shared` gains a dependency on it
  - `:androidApp` and the iOS `Shared` framework are affected through `:shared`
- **Dependencies:**
  - no new third-party libraries; uses the Compose Multiplatform, Material 3 and compose-resources already in
    `gradle/libs.versions.toml`
  - font files (Fraunces and DM Sans, SIL Open Font License) and the flag image are added as Compose resources
- **Code:** `shared/src/commonMain/kotlin/com/vamvisions/verbeclair/App.kt` switches to the VerbeClair theme.
- **Blocked on design:** Material 3 colour roles that Figma does not define yet must be added in Figma before the
  colour scheme is complete (see design.md, Open Questions).
