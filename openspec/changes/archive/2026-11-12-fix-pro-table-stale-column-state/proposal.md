## Why

On the MUI Pro plan, `LazyTable` and `EagerTable` save the user's column order and widths
(`${uniqueId}-columnState`) and rebuild the column list from it on the next render. That rebuild
assumes the saved layout still matches the model, but the model changes between releases:

- A saved `field` the table no longer has turns into `undefined` through
  `cols.find(...)!`. The width loop then throws on `c.field`, and the table crashes.
- A column added after the layout was saved is not in the saved state, so it is never rendered.
- `columnState.sort(...)` sorts the React state array in place.

Any user who saved a layout before a model change hits this after upgrading. Ticket: JNG-6433.
The bug was found while enabling MUI X Pro in rackinspect (`enable-mui-x-pro`).

## What Changes

- In the `{{# if isMUILicensePlanPro }}` block of the `effectiveTableColumns` `useMemo` in
  `actor/src/components/table/LazyTable.tsx.hbs` and `EagerTable.tsx.hbs`, the column-state
  restore now:
  - sorts a copy of `columnState` by `index`, not the state itself;
  - drops saved entries that match no current column;
  - applies saved widths to the remaining columns;
  - appends current columns that are missing from the saved state, in model order (this includes
    the `actions` column when it was not saved).
- Storage is unchanged: the layout stays in `sessionStorage` through the same
  `useDataStore('sessionStorage')` hook that holds filters, sort and page size.
- Community and Premium output are unchanged (the block is Pro-gated).
- `ActionGroupTestPro` snapshots of `LazyTable.tsx` and `EagerTable.tsx` are updated.
- **Pro test coverage** is added. Before this change, nothing checked the license-key wiring or the
  plan resolution:
  - `ActionGroupTestPro` now also snapshot-checks `src/main.tsx` (`@mui/x-license` import and
    `LicenseInfo.setLicenseKey`).
  - A new `ReactStoredVariableHelperTest` covers how the `muiLicensePlan` variable maps to the plan.

## Capabilities

### New Capabilities
- (none)

### Modified Capabilities
- `data-tables`: adds a requirement for the Pro saved column layout. It covers persisting the layout
  and restoring it when the model's columns have changed since it was saved.
- `integration-testing`: the Pro itest snapshot-checks the license-key wiring, and plan resolution
  is unit-tested.

## Impact

- **Templates:** `judo-ui-react/src/main/resources/actor/src/components/table/LazyTable.tsx.hbs`,
  `EagerTable.tsx.hbs` (Pro block only).
- **Snapshots:** `judo-ui-react-itest/ActionGroupTestPro/action_group_test_pro__god/src/test/resources/snapshots/frontend-react/src/components/table/{Lazy,Eager}Table.tsx.snapshot`.
- **Tests:** `judo-ui-react/src/test/java/.../ReactStoredVariableHelperTest.java`. The
  `ActionGroupTestPro/action_group_test_pro__god/pom.xml` diff-checker `<sources>` gains
  `src/main.tsx` with a new snapshot.
- **Generated apps on the Pro plan:** a regenerated app picks up the fix. Saved layouts are kept
  and are not cleared. No API, dependency or storage-key change.
- **Consumers:** rackinspect bumps `judo-ui-react-template-version` to the build carrying this fix
  (its `enable-mui-x-pro` task 4.2).
