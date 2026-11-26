## ADDED Requirements

### Requirement: Bulk actions act only on checked rows

After a bulk action completes or the table is cleared, a table SHALL reset its selection with an empty selection model, so `selectedRows` and the grid selection hold no row. A later bulk action SHALL receive only the rows that are checked at that moment.

#### Scenario: Re-added row is not removed again

- **WHEN** the user bulk-removes row `dark`, re-adds `dark` through the Add selector, checks two other rows and clicks bulk Remove
- **THEN** the remove request contains only the two checked rows
- **AND** `dark` stays in the table

#### Scenario: Clear resets the selection

- **WHEN** the user clicks Clear on a table that has checked rows
- **THEN** no row stays selected and bulk actions that need a selection are disabled
