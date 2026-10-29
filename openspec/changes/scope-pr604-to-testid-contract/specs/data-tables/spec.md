## ADDED Requirements

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
