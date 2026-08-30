## ADDED Requirements

### Requirement: DataGrid slot registration for canonical test IDs

Both generated table components (`LazyTable`, `EagerTable`) SHALL register `row`, `cell` and
`filterPanel` MUI DataGrid slots whose sole added responsibility is stamping the canonical
`data-testid` attributes defined by the `transfer-identity` capability. The slot wrappers SHALL
forward every prop they receive to the underlying MUI component, SHALL NOT alter grid behavior,
layout, virtualization, editing or selection, and SHALL be memoized on the table identity so slot
identity is stable across renders.

Because both this template and `judo-frontend-runtime` render on MUI x-data-grid v8, the slot
wrappers SHALL mirror the runtime's implementation
(`packages/components/src/renderers/TableRenderer.tsx`) rather than introducing a
template-specific mechanism.

#### Scenario: Row and cell slots stamp IDs without changing behavior
- **WHEN** a table is rendered with the row and cell slots registered
- **THEN** each rendered row and cell carries its canonical `data-testid`
- **AND** sorting, filtering, pagination, inline editing, row selection and virtualization behave exactly as before the slots were registered

#### Scenario: Filter-panel slot stamps inputs through filter-form properties
- **WHEN** the filter panel is rendered through the registered slot
- **THEN** the panel wrapper carries the canonical panel test ID
- **AND** the column, operator and value inputs receive their canonical test IDs via the grid's filter-form properties
- **AND** the existing logic-operator restriction on the filter panel is preserved

#### Scenario: Slot registration applies to both table variants
- **WHEN** a model contains both an eager and a lazy table
- **THEN** both generated components register the row, cell and filter-panel slots
- **AND** the emitted test IDs follow the same grammar in both

#### Scenario: Pro and community license tiers both emit canonical IDs
- **GIVEN** the generator is configured for either the community or the Pro MUI license tier
- **WHEN** a table is generated and rendered
- **THEN** the canonical row, cell and filter-panel test IDs are emitted in both tiers
- **AND** tier-specific features such as pinned action columns continue to work

## MODIFIED Requirements

### Requirement: Row Selection

The generator SHALL produce row selection functionality on tables.

`checkboxSelection = ENABLED` SHALL show a checkbox column. `checkboxSelection = DISABLED` SHALL hide
checkboxes. `checkboxSelection = AUTO` SHALL show checkboxes only when bulk actions exist.
`allowSelectMultiple = true` SHALL enable multi-row selection. Selected rows SHALL be available to
bulk actions.

When a checkbox column is shown, each row's selection cell SHALL be addressable through the
canonical row hierarchy defined by the `transfer-identity` capability, so a test can select a known
row without relying on a CSS class, a `[data-field]` attribute, or a positional index.

#### Scenario: Multi-row selection with checkboxes

- **WHEN** a table has `checkboxSelection = ENABLED` and `allowSelectMultiple = true`
- **THEN** a checkbox column is shown and multiple rows can be selected

#### Scenario: Selection cell of a known row is directly addressable

- **GIVEN** a table with a checkbox column and a row whose transfer identity is known
- **WHEN** that row is rendered
- **THEN** its selection cell carries the canonical `…::cell::__check__` test ID
- **AND** checking that cell's control selects exactly that row

### Requirement: Row Actions

The generator SHALL produce per-row action buttons from `Table.rowActionButtonGroup`.

Each row SHALL display action buttons (view, edit, delete, custom). `crudOperationsDisplayed` SHALL
control how many buttons are visible; excess buttons SHALL go to an overflow menu. Row delete actions
SHALL include a confirmation dialog. Row open page actions SHALL navigate to the entity detail view.

Each row-action button and the overflow trigger SHALL carry the canonical row-scoped action test ID
defined by the `transfer-identity` capability.

#### Scenario: Row actions with overflow

- **WHEN** a table has `rowActionButtonGroup` with 5 buttons and `crudOperationsDisplayed = 2`
- **THEN** 2 buttons are visible per row and 3 are in an overflow menu
- **AND** each visible button carries its canonical `…::action::<role>` test ID
- **AND** the overflow trigger carries `…::action::overflow`

**Key Helpers**: `UiActionsHelper.getRowDeleteActionDefinitionForTable()`, `UiWidgetHelper.tableRowButtonHiddenConditions()`, `UiWidgetHelper.tableRowButtonDisabledConditions()`
