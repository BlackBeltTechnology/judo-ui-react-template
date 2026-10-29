# Tasks

## 1. Enumeration combo input id (spec: input-widgets)

- [x] 1.1 Add `FieldTemplateTest.enumerationComboEmitsTheCanonicalNativeInputId`. Verify that it fails before the change (`/tmp/jng6411-g3e-react-combo-red.log`).
- [x] 1.2 Merge the `::input` role into `inputProps` in `enumerationcombo.hbs`. Verify that `mvn -pl judo-ui-react test` passes.
- [x] 1.3 Refresh the itest snapshots once with `-DforceSnapshotOverwrite`, then rerun without it. Verify zero drift.

## 2. Dual-engine check

- [ ] 2.1 Run Tatami OperationParametersTest on React with retries off. Verify that Florist "Decorate Garden" (both) and MenuOperations "mapped output" find their `::input`.
