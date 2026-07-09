## MODIFIED Requirements

### Requirement: Row Selection

The generator SHALL produce row selection functionality on tables.

`checkboxSelection = ENABLED` SHALL show a checkbox column. `checkboxSelection = DISABLED` SHALL hide checkboxes. `checkboxSelection = AUTO` SHALL show checkboxes only when bulk actions exist. `allowSelectMultiple = true` SHALL enable multi-row selection. Selected rows SHALL be available to bulk actions.

Row identity for selection SHALL be resolved through the `resolveTransferId` helper defined in the `transfer-identity` capability. The DataGrid SHALL pass `getRowId={resolveTransferId}` (or an equivalent callback) — never a static `identifierAttribute` string. Selection model construction (`arrayToSelectionModel(selectedRows.current.map(resolveTransferId))`) SHALL derive keys through the same helper. Row `data-testid` attributes SHALL be constructed via `buildRowTestId(tableId, row)` per the `transfer-identity` capability.

#### Scenario: Multi-row selection with checkboxes

- **WHEN** a table has `checkboxSelection = ENABLED` and `allowSelectMultiple = true`
- **THEN** a checkbox column is shown and multiple rows can be selected
- **AND** each row's `data-testid` is `table::<tableId>::row::${resolveTransferId(row)}`

#### Scenario: Selection persists after a saved row's `__identifier` changes

- **GIVEN** a saved row selected before backend confirmation
- **WHEN** the server refresh returns the row with a new `__signedIdentifier` and the previous `__identifier` cleared
- **THEN** the selection model tracks the row via `resolveTransferId` and remains consistent

**Key Helpers**: (unchanged from baseline)
**Runtime Helper**: `resolveTransferId` in `src/utilities/transfer-id.ts` (see `transfer-identity`)

---

### Requirement: Row Actions

The generator SHALL produce per-row action buttons from `Table.rowActionButtonGroup`.

Each row SHALL display action buttons (view, edit, delete, custom). `crudOperationsDisplayed` SHALL control how many buttons are visible; excess buttons SHALL go to an overflow menu. Row delete actions SHALL include a confirmation dialog. Row open page actions SHALL navigate to the entity detail view.

Per-row action buttons SHALL emit `data-testid` values constructed through the helpers defined in the `transfer-identity` capability. Concretely:

- The row-action container `data-testid` SHALL be `` `${buildRowTestId(tableId, row)}::actions` ``.
- Each button's `data-testid` SHALL be `` `${buildRowTestId(tableId, row)}::button::${actionRole}` `` where `actionRole` is drawn from the role vocabulary (`view`, `set`, `remove`, `clear`, or a compound `menu::item::<name>` for overflow entries).

Row-mode operations (edit / cancel / save state per row inside `LazyTable` and `EagerTable`) SHALL key the `rowModesModel` object using `resolveTransferId(rowData)`. No template SHALL retain a `rowData.__identifier!` non-null assertion.

#### Scenario: Row actions with overflow

- **WHEN** a table has `rowActionButtonGroup` with 5 buttons and `crudOperationsDisplayed = 2`
- **THEN** 2 buttons are visible per row and 3 are in an overflow menu
- **AND** each visible button carries a `data-testid` beginning with `` `${buildRowTestId(tableId, row)}::button::` ``

#### Scenario: Row-mode model keyed by resolver

- **WHEN** the user enters edit mode on a row
- **THEN** the `rowModesModel` state is keyed by `resolveTransferId(rowData)`, not by `rowData.__identifier`

**Key Helpers**: (unchanged from baseline)
**Runtime Helper**: `resolveTransferId`, `buildRowTestId` (see `transfer-identity`)

---

### Requirement: Inline Row Creation

When a table has an `InlineCreateRowActionDefinition`, the generator SHALL produce inline row creation.

A new empty row SHALL appear in the table. The user SHALL fill in values inline. The row SHALL be saved via the create action.

The new row object SHALL be seeded with `__tempId: newTempId()` and `__isNew: true` and SHALL NOT contain a `__identifier` field. The predicate for distinguishing not-yet-saved rows in the save/refresh path SHALL be `isNewRow(rowData)`, never `rowData.__identifier!.startsWith(draftIdentifierPrefix)`. Both helpers are defined in the `transfer-identity` capability.

#### Scenario: Inline row creation

- **WHEN** a table has an `InlineCreateRowActionDefinition`
- **THEN** a new empty row appears in the table for inline editing
- **AND** the row object has `__tempId` matching `/^temp::\d+::[0-9a-f]+$/` and `__isNew: true`
- **AND** the row object has no `__identifier` field

#### Scenario: Save routes new row through create endpoint

- **GIVEN** an inline-created row with `__tempId` and `__isNew: true`
- **WHEN** the user commits the row
- **THEN** the save handler evaluates `isNewRow(rowData)` and calls the create service (not the update service)

**Runtime Helpers**: `newTempId`, `isNewRow` (see `transfer-identity`)
