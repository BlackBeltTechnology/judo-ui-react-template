## 1. Java helpers (`UiTableHelper.java`)

- [x] 1.1 Add `isRowActionBatchable(ActionDefinition ad)` — returns true iff `ad.getIsRowDeleteAction() || ad.getIsParameterlessCallOperationAction()`.
- [x] 1.2 Add `tableHasAnyBatchableRowAction(Table table)` — STRICT variant: iterates `rowActionButtonGroup.buttons`, returns `false` immediately on any input-needing action, returns `true` only if ≥1 batchable found and 0 input-needing found. Null-safe on table, button group, and action definition.
- [x] 1.3 Update `checkboxSelectionForOwnPage` AUTO branch: `return tableHasAnyBulkAction(table) || tableHasAnyBatchableRowAction(table);`
- [x] 1.4 Update `multiSelectAllowedForOwnPage` (mirrors `checkboxSelectionForOwnPage` — same change).

## 2. Template — implicit toolbar bulk entries

- [x] 2.1 In `containers/components/table/index.tsx.hbs`, in the `toolBarActions` array builder: after the existing explicit toolbar entries from `tableActionButtonGroup`, add a block gated by `{{# if (tableHasAnyBatchableRowAction table) }}{{# unless (tableHasAnyBulkAction table) }}` that iterates `rowActionButtonGroup.buttons` and emits an implicit toolbar entry for each batchable row action. Each entry reuses the row action's name, label, icon, and sets `isBulk: true`, `bulkFromRowAction: true`, `enabled: selectionModel.ids.size > 0`, and an id of the form `"<rowButton.id>::asBulk"`.
- [x] 2.2 In `utilities/table.ts.hbs`, add `bulkFromRowAction?: boolean` to the `ToolBarActionProps<T>` interface.
- [x] 2.3 In `components/table/LazyTable.tsx.hbs` and `components/table/EagerTable.tsx.hbs`, inside the existing `if (toolBarAction.isBulk)` block, branch on `bulkFromRowAction`: when true, iterate `selectedRows.current` sequentially and call `actions[toolBarAction.name]!(row)` for each, then call `handleOnSelection([])` (LazyTable) / `handleOnSelection(createEmptySelectionModel())` (EagerTable).

## 3. Unit tests

- [x] 3.1 Add tests for `tableHasAnyBatchableRowAction`:
      - `RowDelete` only → true
      - `ParameterlessCallOperation` only → true
      - Both batchable → true
      - `RowDelete` + `InputFormCallOperation` → false (veto)
      - `RowDelete` + `InputSelectorCallOperation` → false (veto)
      - `InputFormCallOperation` only → false
      - `OpenPage` only → false (neutral, no batchable)
      - `RowDelete` + `OpenPage` → true (neutral doesn't veto)
      - Empty button group → false
      - Null table → false
      - Null button group → false
- [x] 3.2 Add tests for `checkboxSelectionForOwnPage` AUTO + row-action matrix:
      - AUTO + bulk-only → true (existing rule)
      - AUTO + batchable-row-only → true (new rule)
      - AUTO + batchable-row + input-needing-row → false (veto)
      - AUTO + no-bulk + no-row-actions → false

## 4. Test fixture

- [~] 4.1 **REVERTED**: The OperationParametersTest fixture edits (FlowerInfo → AUTO, BulkX removal) were tried in a local working tree and successfully demonstrated the new code path. They were then reverted to `origin/develop` to keep the itest model in sync with the live tatami-tests backend, because end-to-end frontend manual verification requires the upstream-deployed services and a fixture mismatch would break unrelated table loads (e.g. `~list` calls). The new code path is therefore covered by **unit tests only** (47 tests in `CheckboxSelectionTest`, including 20 new tests for the STRICT row-action matrix and the AUTO-from-row-action inference).
- [x] 4.2 Verified manually before the revert: `containers/FlowerInfo/Table/components/FlowerInfoTableTableComponent/index.tsx` showed `allowSelectMultiple = true`, `checkboxSelection={isSelector ? true : true}`, and the implicit bulk toolbar entry `{ name: 'rowDeleteAction', id: '...::asBulk', isBulk: true, bulkFromRowAction: true, enabled: selectionModel.ids.size > 0 }`. Runtime branch is present in `components/table/LazyTable.tsx` and `EagerTable.tsx` regardless of fixture state (template-level code, always emitted).
- [x] 4.3 No curated snapshot directory exists for OperationParametersTest; zero snapshot churn. After revert, regenerated FlowerInfo component contains no `bulkFromRowAction` / `::asBulk` entries (confirms suppression when explicit BulkX is present).

## 5. Build verification

- [x] 5.1 `mvn clean install` in `judo-ui-react`: 47 tests pass (was 27, added 20 new tests for the STRICT row-action matrix).
- [x] 5.2 `mvn clean install` in `OperationParametersTest/operation_parameters_test__actor` (with `-DskipPrepareNodeJS`): BUILD SUCCESS.
- [x] 5.3 Diff-checker: green (no curated snapshots in OperationParametersTest).
