## Why

Screen 1 (Verb list) is the first real screen of VerbeClair, and every part of it is drawn from component sets on the
Figma Components page: the search bar, the list-controls icon button with its badge, the All / Favourites tabs, the
verb cell, section headers, skeletons, chips, radio rows, buttons, empty states, the bottom bar and the navigation
rail. None of these exist in code yet: `:core:designsystem` holds only the foundation (theme, icons, flag background).
They have to exist as shared design-system components first, so the verb list and the later screens (details,
settings, tests) reuse one implementation instead of re-drawing them.

## What Changes

- Add a `component` package to `:core:designsystem` with Material 3–based components taken from the Figma Components
  page (3:64), themed only through `VerbeClairTheme` tokens and working in light and dark mode:
  - `SearchBar`: Empty and Filled (with clear button) states
  - `VerbeClairIconButton`: Standard and Tonal styles, with an optional count badge (the active-filter count, L-6)
  - `VerbeClairButton`: Filled, Tonal, Outlined, Text and Disabled
  - `FilterChip`: Filter off and Filter on, clickable in both states (also used by the verb list's active choices
    row, where tapping a chip removes that choice, L-6)
  - `RadioRow`: selected and not selected
  - `ListTabs`: All / Favourites (n), with either tab selected
  - `VerbCell`: infinitive, meta line, optional matched form, optional last score, test button and favourite button;
    a selected state for the tablet list–detail layout (N-15)
  - `SectionHeader`, `VerbCellSkeleton`, `EmptyState` (Neutral and Error tones), `LinearProgress` and
    `BottomSheetHandle`
  - `VerbeClairBottomBar` and `VerbeClairNavigationRail`: Verbs / Tests / Settings with any one selected, on the blue
    flag stripe (N-14)
- Add the component strings (content descriptions and fixed labels) as Compose string resources in English, French and
  Romanian (N-6).
- One `@Preview` per Figma variant and state, light and dark, each preceded by its Figma URL.
- Not in this change: the verb list screen itself, its ViewModel, navigation, data layer and the active choices row
  layout. They come in the later changes `add-verb-data-layer` and `add-verb-list-screen`. Components used only by other
  screens (app bar, mood header, conjugation row, tense card, settings row, synonym chip, dialog, answer row…) are added
  with those screens.

## Requirements and design sources

- Notion: [VerbeClair requirements](https://app.notion.com/p/VerbeClair-requirements-3f1d02620bc1804180a7f241233fdacb):
  - L-2 (top bar), L-3 / L-4 (search, matched form), L-5 / L-6 (list controls, badge, active choices row), L-8 (cell),
    L-9 / L-10 (empty states), L-12 (tabs), L-13 (favourite button), L-14 (test button and score)
  - NV-1, NV-3, NV-4 (bottom bar and navigation rail), N-2 (skeleton), N-4 (accessibility), N-5 (themes),
    N-6 (app languages), N-14 (flag background), N-15 (selected row in list–detail)
- Figma, [Components (3:64)](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=3-64):
  - [Search bar (5:56)](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-56)
  - [Icon button (5:39)](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-39)
  - [Button (5:89)](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-89)
  - [Chip (5:101)](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-101)
  - [Radio row (5:113)](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-113)
  - [List tabs (49:145)](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=49-145)
  - [Verb cell (7:41)](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=7-41)
  - [Section header (7:45)](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=7-45)
  - [Verb cell skeleton (7:49)](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=7-49)
  - [Empty state (7:135)](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=7-135)
  - [Progress/Linear (7:151)](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=7-151)
  - [Bottom sheet/Handle (7:154)](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=7-154)
  - [Bottom nav (49:128)](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=49-128)
  - [Navigation rail (86:9531)](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=86-9531)
- Figma, [Screens (3:65)](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=3-65),
  where these components are used: L-01…L-09 (light and dark), PL-01 (landscape), TP-01 and TL-01 (tablet). No screen
  is built in this change.

## Capabilities

### New Capabilities
- `design-system-components`: the shared, themed UI components from the Figma Components page that the verb list (and
  later screens) are built from, with their states, accessibility labels and previews.

### Modified Capabilities
- None. `design-system-foundation` is used as is.

## Impact

- **Modules:** `:core:designsystem` gains the `component` package and string resources. `:shared`, `:androidApp` and the
  iOS `Shared` framework are affected only through the build (nothing in them uses the components yet).
- **Dependencies:** none new. Material 3, Compose foundation and compose-resources are already in
  `gradle/libs.versions.toml`.
- **Notion:** L-6 now describes the active choices row (updated 2026-10-16).
