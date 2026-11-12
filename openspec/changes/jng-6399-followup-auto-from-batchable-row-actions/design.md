# Design

## Problem reframed

JNG-6399 shipped `AUTO = tableHasAnyBulkAction(table)` — a single predicate that checks the toolbar's `tableActionButtonGroup` for BulkX buttons. This misses a common case: tables whose only actionable surface is per-row, but where those row actions are semantically batchable (no per-row input needed).

The modeler picks AUTO expecting "smart" behavior, but gets `false` on a table with `RowDelete` row actions and no BulkX. The modeler then has to either (a) add an explicit `BulkDelete` button (duplicating intent) or (b) switch to `ENABLED` (losing the smart inference).

## Decision 1: STRICT veto on input-needing row actions

The AUTO predicate considers row actions from `rowActionButtonGroup`. Row actions fall into three categories:

| Category | Closed list | Rationale |
|---|---|---|
| **Batchable** | `RowDeleteActionDefinition`, `ParameterlessCallOperationActionDefinition` | No per-row input needed; semantically N-row-safe. |
| **Input-needing (veto)** | `InputFormCallOperationActionDefinition`, `InputSelectorCallOperationActionDefinition` | Opens a modal per row; cannot batch. |
| **Neutral** | `OpenPageActionDefinition` (OpenForm, OpenView) | Single-row navigation; neither enables nor vetoes. |

The STRICT rule: AUTO=true when the table has ≥1 batchable row action AND zero input-needing row actions. A single input-needing action vetoes AUTO, because the user would see checkboxes + multi-select but one of the visible actions can only operate on one row at a time — misleading UX.

All three `getIs*()` methods on `ActionDefinition` are derived from EClass identity (`this instanceof …`), so the category check is deterministic and null-safe.

## Decision 2: Implicit toolbar bulk entries (Path A)

When AUTO=true is granted only by the row-action path (no explicit BulkX), the generator emits one implicit toolbar entry per batchable row action.

### Runtime contract mismatch and the `bulkFromRowAction` flag

The existing `isBulk` toolbar path calls `actions[name](selectedRows.current)` — i.e. passes the whole selected-rows array and expects `{ result }` back. That's the contract for **explicit BulkX action functions** (e.g. `bulkDeleteAction(rows: T[])`).

A **row action function** (e.g. `rowDeleteAction(row: T)`) takes a single row and returns `Promise<void>`. Reusing the same `name` for an implicit bulk toolbar entry therefore mismatches the runtime contract.

The minimal-change fix: add a new flag `bulkFromRowAction?: boolean` to `ToolBarActionProps`, and one extra branch in the LazyTable/EagerTable click handler that fans out the per-row action across the selection:

```typescript
if (toolBarAction.isBulk) {
  if (toolBarAction.bulkFromRowAction) {
    for (const row of selectedRows.current) {
      await actions[toolBarAction.name]!(row);
    }
    handleOnSelection([]);
  } else {
    const { result: bulkResult } = await actions[toolBarAction.name]!(selectedRows.current);
    // … existing post-handling …
  }
}
```

### Implicit toolbar entry shape

- `name`: same as the row action's name (e.g. `rowDeleteAction`) so it pulls the existing per-row action function.
- `isBulk: true` — enables the toolbar bulk gesture.
- `bulkFromRowAction: true` — selects the new fan-out branch.
- `id`: derived from the row-action button's id with a stable suffix (`"<rowButton.id>::asBulk"`) so explicit and implicit toolbar entries can coexist without collision.
- Label + icon: same as the row action.
- `enabled:` condition: `selectionModel.ids.size > 0` (standard bulk pattern).

The row-action icon in the actions column stays single-row-only with its existing `disabled if getSelectedRows().length > 0` guard. This preserves clean separation: toolbar = bulk, actions column = single row.

### Error semantics for the fan-out

The per-row loop is sequential. If `actions.rowDeleteAction(row)` throws for the 3rd row in a 5-row selection, the loop aborts and the first 2 succeeded mutations remain. This mirrors how a similar BulkCallOperation client-side fan-out would behave today; we explicitly do not introduce transaction rollback. Confirmation dialogs are not added by this change — the row action's own per-row confirmation is bypassed in the bulk path (otherwise the user would be prompted N times).

### Deduplication

If the modeler also declared an explicit BulkX (e.g. `BulkDelete`) on a table that has `RowDelete`, the implicit entry is suppressed. Rule: **explicit BulkX wins**; implicit entries only fire when no BulkX exists in `tableActionButtonGroup`.

## New / changed Java helpers

```java
// UiTableHelper.java

/**
 * STRICT variant: true iff the table has at least one batchable row action
 * and zero input-needing row actions. Neutral actions don't affect the result.
 *
 * Batchable    = RowDeleteActionDefinition, ParameterlessCallOperationActionDefinition
 * Input-needing = InputFormCallOperationActionDefinition, InputSelectorCallOperationActionDefinition
 */
public static boolean tableHasAnyBatchableRowAction(Table table) {
    if (table == null || table.getRowActionButtonGroup() == null) {
        return false;
    }
    boolean hasBatchable = false;
    for (Button button : table.getRowActionButtonGroup().getButtons()) {
        ActionDefinition ad = button.getActionDefinition();
        if (ad == null) continue;
        // Veto check
        if (ad.getIsInputFormCallOperationAction() || ad.getIsInputSelectorCallOperationAction()) {
            return false;
        }
        // Batchable check
        if (ad.getIsRowDeleteAction() || ad.getIsParameterlessCallOperationAction()) {
            hasBatchable = true;
        }
    }
    return hasBatchable;
}
```

`checkboxSelectionForOwnPage` and `multiSelectAllowedForOwnPage` change from:

```java
if (cb == CheckboxSelection.AUTO) {
    return tableHasAnyBulkAction(table);
}
```

to:

```java
if (cb == CheckboxSelection.AUTO) {
    return tableHasAnyBulkAction(table) || tableHasAnyBatchableRowAction(table);
}
```

## Template changes

### `containers/components/table/index.tsx.hbs`

In the `toolBarActions` array builder (currently only reads `tableActionButtonGroup`), add a block that generates implicit bulk toolbar entries from batchable row actions when the table has no BulkX and AUTO was granted by the row-action path.

Template-level condition:

```handlebars
{{# if (tableHasAnyBatchableRowAction table) }}
  {{# unless (tableHasAnyBulkAction table) }}
    {{# each table.rowActionButtonGroup.buttons as |button| }}
      {{# if (isRowActionBatchable button.actionDefinition) }}
        // emit implicit toolbar entry with isBulk: true
      {{/ if }}
    {{/ each }}
  {{/ unless }}
{{/ if }}
```

A new helper `isRowActionBatchable(ActionDefinition)` returns true for `RowDelete` and `ParameterlessCallOperation` action definitions.

## Risk inventory

- **Snapshot churn**: zero on existing models (no current itest model sets `checkboxSelection=AUTO` on a table without BulkX). Only the new fixture produces a diff.
- **Client-side fan-out limitations**: the implicit toolbar entry calls the per-row REST endpoint N times. Errors are not atomic. The existing BulkX path has the same limitation for `BulkCallOperation` (also client-side fan-out in the current templates). Consistent behavior.
- **Deduplication correctness**: the "explicit BulkX wins" rule prevents double-toolbar-entries. If the modeler has both `BulkDelete` and `RowDelete`, only the `BulkDelete` renders as a bulk toolbar button.
