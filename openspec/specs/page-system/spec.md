# Page System

Generates React page components and dialog components from UI `PageDefinition` model elements. Each page definition produces a routable page or a modal dialog, with its own context, actions, types, and customization hooks.

## Requirements

### Requirement: Table Page Generation

The generator SHALL produce a routable page component for each `PageDefinition` with `container.type = TABLE` and `openInDialog = false`.

The page SHALL be registered as a route in `src/routes.tsx`. Data SHALL be loaded via a service call on mount (refresh action). The table SHALL be the primary content with CRUD actions.

For each table page, the generator SHALL produce:
- `src/pages/{Actor}/{PagePath}/index.tsx` — page component with data loading and action handlers
- `src/pages/{Actor}/{PagePath}/context.tsx` — React context for page view model
- `src/pages/{Actor}/{PagePath}/types.ts` — TypeScript interfaces for actions and props
- `src/pages/{Actor}/{PagePath}/customization.ts` — Pandino hook interface key

#### Scenario: Table page with data

- **WHEN** a `PageDefinition` has `container.type = TABLE` and `openInDialog = false`
- **THEN** the page is registered as a route
- **AND** data loads on mount via the refresh action
- **AND** CRUD actions are available based on behaviours

**Key Helpers**: `UiPageHelper.getPagesForRouting()`, `UiPageHelper.getPageRoute()`, `UiPageHelper.pagePath()`, `UiPageHelper.isPageRefreshable()`

---

### Requirement: View Page Generation

The generator SHALL produce a routable page component for each `PageDefinition` with `container.type = VIEW` and `openInDialog = false`.

The page SHALL load a single entity by signed ID from URL parameters. The page SHALL support edit mode toggle (view → edit → save/cancel). Refresh, update, and delete actions SHALL be available based on the entity's behaviours.

Signed ID SHALL provide concurrency control — updates send the signed ID back to the server.

#### Scenario: View page with edit mode

- **WHEN** a `PageDefinition` has `container.type = VIEW`
- **AND** the entity has `UPDATE` behaviour
- **THEN** the page supports toggling between read-only and edit mode
- **AND** save and cancel buttons appear in edit mode

#### Scenario: View page with delete

- **WHEN** the entity has `DELETE` behaviour
- **THEN** a delete action is available on the page

**Key Helpers**: `UiPageHelper.pageHasSignedId()`, `UiPageHelper.isPageUpdateable()`, `UiPageHelper.isPageDeleteable()`

---

### Requirement: Form Page Generation

The generator SHALL produce a routable page component for each `PageDefinition` with `container.type = FORM` and `openInDialog = false`.

The page SHALL present a create/input form. When the entity has a `TEMPLATE` behaviour, a template action SHALL pre-fill default values. Validation SHALL run before submit. On success, the page SHALL navigate to the created entity or return.

#### Scenario: Create form with template

- **WHEN** a `PageDefinition` has `container.type = FORM`
- **AND** the entity has `TEMPLATE` behaviour
- **THEN** the form loads default values via the template action on mount
- **AND** validation runs before the create action

**Key Helpers**: `UiPageHelper.getCreateActionForPage()`, `UiPageHelper.isValidationSupported()`, `UiPageContainerHelper.containerHasCRUD()`

---

### Requirement: Dashboard Page Generation

The generator SHALL produce a landing page for each `PageDefinition` with `dashboard = true`.

The dashboard SHALL be automatically navigated to after authentication.

#### Scenario: Empty dashboard

- **WHEN** a dashboard `PageDefinition` has no content
- **THEN** an empty dashboard container is generated

#### Scenario: Dashboard with content

- **WHEN** a dashboard `PageDefinition` has child elements
- **THEN** the dashboard renders summary widgets and quick links

**Key Helpers**: `UiPageContainerHelper.containerIsEmptyDashboard()`

---

### Requirement: Dialog Page Generation

The generator SHALL produce a modal dialog component for each `PageDefinition` with `openInDialog = true`.

Dialog size SHALL be controlled by `dialogSize` (XS, SM, MD, LG, XL). Dialogs SHALL be stackable — a dialog can open another dialog. Dialogs that produce a result SHALL return it to the caller.

For each dialog page, the generator SHALL produce:
- `src/dialogs/{DialogPath}/index.tsx` — dialog component
- `src/dialogs/{DialogPath}/hooks.tsx` — hook for opening the dialog
- `src/dialogs/{DialogPath}/context.tsx` — dialog context
- `src/dialogs/{DialogPath}/types.ts` — TypeScript interfaces
- `src/dialogs/{DialogPath}/customization.ts` — Pandino hook interface key

#### Scenario: Dialog with result

- **WHEN** a dialog `PageDefinition` has a result type
- **THEN** the dialog returns the result to the caller on close
- **AND** the generated hook exposes the result promise

#### Scenario: Nested dialogs

- **WHEN** a dialog action opens another dialog
- **THEN** the second dialog stacks on top of the first

**Key Helpers**: `UiPageHelper.getPagesForDialogs()`, `UiPageHelper.dialogHasResult()`, `UiPageHelper.dialogDataType()`

---

### Requirement: Selector Page Generation

The generator SHALL produce a specialized dialog for each `PageDefinition` with `isSelector = true` or `isRelationSelector = true`.

The selector SHALL present a table for picking items with filtering, sorting, and pagination. Add selectors SHALL support multi-select. Set selectors SHALL support single-select. The selector SHALL return selected item(s) to the caller.

#### Scenario: Add selector (multi-select)

- **WHEN** a selector page is opened for an Add action
- **THEN** the table allows multi-row selection
- **AND** the selected items are returned on confirmation

#### Scenario: Set selector (single-select)

- **WHEN** a selector page is opened for a Set action
- **THEN** the table allows single-row selection
- **AND** the selected item is returned on confirmation

---

### Requirement: Operation Input/Output Pages

The generator SHALL produce form pages for operation parameters when a `PageDefinition` is linked to a `CallOperationActionDefinition` with input/output parameters.

Input form pages SHALL collect operation parameters. Output pages SHALL display operation results. Faults SHALL show error dialogs.

#### Scenario: Operation with input

- **WHEN** an operation has an input parameter
- **THEN** a form page is generated to collect the input
- **AND** the form submits to the operation call action

#### Scenario: Operation with fault

- **WHEN** an operation call fails with a fault
- **THEN** an error dialog displays the fault information

**Key Helpers**: `UiPageHelper.getCallOperationActionForPage()`, `UiActionsHelper.isActionOnOperationInput()`

---

### Requirement: Page Action Lifecycle

Each page SHALL follow a standard lifecycle: mount (onInit + refresh), interact (user triggers actions), navigate (open pages/dialogs), unmount (cleanup).

Actions SHALL be defined as TypeScript interfaces and implemented in the page component. Custom action hooks via Pandino SHALL be able to override default implementations.

#### Scenario: Page mount with data loading

- **WHEN** a page mounts
- **THEN** the `onInit` action fires
- **AND** data loads via the refresh action

---

### Requirement: Container Reuse

When multiple `PageDefinition` elements share the same `PageContainer`, the generator SHALL produce one container component per unique `PageContainer` and multiple page/dialog wrappers that compose the container with different contexts.

#### Scenario: Shared container

- **WHEN** two pages reference the same `PageContainer`
- **THEN** one container component is generated
- **AND** two page wrappers compose it with different data contexts

**Key Helpers**: `UiPageHelper.getPageContainersToGenerate()`, `UiPageContainerHelper.getContainerUsers()`

**Templates**: `actor/src/pages/index.tsx.hbs`, `actor/src/pages/context.tsx.hbs`, `actor/src/pages/types.ts.hbs`, `actor/src/pages/customization.ts.hbs`, `actor/src/dialogs/index.tsx.hbs`, `actor/src/dialogs/hooks.tsx.hbs`, `actor/src/containers/page.tsx.hbs`, `actor/src/containers/dialog.tsx.hbs`

---

## Integration Test Coverage

- **ActionGroupTest**: Table pages (Galaxies, Matters), view pages (Planet), form containers, dialog pages for relations
- **CRUDActionsTest**: Table pages, view pages, form pages across 3 actors
- **RelationTest**: Selector pages for adding/setting relations, dialog-based relation management
- **OperationParametersTest**: Operation input/output form pages
- **SimpleOrderManagement**: Dashboard pages, multi-actor page routing
