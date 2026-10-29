# Tasks

## 1. Selection reset (spec: data-tables)

- [x] 1.1 Add `TableTemplateTest.lazyTableResetsItsSelectionWithATypedEmptyModel`. Verify that it fails before the change (`/tmp/jng6411-react-e26e27-red.log`).
- [x] 1.2 Reset the `LazyTable` selection with `createEmptySelectionModel()` and type `handleOnSelection`. Verify that `mvn -pl judo-ui-react install` passes (`/tmp/jng6411-react-e26e27-build.log`).
- [ ] 1.3 Refresh the itest snapshots with `-DforceSnapshotOverwrite`. Blocked: under 3 GB free disk.

## 2. Dual-engine check

- [ ] 2.1 Rerun `MatterAssociationRelationTablePage.spec.ts` "adds, removes and clears matters of the association" on React. Verify that the second bulk Remove sends `~remove` only for the two checked rows.
