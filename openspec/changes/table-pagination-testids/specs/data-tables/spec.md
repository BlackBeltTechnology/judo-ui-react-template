## ADDED Requirements

### Requirement: Table pager carries canonical test ids

The footer pager of an `EagerTable` and a `LazyTable` SHALL mark its root, previous and next buttons, rows-per-page combobox and each rows-per-page option with `table::<id>::pagination`, `::previous`, `::next`, `::page-size` and `::page-size::option::<n>`, built by `buildTablePaginationTestId` from the table id of `table::<id>::grid`. The ids SHALL NOT change the pager's behaviour. Without a `tableTestId`, `CustomTablePagination` SHALL emit no pager ids.

#### Scenario: Eager table pager

- **WHEN** an `EagerTable` with table id `_T1` renders its footer pager
- **THEN** the pager root carries `data-testid="table::_T1::pagination"` and its buttons carry `table::_T1::pagination::previous` and `table::_T1::pagination::next`
- **AND** the rows-per-page combobox carries `table::_T1::pagination::page-size`

#### Scenario: Lazy table page-size options

- **WHEN** the user opens the rows-per-page menu of a `LazyTable` with table id `_T2` and page-size options 10 and 20
- **THEN** the options carry `table::_T2::pagination::page-size::option::10` and `table::_T2::pagination::page-size::option::20`

#### Scenario: Pager without a table id

- **WHEN** `CustomTablePagination` is rendered without `tableTestId`
- **THEN** it emits no `table::…::pagination` ids and behaves as before
