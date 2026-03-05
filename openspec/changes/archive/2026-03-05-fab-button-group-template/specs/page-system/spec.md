## ADDED Requirements

### Requirement: FAB button group exclusion from page headers and dialog actions

When a `PageContainer` has `actionButtonGroups` with `isFab=true`, the page header (`PageHeader`) and dialog actions (`DialogActions`) SHALL exclude those groups from their inline button rendering. Only non-FAB button groups SHALL appear in the header/action bar. FAB groups SHALL be rendered separately as floating overlays by the `fab-rendering` capability.

#### Scenario: Page with mixed FAB and non-FAB groups

- **WHEN** a page's `PageContainer` has one FAB group and one non-FAB group
- **THEN** the `PageHeader` SHALL render only the non-FAB group's buttons
- **AND** the FAB group SHALL render as a floating overlay outside the `PageHeader`

#### Scenario: Page with only FAB groups

- **WHEN** a page's `PageContainer` has only FAB button groups
- **THEN** the `PageHeader` SHALL render no action buttons
- **AND** all FAB groups SHALL render as floating overlays

#### Scenario: Dialog with FAB groups

- **WHEN** a dialog's `PageContainer` has FAB button groups
- **THEN** the `DialogActions` area SHALL exclude FAB groups
- **AND** the FAB groups SHALL render as floating overlays within the dialog content area
