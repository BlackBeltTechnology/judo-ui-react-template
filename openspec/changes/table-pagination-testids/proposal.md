# Proposal

## Why

A shared Tatami pager spec has to page and change the page size on both engines. The React template marks the grid (`table::<id>::grid`) but not its footer pager, so the spec would fall back to MUI classes and aria labels that the runtime does not share. The user decided that the pager gets canonical ids on both engines; the runtime emits the same strings.

## What Changes

- New helper `buildTablePaginationTestId(tableId, part?)` in `utilities/transfer-id.ts`.
- New hook `useTablePaginationTestIds(tableTestId?)` in `components/CustomTablePagination.tsx`. It returns `TablePagination` props that emit:
  - `table::<id>::pagination` on the pager root,
  - `table::<id>::pagination::previous` and `::next` on the arrow buttons,
  - `table::<id>::pagination::page-size` on the rows-per-page Select display element (`role="combobox"`),
  - `table::<id>::pagination::page-size::option::<n>` on each rows-per-page `MenuItem`.
- The buttons and options use `slots` wrappers (`slots.actions.previousButton/nextButton`, `slots.menuItem`). The option wrapper reads the `data-value` the Select clones onto each child. Using `slots` instead of `slotProps.actions` keeps the existing `backIconButtonProps` / `nextIconButtonProps` (disabled state) in effect.
- `EagerTable` passes the props through the DataGrid `slotProps.basePagination.material`. `LazyTable` passes `tableTestId` to `CustomTablePagination`, whose new prop is optional; no ids are emitted without it.
- No visual or behavioural change.

## Capabilities

### Modified Capabilities

- `data-tables`: the table pager carries canonical test ids.

## Impact

- Templates: `CustomTablePagination.tsx.hbs`, `table/EagerTable.tsx.hbs`, `table/LazyTable.tsx.hbs`, `utilities/transfer-id.ts.hbs`. Test: `TableTemplateTest`.
- Itest snapshots: `EagerTable.tsx` and `LazyTable.tsx` of `ActionGroupTestPro`.
