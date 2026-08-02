## MODIFIED Requirements

### Requirement: Navigation Menu

The generator SHALL produce a navigation menu from `Application.navigationController`.

The menu layout SHALL be determined by `Application.defaultMenuLayout`: `VERTICAL` produces a sidebar drawer with collapsible sections; `HORIZONTAL` produces a top bar with dropdown menus.

Navigation items SHALL form a recursive tree. Group items (with child `items`) SHALL render as expandable sections. Leaf items (with `target`) SHALL navigate to the referenced `PageDefinition`.

Items with an `actionDefinition` SHALL trigger the associated action (e.g., static operations) from the menu.

Items with `hiddenBy` SHALL be conditionally visible based on runtime state.

Each navigation item SHALL display its `label` and `icon`.

The vertical sidebar drawer SHALL render as a **permanent** drawer on desktop breakpoints and as a **temporary** (overlay) drawer on mobile breakpoints. Switching between these breakpoints SHALL NOT leave a click-blocking overlay over the page content: after a desktop→mobile transition the temporary drawer SHALL start closed and SHALL NOT capture pointer events over page content.

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

- **WHEN** the viewport shrinks from a desktop breakpoint (`md`/`lg`/`xl`) into a mobile breakpoint (`sm`/`xs`)
- **THEN** the generated temporary drawer starts closed for that transition
- **AND** its underlying MUI Modal root is not left visible with `pointer-events: auto` over the page
- **AND** page buttons, links, and navigation items remain clickable without a reload

**Key Helpers**: `UIMenuHelper.applicationHasMenuOperations()`, `UIMenuHelper.getMenuOperationOwnerTypes()`, `UiGeneralHelper.isNavItemAGroup()`

**Templates**: `src/layout/Drawer/**/*.hbs`, `src/layout/Header/**/*.hbs`
