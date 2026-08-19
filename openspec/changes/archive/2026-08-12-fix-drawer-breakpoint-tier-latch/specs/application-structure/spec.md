## MODIFIED Requirements

### Requirement: Navigation Menu

The generator SHALL produce a navigation menu from `Application.navigationController`.

The menu layout SHALL be determined by `Application.defaultMenuLayout`: `VERTICAL` produces a sidebar drawer with collapsible sections; `HORIZONTAL` produces a top bar with dropdown menus.

Navigation items SHALL form a recursive tree. Group items (with child `items`) SHALL render as expandable sections. Leaf items (with `target`) SHALL navigate to the referenced `PageDefinition`.

Items with an `actionDefinition` SHALL trigger the associated action (e.g., static operations) from the menu.

Items with `hiddenBy` SHALL be conditionally visible based on runtime state.

Each navigation item SHALL display its `label` and `icon`.

The vertical sidebar drawer SHALL render as a **permanent** drawer at `sm` and wider, and as a **temporary** (overlay) drawer only at `xs` (below the `sm` breakpoint) — so `sm` is on the permanent-drawer side. Entering `xs` from any wider breakpoint, `sm` included, SHALL NOT leave a click-blocking overlay over the page content: on that transition the temporary drawer SHALL start closed and SHALL NOT capture pointer events over page content.

The drawer's collapsed/expanded state SHALL be a function of the **breakpoint tier**, not of the individual breakpoint transition. Breakpoints SHALL form three tiers: `xs`/`sm` (**compact**), `md` (**medium**), and `lg`/`xl` (**wide**).

Every breakpoint change into or within the compact tier SHALL restore the collapsed baseline: no collapse toggle is rendered at `xs` or `sm`, so an expanded permanent drawer there would be unrecoverable. At `xs`, "collapsed" means the temporary overlay drawer is closed. This is the responsive baseline rather than an absolute runtime state — at `xs` a user action MAY open the temporary overlay drawer, and that choice SHALL stand until the next breakpoint change.

The medium tier SHALL default to collapsed and the wide tier to expanded. Within the medium and wide tiers the user's toggle choice SHALL be preserved, and crossing a tier boundary SHALL re-apply the entered tier's default. `xs` → `sm` is *within* the compact tier rather than a boundary crossing, and is therefore governed by the compact-baseline rule above.

Until the media queries resolve, the breakpoint is unresolved. While unresolved the generated drawer SHALL NOT change its state and SHALL NOT discard the last resolved breakpoint, so that the next resolved breakpoint still sees its own tier transition. On the first resolved breakpoint the application's configured `miniDrawer` default SHALL be honoured for the medium and wide tiers.

#### Scenario: Vertical sidebar navigation

- **WHEN** `Application.defaultMenuLayout = VERTICAL`
- **AND** `navigationController.items` contains leaf items and group items
- **THEN** the generator produces `src/layout/Drawer/` components with a collapsible sidebar menu
- **AND** leaf items link to their target pages
- **AND** group items expand to reveal sub-items

#### Scenario: Horizontal top bar navigation

- **WHEN** `Application.defaultMenuLayout = HORIZONTAL`
- **THEN** the generator produces `src/layout/Header/` components with a top bar menu

#### Scenario: Menu with operations

- **WHEN** navigation items have `actionDefinition` set (e.g., static operations)
- **THEN** the generator produces `menuTypes.ts` and `menuCustomization.ts`
- **AND** clicking the menu item triggers the operation

#### Scenario: Conditional menu item visibility

- **WHEN** a navigation item has `hiddenBy` set
- **THEN** the menu item is rendered conditionally based on the runtime value

#### Scenario: Responsive drawer breakpoint switch

- **WHEN** the viewport shrinks from any wider breakpoint (`sm`/`md`/`lg`/`xl`) into `xs`, where the temporary drawer branch is rendered
- **THEN** the generated temporary drawer starts closed for that transition
- **AND** its underlying MUI Modal root is not left visible with `pointer-events: auto` over the page
- **AND** page buttons, links, and navigation items remain clickable without a reload

#### Scenario: Compact baseline restored when leaving the temporary overlay

- **WHEN** the user opens the temporary overlay drawer at `xs`
- **AND** the viewport then widens into `sm`, which stays within the compact tier
- **THEN** the generated drawer is collapsed to the mini rail at `sm`
- **AND** it is not left expanded at a width where no collapse toggle is rendered
- **AND** narrowing back to `xs` does not reopen the overlay drawer without a user action

#### Scenario: Drawer state preserved within the wide tier

- **WHEN** the user collapses the drawer at `lg`
- **AND** the viewport widens into `xl`
- **THEN** the drawer stays collapsed because no tier boundary was crossed

#### Scenario: Configured drawer default on first render

- **WHEN** the application is loaded at `md`, `lg`, or `xl`
- **THEN** the generated drawer honours the consuming application's configured `miniDrawer` default
- **AND** the resize effect does not override it until a tier boundary is crossed

#### Scenario: Unresolved breakpoint leaves the drawer untouched

- **WHEN** the media queries have not yet reported, so the breakpoint is unresolved
- **THEN** the generated drawer does not change its collapsed/expanded state
- **AND** the last resolved breakpoint is retained, so the next resolved breakpoint still applies its own tier transition

**Key Helpers**: `UIMenuHelper.applicationHasMenuOperations()`, `UIMenuHelper.getMenuOperationOwnerTypes()`, `UiGeneralHelper.isNavItemAGroup()`

**Templates**: `src/layout/Drawer/**/*.hbs`, `src/layout/Header/**/*.hbs`
