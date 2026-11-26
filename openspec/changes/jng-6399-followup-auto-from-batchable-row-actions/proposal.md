## Why

JNG-6399 introduced `checkboxSelection=AUTO`, which turns on the checkbox column only when the table has at least one BulkX toolbar button (`BulkDelete`, `BulkRemove`, `BulkCallOperation`). A modeler who picks AUTO on a table with only row-level actions (no BulkX) gets `AUTO → false` — even when the table has row actions that are semantically batchable (e.g. `RowDelete` or `ParameterlessCallOperation`). The modeler expected "the generator figures it out," but it doesn't.

## What Changes

### AUTO predicate extended to consider row-action eligibility

`multiSelectAllowedForOwnPage(table)` returns `true` under AUTO when **either**:

1. The table has at least one BulkX toolbar button (existing rule, unchanged), **or**
2. The table has at least one **batchable** row action AND **no** input-needing row action (STRICT veto rule).

Action categories (closed list):

| Category | Action definitions | Effect on AUTO |
|---|---|---|
| **Batchable** | `RowDeleteActionDefinition`, `ParameterlessCallOperationActionDefinition` | Enables AUTO=true |
| **Input-needing (veto)** | `InputFormCallOperationActionDefinition`, `InputSelectorCallOperationActionDefinition` | VETOES AUTO=true |
| **Neutral** | `OpenPageActionDefinition` | No effect |

A single input-needing row action vetoes AUTO, even if batchable actions also exist.

### Implicit toolbar bulk entries for batchable row actions

When AUTO=true is granted **only** by the batchable-row-action rule (no explicit BulkX toolbar button), the generator emits one implicit toolbar entry per batchable row action. Each entry reuses the existing `isBulk: true` toolbar-action path and calls the per-row REST endpoint N times from the client. The row-action icon in the actions column stays single-row-only. Toolbar = bulk, actions column = single row.

## Capabilities

### Modified Capabilities

- `data-tables`: The "Row Selection" AUTO rule is extended so the inference considers row-action eligibility, and the generated app gains implicit bulk toolbar entries when AUTO was granted by the row-action path.

## Impact

- **Java helpers**: `UiTableHelper.java` — new helper `tableHasAnyBatchableRowAction(Table)`, updated `checkboxSelectionForOwnPage` / `multiSelectAllowedForOwnPage`.
- **Templates**: `containers/components/table/index.tsx.hbs` — implicit toolbar entries for batchable row actions when no BulkX exists.
- **Unit tests**: extended `CheckboxSelectionTest` for the STRICT veto matrix.
- **itest snapshots**: zero diff for existing models (no current itest model sets `checkboxSelection=AUTO` on a table without BulkX). New fixture exercises the new code path.
