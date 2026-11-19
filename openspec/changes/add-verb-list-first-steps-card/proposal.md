## Why

A new user opens the verb list and sees 7,000 verbs with no hint where to start. Requirement **OB-1** (Notion, section 13A, added 2026-10-22) adds a "First steps" card that points beginners at the most common verbs and at a first test. The verb list does not have it yet.

## What Changes

- Add a **First steps card** as the first item of the All tab list on Screen 1. It scrolls with the list.
  - Shown only with an empty search, no active choice (no filter, grouping or sort), the All tab, no saved attempt, and the card not closed. It is also hidden in the no-data, empty-result and first-page-skeleton states. Removing the last chip shows it again.
  - Content: "New to French verbs? Start with the most common ones." (fr and ro strings are in the table below), a **Test être** button (opens Screen 4 for être), a **Most common first** button (sets the sort to most frequent, so the sort chip appears and the card is hidden until the chip is removed) and a close (X) button with the accessible label "Close tip".
  - Closing it hides it for good. The closed flag lives in the app-settings DataStore, is never exported, and resets on reinstall.
  - Any saved test or review attempt hides it for good, including attempts that arrive through an imported file (S-13).
- **Phone landscape:** the card spans both columns of the list grid. Title and body are left-aligned, the buttons are centred.
- **Phone portrait and tablet:** the card keeps left-aligned buttons.
- **Tablet (list-detail):** the card is the first item of the 360 dp left list pane. While it is shown and no verb is selected, the right pane shows a **top-10 panel** titled "Does any of these catch your eye?". It is a plain list of the 10 most frequent non-reflexive verbs (`sort=frequency&reflexive=false`, 10 items): être, avoir, aller, faire, dire, pouvoir, vouloir, savoir, voir, devoir. No scores, ranks or chevrons.
  - Tapping a verb in the panel or in the list selects it and opens its details in the right pane.
  - If the selection is cleared while the card is still shown, the panel comes back. Once the card is gone, the pane shows "Choose a verb to see its conjugation" as before.
- New strings (French formal, Romanian informal):

  | String | English | Français | Română |
  |---|---|---|---|
  | Card title | New to French verbs? | Vous débutez avec les verbes français ? | Abia ai început să înveți verbele în franceză? |
  | Card body | Start with the most common ones. | Commencez par les plus courants. | Începe cu cele mai frecvente. |
  | Button 1 | Test être | Tester être | Testează être |
  | Button 2 | Most common first | Les plus courants d'abord | Cele mai frecvente primele |
  | Close label | Close tip | Fermer le conseil | Închide sfatul |
  | Panel title | Does any of these catch your eye? | L'un de ces verbes vous attire-t-il ? | Îți atrage atenția vreunul dintre aceste verbe? |

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `verb-list-screen`: new requirement for the First steps card (visibility rules, buttons, close, layouts) and for the tablet top-10 panel in the detail pane.
- `design-system-components`: new requirement for the **First steps card** component (Figma component `151:248`), written so other screens can reuse it later. It is a generic tip card with title, body, two buttons and a close button; the first-steps wording stays in the verb list.
- `user-data`: the app-settings store keeps the card's closed flag (not exported), and the test and review data can report whether any attempt exists, including imported ones.

## Impact

- **Code:** `feature/verblist` (`VerbListScreen`, `VerbListState`, `VerbListAction`, a new card composable and the top-10 panel; the card is a grid item with full span before the paged rows, outside paging). `core/domain` and `core/data` (a small settings interface and DataStore entry for the closed flag; an "any attempt exists" flow on the test-score repository). String resources in en, fr and ro.
- **Dependencies:** none new. No API or database schema change. The top-10 list comes from the existing sort and reflexive filter.
- **Tests:** unit tests for the visibility rule and the actions, UI tests for phone portrait, phone landscape and tablet.

## Sources

- **Notion requirement:** OB-1 in the [VerbeClair requirements](https://app.notion.com/p/VerbeClair-requirements-3f1d02620bc1804180a7f241233fdacb) page, section 13A. The N-15 note also covers the landscape and tablet behaviour.
- **Figma frames** (file `1TyF6efL7m5d6DSTktnnLy`, page Screens). "L-10" here is a Figma frame number, not the Notion requirement L-10 (No data, Figma frame L-06):
  - Phone portrait, L-10 Light: [`152:15348`](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=152-15348)
  - Phone portrait, L-10 Dark: [`154:16242`](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=154-16242)
  - Phone landscape, PL-07 Light: [`195:17561`](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=195-17561)
  - Phone landscape, PL-07 Dark: [`195:17859`](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=195-17859)
  - Tablet portrait, TP-07 Light: [`195:18161`](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=195-18161)
  - Tablet portrait, TP-07 Dark: [`195:18557`](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=195-18557)
  - Tablet landscape, TL-07 Light: [`195:18953`](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=195-18953)
  - Tablet landscape, TL-07 Dark: [`195:19327`](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=195-19327)
  - Component "First steps card": [`151:248`](https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=151-248)

## Defaults and open questions

Decided with the product owner; recorded here so the specs can turn them into scenarios:

- Any active choice (filter, grouping or sort) hides the card; removing the last chip shows it again.
- The card is hidden in the no-data, empty-result and first-page-skeleton states.
- Any saved attempt counts, including attempts from an imported file. The closed flag is not exported and resets on reinstall.

Resolved:

- The card's fr and ro strings (table above) are accepted from this proposal and are not added to Notion.
- Notion OB-1 is not extended with the landscape alignment (text left, buttons centred). That is visual detail owned by Figma; Notion already says the card spans both columns, so the two sources do not disagree.
- The card is a design-system component (`design-system-components`), because it may be reused.

Still open:

- The card's fonts, colours and spacing have not been read from Figma metadata yet (`get_design_context`, `get_variable_defs`). Do that in `design.md`.
