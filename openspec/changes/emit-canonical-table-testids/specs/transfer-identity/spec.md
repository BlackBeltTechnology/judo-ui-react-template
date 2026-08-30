## ADDED Requirements

### Requirement: Canonical grid row and cell test IDs

Every rendered data-grid row SHALL carry `data-testid` equal to
`table::<table-id>::row::<resolved-transfer-id>`, and every rendered cell SHALL carry
`table::<table-id>::row::<resolved-transfer-id>::cell::<column-field>`. The `<table-id>` token SHALL
be the value produced by the template's element-identity helper (`sourceId` when present, otherwise
the sanitized `xmi:id`), the transfer segment SHALL be produced by `resolveTransferId`, and the
column segment SHALL be the model field name. Emission SHALL delegate to the helpers in
`utilities/transfer-id.ts.hbs`; no template SHALL re-implement the `::` joining grammar.

#### Scenario: Saved row and cell are uniquely addressable
- **GIVEN** a table whose model element carries a `sourceId`, and a row whose transfer has a `__signedIdentifier`
- **WHEN** the grid renders that row
- **THEN** exactly one element matches `table::<sourceId>::row::<signedIdentifier>`
- **AND** exactly one element matches `table::<sourceId>::row::<signedIdentifier>::cell::<column-field>` for each rendered column
- **AND** the row and cell selectors identify different DOM elements

#### Scenario: Column segment is model-derived, not presentational
- **GIVEN** a column whose model field is `customerName` and whose visible header is translated
- **WHEN** the cell test ID is constructed
- **THEN** the segment is `::cell::customerName`
- **AND** it does not use the translated header text, the visual column index, or the current column order

#### Scenario: Unsaved row uses the temp identity
- **GIVEN** a client-created row that has been seeded with `__tempId` and `__isNew`
- **WHEN** the grid renders that row
- **THEN** the row test ID's transfer segment is the `__tempId` value
- **AND** the value carries the `temp::<ms>::<7-hex>` shape produced by `newTempId`

#### Scenario: Transfer identity precedence matches the runtime
- **GIVEN** a transfer object
- **WHEN** its row test ID is constructed
- **THEN** the transfer segment resolves in the order `__signedIdentifier`, `__identifier`, `__tempId`, `id`, then `idx-<index>`
- **AND** the resolved value equals what `@judo/test-ids`' `resolveTransferId` returns for the same object and index

#### Scenario: Cross-engine equality for the same model and data
- **GIVEN** the same UI model element and the same transfer object rendered by this template and by `judo-frontend-runtime`
- **WHEN** both engines emit the row and cell test IDs
- **THEN** the emitted strings are identical
- **AND** a single Playwright locator built from the model identity matches on both engines

### Requirement: Canonical selection-cell test ID

When a grid renders a selection checkbox column, the checkbox's owning cell SHALL carry
`table::<table-id>::row::<resolved-transfer-id>::cell::__check__`, so a row's selection control is
addressable through the same row hierarchy as its data cells and requires no DOM-shape locator.

#### Scenario: Selection checkbox is addressable through the row hierarchy
- **GIVEN** a table rendered with checkbox selection enabled
- **WHEN** a row is rendered
- **THEN** exactly one element matches `table::<table-id>::row::<transfer-id>::cell::__check__`
- **AND** an interactive checkbox is reachable within that element
- **AND** the selection cell is not addressed by a CSS class, `[data-field]`, or positional index

#### Scenario: No selection column emits no selection cell
- **GIVEN** a table rendered without checkbox selection
- **WHEN** its rows are rendered
- **THEN** no element matches `…::cell::__check__` for any row

### Requirement: Canonical filter-panel test IDs

The data-grid filter panel SHALL carry `table::<table-id>::filter-panel`, and its column, operator
and value inputs SHALL carry that value suffixed with `::column`, `::operator` and `::value`
respectively. The inputs SHALL be stamped through the grid's own filter-form properties so the IDs
survive MUI's internal rendering.

#### Scenario: Filter inputs are addressable without label locators
- **GIVEN** a table that supports filtering
- **WHEN** the filter panel is opened
- **THEN** exactly one element matches `table::<table-id>::filter-panel`
- **AND** exactly one element each matches its `::column`, `::operator` and `::value` suffixes
- **AND** no filter input is addressed by translated label text, placeholder, or DOM position

#### Scenario: Filter-panel IDs are table-scoped
- **GIVEN** two tables rendered on one page
- **WHEN** the filter panel of one table is opened
- **THEN** its panel and input test IDs carry that table's own identity
- **AND** they do not match the other table's panel selectors

## MODIFIED Requirements

### Requirement: Row-action buttons use the canonical row-scoped action grammar

Row-action buttons SHALL carry `table::<table-id>::row::<resolved-transfer-id>::action::<role>`,
where `<role>` is the normalized action role shared with the runtime (for example `set`, `create`,
`view`, `delete`). An overflow/dropdown trigger SHALL use the reserved role `overflow`. When no role
can be resolved for a button, emission SHALL fall back to the standalone button grammar
`button::<id>::<actionType>`. The previous form `…::row::<id>::button::<name>`, keyed on a free-form
`testId ?? id`, SHALL NOT be emitted.

This resolves the row-action grammar left open as design decision D3 of
`align-buttons-with-runtime`: `judo-frontend-runtime`'s `RowActionCell` renderer resolves a role and
calls `getRowActionTestId`, using `getButtonTestId` only as the no-role fallback, so the row-scoped
`::action::<role>` form is the settled canonical shape.

#### Scenario: Row action is addressed by canonical role
- **GIVEN** a table row rendering a delete row-action
- **WHEN** the action button is rendered
- **THEN** its test ID is `table::<table-id>::row::<transfer-id>::action::delete`
- **AND** no element for that action matches `…::row::<transfer-id>::button::<anything>`

#### Scenario: Overflow trigger uses the reserved role
- **GIVEN** a row whose actions exceed the featured-action count
- **WHEN** the overflow dropdown trigger is rendered
- **THEN** its test ID is `table::<table-id>::row::<transfer-id>::action::overflow`

#### Scenario: Role-less button keeps the standalone grammar
- **GIVEN** a row-action button for which no action role can be resolved
- **WHEN** its test ID is constructed
- **THEN** the emitted value is the standalone `button::<id>::<actionType>` form
- **AND** the emission does not produce an empty or `unknown` role segment

#### Scenario: Cross-engine equality for row actions
- **GIVEN** the same row-action button and transfer object rendered by this template and by `judo-frontend-runtime`
- **WHEN** both engines emit the row-action test ID
- **THEN** the emitted strings are identical
