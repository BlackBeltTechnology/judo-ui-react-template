# Tasks

## 1. Enum filter value ids (spec: data-tables)

- [x] 1.1 Add `FilterTemplateTest.theEnumFilterValueMarksTheComboboxAndEachOptionByMember`. Verify that it fails before the change (`/tmp/jng6411-react-e26e27-red.log`).
- [x] 1.2 Add `buildFilterPanelValueOptionTestId` and wire the combobox and option ids into `FilterDialog`. Verify that `mvn -pl judo-ui-react install` passes (`/tmp/jng6411-react-e26e27-build.log`).
- [ ] 1.3 Refresh the itest snapshots with `-DforceSnapshotOverwrite`. Blocked: under 3 GB free disk.

## 2. Dual-engine check

- [ ] 2.1 Run a Tatami spec that filters on an enum column on React and on the runtime with retries off.
