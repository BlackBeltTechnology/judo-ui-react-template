# Data Tables

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

`checkboxSelection = ENABLED` SHALL show a checkbox column. `checkboxSelection = DISABLED` SHALL hide checkboxes. `checkboxSelection = AUTO` SHALL show checkboxes only when bulk actions exist. `allowSelectMultiple = true` SHALL enable multi-row selection. Selected rows SHALL be available to bulk actions.

#### Scenario: Multi-row selection with checkboxes

- **WHEN** a table has `checkboxSelection = ENABLED` and `allowSelectMultiple = true`
- **THEN** a checkbox column is shown and multiple rows can be selected

---

### Requirement: Row Actions

The generator SHALL produce per-row action buttons from `Table.rowActionButtonGroup`.

Each row SHALL display action buttons (view, edit, delete, custom). `crudOperationsDisplayed` SHALL control how many buttons are visible; excess buttons SHALL go to an overflow menu. Row delete actions SHALL include a confirmation dialog. Row open page actions SHALL navigate to the entity detail view.

#### Scenario: Row actions with overflow

- **WHEN** a table has `rowActionButtonGroup` with 5 buttons and `crudOperationsDisplayed = 2`
- **THEN** 2 buttons are visible per row and 3 are in an overflow menu

**Key Helpers**: `UiActionsHelper.getRowDeleteActionDefinitionForTable()`, `UiWidgetHelper.tableRowButtonHiddenConditions()`, `UiWidgetHelper.tableRowButtonDisabledConditions()`

---

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

#### Scenario: Inline row creation

- **WHEN** a table has an `InlineCreateRowActionDefinition`
- **THEN** a new empty row appears in the table for inline editing

---

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

## Integration Test Coverage

- **ActionGroupTest** (Community): LazyTable, EagerTable, table pages with filters, sorting, row actions, Galaxy domain tables
- **ActionGroupTestPro** (Pro): DataGridPro tables with pro-specific features, inline column filters
- **CRUDActionsTest**: CRUD operations on table pages across multiple actors
- **RelationTest**: Tags representation, card representation, inline editing
