# Tasks

## 1. Pager ids (spec: data-tables)

- [x] 1.1 Add `TableTemplateTest.bothTableVariantsMarkThePagerWithCanonicalIds`. Verify that it fails before the change (`/tmp/jng6411-react-pager-red.log`).
- [x] 1.2 Add `buildTablePaginationTestId` and `useTablePaginationTestIds`, wire them into `EagerTable` and `LazyTable`. Verify that `mvn -pl judo-ui-react test` passes.
- [x] 1.3 Refresh the itest snapshots with `-DforceSnapshotOverwrite` (`/tmp/jng6411-react-pager-snapshot.log`, rc=0, generated apps build). The rerun without it stops on the pre-existing "manual changes in generated files" check before the diff (`/tmp/jng6411-react-pager-build.log`), so zero drift is not yet shown.

## 2. Dual-engine check

- [ ] 2.1 Run a Tatami pager spec on React and on the runtime with retries off. Verify that both find the five ids.
