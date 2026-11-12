## MODIFIED Requirements

### Requirement: Row Selection

The generator SHALL honor `Table.checkboxSelection` faithfully when the table renders on its own page (own-page context), and SHALL exempt selector-mode rendering from that value.

**Own-page context** is any rendering where the table is the page's primary content (access table page, view page, table embedded in a regular form). It is identified at runtime by `isSelector === false`.

**Selector-mode context** is any rendering where the table is opened as an Add / Set / Operation-Input selector dialog. It is identified at runtime by `isSelector === true`.

#### Own-page context rules

The three enum values have distinct, non-overlapping meanings:

`checkboxSelection = ENABLED` (and the ecore-default `null`) SHALL render the checkbox column and default `allowSelectMultiple` to `true`. No inference, no exceptions — the column appears even on tables with zero bulk operations. Bulk toolbar buttons SHALL appear when present in the model.

`checkboxSelection = DISABLED` SHALL:

- Hide the checkbox column on the generated MUI DataGrid.
- Set the default `allowSelectMultiple` prop to `false` so Shift-click / Ctrl-click do not produce multi-row selections.
- Hide any bulk toolbar buttons (`isBulk: true` toolbar entries) so the toolbar reflects that bulk actions are unreachable in this layout.

`checkboxSelection = AUTO` SHALL render the checkbox column and allow multi-select if and only if the table has at least one bulk action. A "bulk action" is a button on `tableActionButtonGroup` whose action definition is one of `BulkDeleteActionDefinition`, `BulkRemoveActionDefinition`, or `BulkCallOperationActionDefinition`. The list is closed. When no such button exists, AUTO behaves identically to `DISABLED` (no column, no multi-select). When at least one exists, AUTO behaves identically to `ENABLED`.

#### Selector-mode context rules

Selector-mode rendering SHALL ignore the source table's `checkboxSelection` value. The checkbox column SHALL always render, and `allowSelectMultiple` SHALL be derived from the page-level `allowSelectMultipleForPage` helper (which inspects whether the parent page has an Add action wired into a container button group).

Rationale: a selector dialog's purpose is to pick rows. The modeler's `DISABLED` describes the data display preference, not the picker contract. Form scenarios such as "open Galaxy as a selector to add multiple to a form" remain unaffected.

#### Card and Tag representations

For `representationComponent = CARD` and `representationComponent = TAG`, the `allowSelectMultiple` rules above apply identically (DISABLED ⇒ no multi, ENABLED/AUTO ⇒ multi). There is no checkbox column to hide in these representations; the gate is purely about whether row click contributes to the selection set.

#### Scenario: DISABLED on a table's own page

- **GIVEN** a `Table` with `checkboxSelection = DISABLED`
- **AND** the table is rendered on a page where `isSelector = false`
- **WHEN** the React app loads that page
- **THEN** the generated DataGrid has `checkboxSelection={false}`
- **AND** `allowSelectMultiple` resolves to `false`
- **AND** any bulk toolbar buttons render with `enabled: () => false` (i.e. are hidden by `tableButtonVisibilityConditions`)

#### Scenario: DISABLED on a table used as a selector

- **GIVEN** the same `Table` with `checkboxSelection = DISABLED`
- **AND** an Add action elsewhere opens this table as a selector dialog
- **WHEN** the user opens that selector dialog
- **THEN** the embedded DataGrid renders the checkbox column
- **AND** `allowSelectMultiple` is derived from `allowSelectMultipleForPage(page)` — typically `true` when the parent Add action is wired through

#### Scenario: ENABLED is the default and produces the previous behavior

- **GIVEN** a `Table` whose `checkboxSelection` attribute is not set in the model (EMF ecore default `ENABLED`)
- **THEN** the generated component behaves exactly as in versions prior to this change: checkbox column visible, multi-select on, bulk buttons visible

#### Scenario: AUTO with at least one bulk action

- **GIVEN** a `Table` with `checkboxSelection = AUTO`
- **AND** the table's `tableActionButtonGroup` contains at least one BulkDelete, BulkRemove, or BulkCallOperation button
- **THEN** the generated component behaves identically to `checkboxSelection = ENABLED` (column visible, multi-select on, bulk buttons rendered)

#### Scenario: AUTO without any bulk action

- **GIVEN** a `Table` with `checkboxSelection = AUTO`
- **AND** the table's `tableActionButtonGroup` contains no BulkDelete, BulkRemove, or BulkCallOperation button
- **THEN** the generated component behaves identically to `checkboxSelection = DISABLED` (column hidden, multi-select off)
- **AND** the selector-mode override still applies: opening this table as a selector still renders the checkbox column

**Key Helpers**: `UiTableHelper.tableHasAnyBulkAction()`, `UiTableHelper.checkboxSelectionForOwnPage()`, `UiTableHelper.multiSelectAllowedForOwnPage()`, `UiPageHelper.allowSelectMultipleForPage()`, `UiWidgetHelper.tableButtonVisibilityConditions()`

**Template positions**: `containers/components/table/index.tsx.hbs` (the `checkboxSelection={...}` inline expression and the `allowSelectMultiple` destructured default), `containers/components/cards/index.tsx.hbs`, `containers/components/tag/index.tsx.hbs`
