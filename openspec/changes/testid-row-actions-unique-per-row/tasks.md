## Definition of Done

- `mvn clean install -DskipPrepareNodeJS=true` exits green across all itests (verified for `ActionGroupTestPro`; full reactor build in progress).
- Generated `table-row-actions.tsx` exports `columnsActionCalculator` with the new optional `isSelectorMount: boolean = false` final parameter and the new `buildRowTestId` helper.
- Generated `LazyTable.tsx` passes `containerIsSelector` to `columnsActionCalculator`, conditionalizes `baseCheckbox.testIdPrefix` on `containerIsSelector`, and conditionalizes the toolbar Button `data-testid` on `containerIsSelector`.
- Generated `EagerTable.tsx` does the symmetric thing using `isSelectorTable`.
- Generated `DropdownButton.tsx` accepts optional `testId` and `suppressTestId` props (back-compat preserved: only-`id` callers behave unchanged).
- Generated `SelectCheckbox.tsx` omits `data-testid` entirely when `testIdPrefix` is undefined / null.
- Snapshots regenerated for `ActionGroupTestPro/action_group_test_pro__god/.../{Lazy,Eager}Table.tsx.snapshot` only (no other itest carries committed snapshots for the affected files).
- Spot-check: in a fixture grid with 3 rows, the Edit/Save/Cancel buttons emit 3 distinct `data-testid="<tableXmiId>-row-edit-<rowId>"` values (one per row), not 3 identical copies.
- Spot-check: in a fixture grid mounted inside an AddSelector dialog overlay, all checkbox / toolbar / row-action `data-testid` attributes are absent from the dialog mount; the underlying page mount retains them.
- Commit message: `JNG-6391 row-unique data-testid on per-row actions + strip testids in selector mount (F11 broad + F10 widening)`.

## 1. Update DropdownButton with explicit testid props

- [x] 1.1 Add `testId?: string` and `suppressTestId?: boolean` to `DropdownMenuItem` interface in `actor/src/components/DropdownButton.tsx.hbs`.
- [x] 1.2 Add the same two optional props to `DropdownButtonProps`.
- [x] 1.3 Destructure `testId` and `suppressTestId = false` in the function signature.
- [x] 1.4 Compute `triggerTestId = suppressTestId ? undefined : (testId ?? id)` and emit `data-testid={triggerTestId}` on the trigger Button.
- [x] 1.5 In the MenuItem render, emit `data-testid={menuItem.suppressTestId ? undefined : (menuItem.testId ?? menuItem.id)}`.
- [x] 1.6 Verify all existing callers (which pass only `id`) still produce identical output via snapshot diff.

## 2. Update SelectCheckbox for selector-mount suppression

- [x] 2.1 In `actor/src/components/table/SelectCheckbox.tsx.hbs`, add an early-return branch: if `testIdPrefix` is `undefined` or `null`, return `<Checkbox>` without `data-testid`.
- [x] 2.2 Keep the existing branches for `isRowCell` / `__identifier` / header `select-all` unchanged.
- [x] 2.3 Update the component's preamble comment to reference F10 widening alongside F3.

## 3. Update table-row-actions for per-row composition + selector strip

- [x] 3.1 Add optional last parameter `isSelectorMount: boolean = false` to `columnsActionCalculator` in `actor/src/components/table/table-row-actions.tsx.hbs`.
- [x] 3.2 Add a `buildRowTestId(actionId, rowIdentifier)` helper that returns `\`${actionId}-${rowIdentifier}\`` (or bare `actionId` if no rowIdentifier).
- [x] 3.3 Change the split-action Button's `data-testid={a.id}` to `data-testid={isSelectorMount ? undefined : buildRowTestId(a.id, params.row.__identifier)}`.
- [x] 3.4 Change the DropdownButton call to keep `id={id}` (for React keying) and additionally pass `testId={buildRowTestId(id, params.row.__identifier)}` and `suppressTestId={isSelectorMount}`.
- [x] 3.5 Change each menu item factory in `menuItems={dropdownActions...}` to keep `id: action.id` (keying) and add `testId: buildRowTestId(action.id, params.row.__identifier)` and `suppressTestId: isSelectorMount`.

## 4. Update LazyTable to thread containerIsSelector

- [x] 4.1 In `actor/src/components/table/LazyTable.tsx.hbs`, append `containerIsSelector` as the new final argument to the `columnsActionCalculator(...)` call at line ~309.
- [x] 4.2 Change `baseCheckbox: { testIdPrefix: uniqueId }` to `baseCheckbox: { testIdPrefix: containerIsSelector ? undefined : uniqueId }`.
- [x] 4.3 Change toolbar Button `data-testid={toolBarAction.id}` to `data-testid={containerIsSelector ? undefined : toolBarAction.id}`.

## 5. Update EagerTable symmetrically using isSelectorTable

- [x] 5.1 Append `isSelectorTable` as the new final argument to the `columnsActionCalculator(...)` call at line ~278.
- [x] 5.2 Change `baseCheckbox: { testIdPrefix: uniqueId }` to `baseCheckbox: { testIdPrefix: isSelectorTable ? undefined : uniqueId }`.
- [x] 5.3 Change toolbar Button `data-testid={toolBarAction.id}` to `data-testid={isSelectorTable ? undefined : toolBarAction.id}`.

## 6. Update ColumnActionsProvider type

- [x] 6.1 In `actor/src/utilities/interfaces.ts.hbs`, append optional `isSelectorMount?: boolean` parameter to `ColumnActionsProvider<R, RStored>` with an inline comment explaining the semantics.

## 7. Regenerate snapshots and verify

- [x] 7.1 Run `mvn -pl judo-ui-react-itest/ActionGroupTestPro/action_group_test_pro__god -am clean install -DskipPrepareNodeJS=true -DforceSnapshotOverwrite=true` to update the two committed snapshots.
- [x] 7.2 `git diff judo-ui-react-itest/.../snapshots/` shows ONLY the expected three-region changes in LazyTable.tsx.snapshot and EagerTable.tsx.snapshot, no surprise edits elsewhere.
- [x] 7.3 Re-run `mvn -pl judo-ui-react-itest/ActionGroupTestPro/action_group_test_pro__god -am clean install -DskipPrepareNodeJS=true` (without `-DforceSnapshotOverwrite`) and confirm exit 0 (snapshots match).
- [x] 7.4 Re-run with frontend build enabled (no `-DskipExecuteFrontendBuild`) and confirm Biome formatting + Vite TypeScript compile pass cleanly.
- [x] 7.5 Full reactor build (`mvn clean install -DskipPrepareNodeJS=true`) green across all 6 itests — BUILD SUCCESS in 3:31, all 14 sub-modules green (ActionGroupTest/Pro × God, CRUDActionsTest × Actor/CollectionDashboard/SingleDashboard, OperationParametersTest × Actor, RelationTest × Actor, SimpleOrderManagement × Registration/Customer).

## 8. Coordinate downstream catalogue update

- [ ] 8.1 Open companion change in `judo-ui-e2e-template` to reshape `editActions.{edit,save,cancel}` from scalar `id:` to `idPrefix:`, matching the existing `selectActions.selectRow.idPrefix` precedent. Reference this proposal.
- [ ] 8.2 Confirm no Playwright spec migration is required (tatami-tests survey: zero references to `editActions.*.id` outside the auto-generated `VisualElementIds.ts`).
