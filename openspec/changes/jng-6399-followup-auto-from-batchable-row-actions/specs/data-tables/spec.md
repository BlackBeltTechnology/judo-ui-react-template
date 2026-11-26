## MODIFIED Requirements

### Requirement: Row Selection — AUTO mode inference

JNG-6399 defined AUTO as: "checkbox column renders iff `tableHasAnyBulkAction(table)` is true." This change extends the predicate.

#### Extended AUTO inference rule

When `checkboxSelection = AUTO`, the checkbox column SHALL render and `allowSelectMultiple` SHALL default to `true` if **either**:

1. The table has at least one BulkX toolbar button (`BulkDeleteActionDefinition`, `BulkRemoveActionDefinition`, `BulkCallOperationActionDefinition`), **or**
2. The table satisfies the **STRICT row-action batchability** rule:
   - The table's `rowActionButtonGroup` contains at least one **batchable** row action, AND
   - The table's `rowActionButtonGroup` contains **zero** input-needing row actions.

##### Row-action categories (closed list)

| Category | Action definitions | Effect |
|---|---|---|
| **Batchable** | `RowDeleteActionDefinition`, `ParameterlessCallOperationActionDefinition` | Enables AUTO=true |
| **Input-needing (veto)** | `InputFormCallOperationActionDefinition`, `InputSelectorCallOperationActionDefinition` | VETOES AUTO=true |
| **Neutral** | `OpenPageActionDefinition` | No effect on AUTO |

A single input-needing row action SHALL veto AUTO, even if batchable row actions also exist.

#### Implicit toolbar bulk entries

When AUTO=true is granted **only** by the STRICT row-action rule (i.e. the table has no BulkX toolbar button), the generator SHALL emit one implicit toolbar entry per batchable row action in `rowActionButtonGroup`. Each implicit entry:

- SHALL set `isBulk: true` and `bulkFromRowAction: true`.
- SHALL use the same name (the per-row action name, e.g. `rowDeleteAction`), label, and icon as the corresponding row action.
- SHALL use an id derived from the row-action button's id with a stable suffix (e.g. `"<rowButton.id>::asBulk"`).
- SHALL NOT be emitted when an explicit BulkX toolbar button already exists on the table. Explicit BulkX wins.

The runtime SHALL, on a toolbar bulk click where `bulkFromRowAction` is true, iterate the selected rows sequentially and invoke the per-row action for each, then clear the selection. Errors abort the loop.

The row-action icon in the actions column SHALL remain single-row-only (no change to existing `disabled` guard).

#### Scenario: AUTO on a table with RowDelete only (no BulkX)

- **GIVEN** a `Table` with `checkboxSelection = AUTO`
- **AND** the table has a `RowDelete` row action in `rowActionButtonGroup`
- **AND** the table has no BulkX toolbar button in `tableActionButtonGroup`
- **AND** the table has no input-needing row actions
- **WHEN** the React app loads that page
- **THEN** the checkbox column renders and `allowSelectMultiple` is `true`
- **AND** an implicit bulk toolbar entry for the row-delete action renders with `isBulk: true`, `bulkFromRowAction: true`, and `enabled: selectionModel.ids.size > 0`

#### Scenario: AUTO vetoed by InputFormCallOperation row action

- **GIVEN** a `Table` with `checkboxSelection = AUTO`
- **AND** the table has both a `RowDelete` row action and an `InputFormCallOperation` row action
- **AND** the table has no BulkX toolbar button
- **WHEN** the React app loads that page
- **THEN** the checkbox column does NOT render (`AUTO → false`)
- **AND** no implicit toolbar entry is generated

#### Scenario: AUTO on a table with ParameterlessCallOperation only

- **GIVEN** a `Table` with `checkboxSelection = AUTO`
- **AND** the table has a `ParameterlessCallOperation` row action
- **AND** the table has no BulkX and no input-needing row actions
- **THEN** the checkbox column renders and an implicit bulk toolbar entry is generated

#### Scenario: Explicit BulkX suppresses implicit entries

- **GIVEN** a `Table` with `checkboxSelection = AUTO`
- **AND** the table has both a `BulkDelete` toolbar button and a `RowDelete` row action
- **THEN** the checkbox column renders (existing BulkX rule)
- **AND** no implicit toolbar entry is generated for the `RowDelete` (explicit BulkX wins)
