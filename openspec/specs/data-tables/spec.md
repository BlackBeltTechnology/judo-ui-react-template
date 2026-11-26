# Data Tables

## Purpose

Generates MUI DataGrid-based table components from UI `Table` model elements. Tables are the primary mechanism for displaying collections of data with sorting, filtering, pagination, row selection, CRUD actions, and inline editing. The generator supports three MUI license tiers (Community, Pro, Premium) and three visual representations (TABLE, TAG, CARD).

## Requirements

### Requirement: Lazy Table Generation

The generator SHALL produce a `<LazyTable>` component wrapping MUI DataGrid from `Table` elements with `isEager = false` (default).

Data SHALL be loaded page-by-page from the server via the refresh action. Sort and filter parameters SHALL be sent to the server with each request. Pagination controls SHALL be rendered at the bottom.

For each lazy table, the generator SHALL produce:
- `src/containers/{ContainerPath}/components/{TableName}/index.tsx` — table component
- `src/containers/{ContainerPath}/components/{TableName}/types.ts` — TypeScript types

#### Scenario: Lazy table with server-side pagination

- **WHEN** a `Table` element has `isEager = false`
- **THEN** data is loaded page-by-page from the server
- **AND** pagination, sorting, and filtering are server-side

**Key Helpers**: `UiWidgetHelper.tableComponentName()`, `UiTableHelper.calculateTablePageLimit()`, `UiActionsHelper.getRefreshActionDefinitionForTable()`

**Template**: `actor/src/containers/components/table/index.tsx.hbs`

---

### Requirement: Eager Table Generation

The generator SHALL produce an `<EagerTable>` component wrapping MUI DataGrid from `Table` elements with `isEager = true`.

All data SHALL be loaded at once into client memory. Sorting and filtering SHALL be performed client-side.

#### Scenario: Eager table with client-side data

- **WHEN** a `Table` element has `isEager = true`
- **THEN** all data is loaded into memory at once
- **AND** sorting and filtering are performed client-side

---

### Requirement: Column Configuration

The generator SHALL produce `GridColDef` definitions from `Column` elements on a table.

Column type SHALL be determined by the bound attribute's data type: `StringType` → `string`, `NumericType` → `number`, `BooleanType` → `boolean`, `DateType` → `date`, `TimestampType` → `dateTime`, `TimeType` → `string` (formatted), `EnumerationType` → `singleSelect` with enum options, `BinaryType` → `string` (filename display).

`width` SHALL set the column width; defaults SHALL be type-dependent. `format` pattern SHALL apply value formatting. `formatValue` SHALL control whether values are formatted or shown raw.

#### Scenario: Columns with typed formatting

- **WHEN** a table has columns bound to various data types
- **THEN** each column is configured with the correct grid column type and formatting

**Key Helpers**: `UiTableHelper.columnType()`, `UiTableHelper.columnWidth()`, `UiTableHelper.isColumnString()`, `UiTableHelper.isColumnNumeric()`, `UiTableHelper.isColumnBoolean()`

---

### Requirement: Aggregated Relation Columns

When a `Column` has `representsRelation` set to a `RelationType`, the generator SHALL produce a column that displays data from a related entity rather than a direct attribute.

`representsRelation` SHALL identify the relation path. `representationOfRelation` SHALL identify the display attribute on the related entity. Additional mask relations SHALL ensure the related data is fetched.

#### Scenario: Column showing related entity data

- **WHEN** a column has `representsRelation` set
- **THEN** the column displays the `representationOfRelation` attribute from the related entity

---

### Requirement: Sorting

The generator SHALL support column sorting on tables.

Columns with `sort = ASC` or `sort = DESC` SHALL be default-sorted on load. `sortPrecedence` SHALL determine multi-column sort order (higher value = sorted first). Users SHALL be able to click column headers to toggle sort. For lazy tables, sort parameters SHALL be sent to the server. For eager tables, sorting SHALL be client-side.

#### Scenario: Multi-column default sort

- **WHEN** multiple columns have `sort != NONE` with different `sortPrecedence` values
- **THEN** the table loads with multi-column sort applied in precedence order

**Key Helpers**: `UiTableHelper.getDefaultSortParamsForTable()`, `UiTableHelper.getSortDirection()`, `UiWidgetHelper.getSortColumnForTable()`

---

### Requirement: Filtering

The generator SHALL produce filter components for tables with `Filter` definitions.

A filter button SHALL open a `<FilterDialog>` with configurable operators per attribute. When `alwaysShown` is `true`, the filter SHALL render inline above the table. When `range` is `true`, min/max inputs SHALL be shown for numeric/date types. When `multiValue` is `true`, multiple filter values SHALL be selectable. Filter operations SHALL map to the `FilterOperationType` enum (EQUAL, NOT_EQUAL, IS_EMPTY, IS_NOT_EMPTY, LESS, LESS_OR_EQUAL, GREATER, GREATER_OR_EQUAL, LIKE).

When the MUI license is Pro and `useInlineColumnFilters` is enabled, inline column filters SHALL be generated.

#### Scenario: Dialog filter

- **WHEN** a table has filter definitions with `alwaysShown = false`
- **THEN** a filter button opens a filter dialog with operators per attribute

#### Scenario: Always-shown filter

- **WHEN** a filter has `alwaysShown = true`
- **THEN** the filter renders inline above the table

#### Scenario: Inline column filters (Pro)

- **WHEN** the MUI license is Pro and `useInlineColumnFilters = true`
- **THEN** filters are rendered directly in the column headers

**Key Helpers**: `UiTableHelper.getFilterTypeForAttribute()`, `UiTableHelper.getFilterTypeForFilter()`, `ReactStoredVariableHelper.isUseInlineColumnFilters()`

**Template**: `src/components/table/FixedTableFilters.tsx.hbs`

---

### Requirement: Pagination

The generator SHALL produce pagination controls for tables.

`rowsPerPage` SHALL set the default page size. The template parameter `tablePageLimit` SHALL be able to override the model value. A `<CustomTablePagination>` component SHALL provide page navigation. For lazy tables, page navigation SHALL trigger server requests.

#### Scenario: Paginated lazy table

- **WHEN** a lazy table has `rowsPerPage = 10`
- **THEN** data is loaded 10 rows at a time with pagination controls

**Key Helpers**: `UiTableHelper.calculateTablePageLimit()`

---

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

#### Scenario: Selection persists after a saved row's `__identifier` changes

- **GIVEN** a saved row selected before backend confirmation
- **WHEN** the server refresh returns the row with a new `__signedIdentifier` and the previous `__identifier` cleared
- **THEN** the selection model tracks the row via `resolveTransferId` and remains consistent

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

#### Scenario: Row-mode model keyed by resolver

- **WHEN** the user enters edit mode on a row
- **THEN** the `rowModesModel` state is keyed by `resolveTransferId(rowData)`, not by `rowData.__identifier`

**Key Helpers**: `UiActionsHelper.getRowDeleteActionDefinitionForTable()`, `UiWidgetHelper.tableRowButtonHiddenConditions()`, `UiWidgetHelper.tableRowButtonDisabledConditions()`

### Requirement: Table-Level Actions

The generator SHALL produce table-level action buttons from `Table.tableActionButtonGroup`.

Actions SHALL be displayed above/beside the table (create, add, export, bulk operations). Bulk actions SHALL operate on selected rows (bulk delete, bulk remove, bulk call operation). Export actions SHALL generate a data export.

#### Scenario: Table with bulk delete

- **WHEN** a table has a `BulkDeleteActionDefinition` in its table action button group
- **AND** rows are selected
- **THEN** the bulk delete action operates on all selected rows

**Key Helpers**: `UiActionsHelper.getBulkRemoveActionDefinitionForTable()`, `UiPageHelper.hasExportAction()`

---

### Requirement: Inline Cell Editing

When a `Table` has `isInlineEditable = true`, the generator SHALL produce inline cell editing capabilities.

Cells SHALL become editable on click/double-click. Cell edit type SHALL be determined by the column data type. Changes SHALL be saved per-cell or per-row.

#### Scenario: Inline editable table

- **WHEN** a table has `isInlineEditable = true`
- **THEN** cells are editable inline with type-appropriate editors

**Key Helpers**: `UiWidgetHelper.getCellEditType()`, `UiTableHelper.isColumnEditable()`

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

### Requirement: Table Representations

The generator SHALL support three visual representations based on `Table.representationComponent`.

`TABLE` (default) SHALL render a standard MUI DataGrid. `TAG` SHALL render a tag/chip display for compact collection views. `CARD` SHALL render a card grid layout for visual collections.

For TAG representation, the generator SHALL produce a `<Tags>` component. For CARD representation, the generator SHALL produce a `<CardsContainer>` with `<DefaultCard>` and a `customization.ts` file for card configuration hooks.

#### Scenario: Tag representation

- **WHEN** a table has `representationComponent = TAG`
- **THEN** the collection is rendered as a tag/chip display

#### Scenario: Card representation

- **WHEN** a table has `representationComponent = CARD`
- **THEN** the collection is rendered as a card grid with customization hooks

**Key Helpers**: `UiWidgetHelper.tagComponentName()`, `UiWidgetHelper.cardsComponentName()`, `UiWidgetHelper.getTablesWithCardRepresentations()`

---

### Requirement: Total Count Display

When a `Table` has `showTotalCount = true`, the generator SHALL display the total number of records in the pagination area.

This SHALL require an additional count API call.

#### Scenario: Table with total count

- **WHEN** a table has `showTotalCount = true`
- **THEN** the total record count is displayed in the pagination area

**Key Helpers**: `UiPageContainerHelper.containerHasTableWithTotalCount()`

---

### Requirement: Data Masking

The generator SHALL create a data mask specifying which attributes to fetch from the API for each table.

Visible columns plus `additionalMaskAttributes` SHALL form the mask. `additionalMaskRelations` SHALL add related object prefetching. The mask SHALL be serialized as a JSON-like query customizer.

#### Scenario: Table with additional mask attributes

- **WHEN** a table has `additionalMaskAttributes` set
- **THEN** the mask includes both visible column attributes and the additional attributes

**Key Helpers**: `UiPageContainerHelper.getMaskForTable()`, `UiPageContainerHelper.serializeMaskForTable()`

---

### Requirement: MUI License Tier Differentiation

The generator SHALL select the appropriate MUI DataGrid component based on the configured license tier.

Community tier SHALL use `<DataGrid>`. Pro tier SHALL use `<DataGridPro>` with inline column filters, multi-column sorting, and column pinning. Premium tier SHALL use `<DataGridPremium>`.

#### Scenario: Pro-licensed table features

- **WHEN** the `muiLicensePlan` template parameter is `pro`
- **THEN** the generator uses `<DataGridPro>` and enables pro-specific features

**Key Helpers**: `ReactStoredVariableHelper.isMUILicensed()`, `ReactStoredVariableHelper.muiDataGridComponent()`, `ReactStoredVariableHelper.getMUIDataGridPlanSuffix()`

---

### Requirement: Row Highlight Legend

When a table has configured row stylers, the generator SHALL render one legend entry per styler: a dot filled with the styler's unchanged `backgroundColor` and outlined in `theme.palette.grey[600]`, followed by the styler's translated label (`table-custom-row-style-<name>`).

#### Scenario: Light styler colour stays visible

- **GIVEN** a table with a row styler whose `backgroundColor` is white
- **WHEN** the legend is rendered on the white table card
- **THEN** the legend dot is outlined in `theme.palette.grey[600]`
- **AND** the dot remains distinguishable from the card
- **AND** the outline has at least 3:1 contrast against the white card (WCAG 2.2 SC 1.4.11)

#### Scenario: Fill colour is preserved

- **GIVEN** a table with a row styler whose `backgroundColor` is a saturated colour
- **WHEN** the legend is rendered
- **THEN** the dot's fill equals the styler's `backgroundColor`
- **AND** only a 1px outline is added around it

#### Scenario: Outline follows the theme

- **GIVEN** a consuming application that overrides the MUI `grey` palette
- **WHEN** the legend is rendered
- **THEN** the outline colour is read from the active theme rather than a hard-coded value

---

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

### Requirement: Boolean cells carry the runtime's value marker

A generated boolean table cell SHALL contain an element whose `data-testid` is `boolean-true`, `boolean-false` or `boolean-null` that matches the value, the same marker the runtime's table renders.

#### Scenario: True value
- **WHEN** a boolean column value is true
- **THEN** the cell contains an element with `data-testid="boolean-true"`

#### Scenario: Null value
- **WHEN** a boolean column value is null
- **THEN** the cell contains an element with `data-testid="boolean-null"`

### Requirement: Grid and loading overlay carry the runtime's ids

A generated table's DataGrid SHALL carry `table::<id>::grid`, and its loading overlay SHALL carry `table::<id>::loading`, the ids the runtime emits for the same table.

#### Scenario: Waiting for a table to load
- **WHEN** a table is fetching its rows
- **THEN** an element with `data-testid="table::<id>::loading"` is shown, and it disappears when the rows are rendered

### Requirement: Fixed filters carry the runtime's ids

A generated table's always-shown filter bar SHALL carry `table::<id>::fixed-filters`, and each always-shown filter SHALL carry `table::<id>::fixed-filter::<attributeName>` on its field root, the ids the runtime emits.

#### Scenario: Filtering by an always-shown string filter
- **GIVEN** a table with an always-shown filter on `name`
- **WHEN** the table renders
- **THEN** the filter's root carries `data-testid="table::<id>::fixed-filter::name"` inside `table::<id>::fixed-filters`

## Integration Test Coverage

- **ActionGroupTest** (Community): LazyTable, EagerTable, table pages with filters, sorting, row actions, Galaxy domain tables
- **ActionGroupTestPro** (Pro): DataGridPro tables with pro-specific features, inline column filters
- **CRUDActionsTest**: CRUD operations on table pages across multiple actors
- **RelationTest**: Tags representation, card representation, inline editing
