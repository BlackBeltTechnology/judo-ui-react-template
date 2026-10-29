# Tasks

## 1. Button-group dropdown ids (spec: transfer-identity)

- [x] 1.1 Add `ButtonGroupTemplateTest` asserting the trigger and item ids in `buttongroup.hbs` and `page.tsx.hbs`, the helper and its imports. Verify that it fails before the change.
- [x] 1.2 Add `buildButtonGroupTestId` to `transfer-id.ts.hbs` with a vitest case, import it in `page.tsx.hbs` and `common-imports.fragment.hbs`, and pass `testId` to the trigger and every menu item. Verify that `mvn -pl judo-ui-react test` passes.
- [x] 1.3 Refresh the itest snapshots once with `-DforceSnapshotOverwrite`, then rerun without it. Verify zero drift.

## 2. Dual-engine check

- [ ] 2.1 Run Tatami ActionGroupTest `MatterOperationsAndCollection.spec.ts` on React with retries off. Verify that it opens `button-group::<id>::dropdown` and clicks the canonical button.
