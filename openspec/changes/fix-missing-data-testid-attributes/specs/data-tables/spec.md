## MODIFIED Requirements

### Requirement: Column Configuration

The generator SHALL produce `GridColDef` definitions from `Column` elements on a table.

Column type SHALL be determined by the bound attribute's data type: `StringType` → `string`, `NumericType` → `number`, `BooleanType` → `boolean`, `DateType` → `date`, `TimestampType` → `dateTime`, `TimeType` → `string` (formatted), `EnumerationType` → `singleSelect` with enum options, `BinaryType` → `string` (filename display).

`width` SHALL set the column width; defaults SHALL be type-dependent. `format` pattern SHALL apply value formatting. `formatValue` SHALL control whether values are formatted or shown raw.

Each column header SHALL render a `data-testid` attribute matching the column's XMI ID with a table discriminator suffix. The `GridColDef` SHALL include a `renderHeader` function that wraps the header content in a `<span>` element with `data-testid` set to the column's declared VisualElementId pattern: `{columnXMIID}/TableColumn/(discriminator/{tableXMIID}/{tableTypeSuffix})`.

#### Scenario: Columns with typed formatting

- **WHEN** a table has columns bound to various data types
- **THEN** each column is configured with the correct grid column type and formatting

#### Scenario: Column headers have data-testid

- **WHEN** a table has columns
- **THEN** each column header renders a `<span>` with `data-testid` matching the column's declared VisualElementId
- **AND** the `data-testid` value follows the pattern `{columnXMIID}/TableColumn/(discriminator/{tableXMIID}/{tableTypeSuffix})`

**Key Helpers**: `UiTableHelper.columnType()`, `UiTableHelper.columnWidth()`, `UiTableHelper.isColumnString()`, `UiTableHelper.isColumnNumeric()`, `UiTableHelper.isColumnBoolean()`

---

### Requirement: Row Actions

The generator SHALL produce per-row action buttons from `Table.rowActionButtonGroup`.

Each row SHALL display action buttons (view, edit, delete, custom). `crudOperationsDisplayed` SHALL control how many buttons are visible; excess buttons SHALL go to an overflow menu. Row delete actions SHALL include a confirmation dialog. Row open page actions SHALL navigate to the entity detail view.

All buttons in `rowActionButtonGroup` SHALL be rendered, including `OpenPageActionDefinition` (view) buttons. The generator SHALL NOT skip any button type from the row actions array.

#### Scenario: Row actions with overflow

- **WHEN** a table has `rowActionButtonGroup` with 5 buttons and `crudOperationsDisplayed = 2`
- **THEN** 2 buttons are visible per row and 3 are in an overflow menu

#### Scenario: Row view button is rendered

- **WHEN** a table has a `RowOpenPageActionDefinition` button in `rowActionButtonGroup`
- **THEN** the View button SHALL be included in the `rowActions` array with its XMI ID as `data-testid`
- **AND** the View button coexists with the `onRowClick` navigation handler

**Key Helpers**: `UiActionsHelper.getRowDeleteActionDefinitionForTable()`, `UiWidgetHelper.tableRowButtonHiddenConditions()`, `UiWidgetHelper.tableRowButtonDisabledConditions()`
