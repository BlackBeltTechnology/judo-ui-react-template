# table-row-actions-testids Specification

## Purpose
Specifies row-unique `data-testid` composition for per-row action buttons emitted by `columnsActionCalculator` in `table-row-actions.tsx`. Each row's edit/save/cancel/delete/view/custom-action button and dropdown menu items emit `data-testid="${actionId}-${row.__identifier}"` (composed at render time via the `buildRowTestId` helper), so an N-row table no longer produces N identical testids — satisfying Playwright strict-mode locators.
## Requirements
### Requirement: Per-row action buttons emit row-unique data-testid

The generated `columnsActionCalculator(...)` in `table-row-actions.tsx` SHALL emit per-row action button `data-testid` attributes that incorporate the row's `__identifier`. Specifically:

- For each split-action `<Button>` rendered inside `getActions(params: GridRowParams)`, the `data-testid` SHALL be composed as `${action.id}-${params.row.__identifier}` whenever `params.row.__identifier` is a non-empty string.
- For the per-row dropdown trigger `<Button>` rendered by `<DropdownButton>`, the `data-testid` SHALL be composed as `${id}-${params.row.__identifier}` (where `id` is the table's `dataElementId`).
- For each per-row dropdown `<MenuItem>` rendered inside `<DropdownButton menuItems>`, the `data-testid` SHALL be composed as `${action.id}-${params.row.__identifier}`.

If `params.row.__identifier` is missing (undefined, null, or empty string), the calculator SHALL fall back to emitting the bare `action.id` (preserving the prior behaviour for rows without a stable identifier). This fallback is a defensive measure; in practice every DataGrid row in the JUDO generated app carries a `__identifier`.

React `key` properties on Tooltip, MenuItem, and other elements SHALL continue to use the scalar `action.id` (or stable equivalents) — only the `data-testid` attribute is row-suffixed. This decoupling preserves React keying semantics while making every emitted testid in the DOM globally unique within one table mount.

The suppression behaviour for selector-overlay mounts (covered by the `table-toolbar-rendering` capability) takes precedence over row composition: when `isSelectorMount === true`, no `data-testid` is emitted regardless of row identifier.

#### Scenario: Three-row table emits three distinct row-edit testids

- **GIVEN** a generated table with rowEdit enabled (`editable === true`) and three rows whose `__identifier` values are `r1`, `r2`, `r3`
- **AND** none of the rows are in edit mode (so each row shows the Edit row-action button)
- **WHEN** the table renders
- **THEN** the DOM SHALL contain exactly three `<Button>` elements with `data-testid` values `<tableXmiId>-row-edit-r1`, `<tableXmiId>-row-edit-r2`, `<tableXmiId>-row-edit-r3`
- **AND** `page.getByTestId('<tableXmiId>-row-edit-r2')` SHALL resolve to exactly one element (no Playwright strict-mode violation)

#### Scenario: Row in edit mode swaps to per-row Save and Cancel testids

- **GIVEN** the same table as the previous scenario, with row `r2` placed into edit mode
- **WHEN** the table renders
- **THEN** the DOM SHALL contain `<tableXmiId>-row-edit-r1` and `<tableXmiId>-row-edit-r3` (no Edit button for r2)
- **AND** the DOM SHALL contain exactly one `<tableXmiId>-row-save-r2` and exactly one `<tableXmiId>-row-cancel-r2`
- **AND** the DOM SHALL contain zero `<tableXmiId>-row-save-r1`, `<tableXmiId>-row-save-r3` (Save is hidden when not in edit mode)

#### Scenario: Container-supplied row actions inherit the row-suffix automatically

- **GIVEN** a TableComponent whose container generates `tableRowActions` including a custom action with `id: '<tableXmiId>-delete-row'`
- **AND** the table has N rows
- **WHEN** the table renders
- **THEN** the DOM SHALL contain N distinct buttons with `data-testid` values `<tableXmiId>-delete-row-<rowId1>`, …, `<tableXmiId>-delete-row-<rowIdN>`
- **AND** no template or container code change SHALL be required for container-supplied actions to acquire this property — the composition happens at the render site

#### Scenario: Row without __identifier falls back to bare action id

- **GIVEN** a row whose `__identifier` is `undefined` or empty string (edge case)
- **WHEN** the table renders that row's action buttons
- **THEN** the `data-testid` SHALL fall back to the bare `action.id` (matching pre-change behaviour for that single row)
- **AND** the render SHALL NOT throw or produce `undefined` in the testid string (no `"…-undefined"` artifacts)

#### Scenario: Dropdown trigger and menu items both row-suffixed

- **GIVEN** a table with more row actions than `crudOperationsDisplayed + transferOperationsDisplayed`, so a dropdown trigger is rendered alongside the split actions
- **AND** N rows in the grid
- **WHEN** the table renders
- **THEN** the DOM SHALL contain N dropdown trigger `<Button>` elements with `data-testid` values `<tableXmiId>-<rowId1>`, …, `<tableXmiId>-<rowIdN>` (one per row, all distinct)
- **AND** when the user opens the dropdown for row `rK`, the menu's `<MenuItem>` elements SHALL each carry `data-testid="<menuItemActionId>-<rowK identifier>"`
- **AND** Playwright `getByTestId('<menuItemActionId>-<rowK identifier>')` SHALL resolve to exactly one element while the menu is open

