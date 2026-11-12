## ADDED Requirements

### Requirement: Pro Column Layout Persistence

When the MUI license plan is Pro, generated `LazyTable` and `EagerTable` components SHALL save the
column order and widths a user sets, per table, under `${uniqueId}-columnState` in `sessionStorage`,
and SHALL restore them when the table is rendered again in the same browser tab. The selection
checkbox column (`__check__`) SHALL NOT be saved.

#### Scenario: Layout restored after reload

- **WHEN** a user reorders or resizes columns on a Pro table and reloads the page in the same tab
- **THEN** the table shows the saved column order and widths

#### Scenario: No saved layout

- **WHEN** a Pro table has no saved column state
- **THEN** the columns appear in model order followed by the actions column

#### Scenario: Community plan

- **WHEN** the `muiLicensePlan` template parameter is not `pro`
- **THEN** the generated tables contain no column-state save or restore code

### Requirement: Saved Column Layout Tolerates Model Changes

Restoring a saved Pro column layout SHALL NOT fail when the table's columns changed after the layout
was saved. Saved entries without a matching column SHALL be ignored, and current columns missing from
the saved state SHALL be shown after the saved ones, in model order. Restoring SHALL NOT mutate the
stored column-state array order.

#### Scenario: Saved layout names a removed column

- **WHEN** the saved column state contains a `field` that the table no longer has
- **THEN** the table renders without error, shows its remaining columns in the saved order, and ignores the unknown field

#### Scenario: Column added after the layout was saved

- **WHEN** the table has a column that is not in the saved column state
- **THEN** that column is shown after the saved columns, in model order

#### Scenario: Actions column not in the saved state

- **WHEN** the saved column state does not contain the `actions` column
- **THEN** the actions column is still rendered, after the saved columns

#### Scenario: Saved width for a known column

- **WHEN** a saved entry has a `width` and its `field` matches a current column
- **THEN** that column is rendered with the saved width
