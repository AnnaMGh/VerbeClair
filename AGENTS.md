# AGENTS.md

**Before doing any work in this repository, read [`openspec/config.yaml`](openspec/config.yaml).**
It is the single, authoritative source for project context, sources of truth (Figma, Notion),
per-artifact rules and the definition of done. If anything here disagrees with it, `config.yaml` wins.

@openspec/config.yaml

## Non-negotiables (summary — details in `openspec/config.yaml`)

1. **Sources of truth** — Design: Figma. Requirements and API: Notion. Check both before any decision;
   if neither specifies it, ask the user instead of guessing.
2. **Figma MCP is mandatory** — if the Figma MCP tools cannot connect, stop and fail with an error.
   Never improvise a design.
3. **Implement from real Figma metadata** (`get_metadata`, `get_design_context`, `get_variable_defs`),
   never from screenshots of the mocks. Screenshots are allowed only to verify a finished
   implementation against Figma.
4. **Reuse the design system** — tokens come from Figma Foundation/Components; check for an existing
   component before creating one. Icons come from Figma as SVG (via `figma-to-android-vector`);
   fall back to Material 3 icons only if the icon is not in Figma.
5. **Previews** — every component and screen has one `@Preview` per state, all states covered, each
   preceded by a `// Figma: <url-of-that-state>` comment.
6. **Targets** — phone portrait, phone landscape, tablet; light and dark mode.
7. **Definition of done** — all tests pass and `./gradlew build` passes for every affected module
   (including modules that depend on the edited one), not just the module edited.
8. **Branching** — `main` ← `dev` ← work branch. Every feature or change gets a new branch created
   from `dev`; work branches merge into `dev`, and only `dev` merges into `main`. Never commit
   directly to `main` or `dev`. Merges into `dev` and `main` happen only through a GitHub pull
   request approved by the user — never merge locally or push to `dev`/`main` directly.
