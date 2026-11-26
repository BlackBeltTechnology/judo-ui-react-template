# Proposal

## Why

On an association table of an edit page, the user removes row `dark` with bulk Remove, re-adds `dark` through the Add selector, checks two other rows and clicks bulk Remove. React sends `~remove` for all three rows, including the unchecked `dark`. The runtime removes only the two checked rows. React is the behavioural reference, so it must not remove rows the user did not check.

Root cause: after a bulk action or Clear, `LazyTable` reset its selection with `handleOnSelection([])`. MUI X 8 selection models are `{ type, ids }`. `getUpdatedRowsSelected` iterated `[].ids`, threw, and left `selectedRows` holding the removed rows. Because the grid sets `keepNonExistentRowsSelected`, the stale id also stayed in the grid's selection model. The next bulk Remove passed the stale `selectedRows` to the action.

## What Changes

- `components/table/LazyTable.tsx`: both resets call `handleOnSelection(createEmptySelectionModel())`.
- `handleOnSelection` is typed `(newSelectionModel: GridRowSelectionModel) => void`, so a bare array no longer compiles.
- `EagerTable` already resets with `createEmptySelectionModel()` and does not change.

## Capabilities

### Modified Capabilities

- `data-tables`: a bulk action acts only on the currently checked rows.

## Impact

- Template: `components/table/LazyTable.tsx.hbs`. Test: `TableTemplateTest.lazyTableResetsItsSelectionWithATypedEmptyModel`.
- Every generated `LazyTable` with bulk actions or Clear.
