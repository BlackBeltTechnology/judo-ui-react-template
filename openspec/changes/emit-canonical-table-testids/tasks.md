## 0. Branch baseline and coverage guard

- [x] 0.1 Fast-forward the working branch onto `origin/feature/JNG-6391_unified_data_id_and_testid` (strict descendant of `develop`: 0 behind / 27 ahead). Resolve the single `ui-react.yaml` conflict by keeping both `templates:` entries (`storage-namespace.ts` from the working tree, `transfer-id.ts` from the branch). Confirm the other six working-tree files apply cleanly. (design: Migration Plan 1)
- [x] 0.2 Capture the pre-change `data-testid` coverage baseline: `grep -rc 'data-testid' judo-ui-react/src/main/resources/actor/ | sort > /tmp/testid-counts.before.txt`. Task 6.2 asserts no file's count decreases. (design D6)
- [x] 0.3 Record the current `tableTestId` emission form: `grep -rn "tableTestId={" judo-ui-react/src/main/resources/actor/` — expect `'{{ getXMIID table }}'` in the table container. This is the value D2 changes. (design D2)

## 1. Browser-state isolation (dual-engine precondition)

- [x] 1.1 Add a failing unit test asserting `deriveStorageNamespace` maps `/RelationTest/Actor/` → `RelationTest.Actor`, `/RelationTest_runtime/Actor/` → `RelationTest_runtime.Actor`, `/` → `root`, and collapses redundant slashes. (browser-state-isolation: Namespace derivation is total)
- [x] 1.2 Add a failing test asserting `getStorageNamespace()` returns `root` when `document` is undefined and never throws on a malformed base URI. (browser-state-isolation: Non-browser context falls back safely)
- [x] 1.3 Register `actor/src/utilities/storage-namespace.ts` in `ui-react.yaml` and implement the module so 1.1–1.2 pass. Verify byte-level scheme parity with `@judo/core`'s `packages/core/src/utils/storage-namespace.ts` (`judo:` prefix, `root` fallback, dot-joined segments). (browser-state-isolation: Persisted browser state is namespaced)
- [x] 1.4 Route `axiosInterceptor.storageKey()` through `namespacedStorageKey` and confirm the OIDC entry is path-scoped. This is the collision that makes two engines on one origin impossible; it must be verifiable in isolation. (browser-state-isolation: Authentication sessions do not collide)
- [x] 1.5 Route `useDataStore` and `useLocalStorage` through `namespacedStorageKey`, including the cross-tab storage-event key comparison. (browser-state-isolation: Storage-change listeners observe the namespaced key)
- [x] 1.6 Replace the blanket `sessionStorage.clear()` in `RootErrorBoundary` with `clearNamespacedStorage`, and namespace the `judo.experiment` read in `Customization`. (browser-state-isolation: Scoped clear leaves sibling application state intact)
- [x] 1.7 Export the module from `utilities/index.tsx.hbs`; keep the five call sites on direct path imports so the early auth-bootstrap path stays barrel-free. (design D5)

> Note (1.3): `judo-frontend-runtime` / `@judo/core` is not checked out in this workspace, so byte-level
> parity was verified against the scheme recorded in `design.md` (`judo:` prefix, `root` fallback,
> dot-joined segments) rather than against the runtime source directly. The paired
> `react-template-e2e-parity` change executes the cross-engine check.

## 2. Test-ID helpers and role mapping

- [x] 2.1 Add failing tests for `buildRowActionTestId(tableId, row, role, index?)` producing `table::<id>::row::<transfer>::action::<role>`, and for the reserved `overflow` role. (transfer-identity: Row-action buttons use the canonical row-scoped action grammar)
- [x] 2.2 Add failing tests for `buildFilterPanelTestId` and its `::column`/`::operator`/`::value` variants. (transfer-identity: Canonical filter-panel test IDs)
- [x] 2.3 Add failing tests for `buildSelectionCellTestId` delegating to `buildCellTestId(…, '__check__')`, mirroring the runtime's derivation rather than duplicating the string. (transfer-identity: Canonical selection-cell test ID)
- [x] 2.4 Implement 2.1–2.3 in `utilities/transfer-id.ts.hbs`. No new `::` joining logic — compose from the existing helpers. (design D1)
- [x] 2.5 Add a failing `UiActionsHelperTest` case for a role-mapping helper reproducing the runtime's `getButtonRole` switch exactly: `opensetselector→set`, `opencreateform→create`, `openpage`/`rowopenpage→view`, `rowdelete→delete`, default → raw action type. Then implement it beside the existing `getButtonActionType`. (design D4)
- [x] 2.6 Resolve OQ1 before proceeding: check a live runtime table DOM for any inline-edit control test ID. If none exists, adopt reserved `edit`/`save`/`cancel` roles and record in `design.md` that these three are template-only until the runtime adopts them. (design OQ1)

## 3. Table identity correction (D2)

- [x] 3.1 Add a failing generator assertion that a generated table's `tableTestId` prop equals its container `div`'s `buildTableTestId(...)` argument — i.e. both derive from `getElementId`. This currently fails: the prop uses `getXMIID`, the div uses `getElementId`. (transfer-identity: Canonical grid row and cell test IDs)
- [x] 3.2 Switch `tableTestId={ '{{ getXMIID table }}' }` to `{{ getElementId table }}` in the table container template (both emission sites). (design D2)
- [x] 3.3 Verify no generated `::row::` segment carries an `Actor/(esm/…)` prefix for a table element that has a `sourceId`: `grep -rn '::row::' <generated output>` — expect only short-form table IDs. (design D2 risk row)

## 4. Row-action grammar migration (D4)

- [x] 4.1 Add a failing assertion that a generated row-delete action emits `…::row::<transfer>::action::delete` and that no `…::row::<transfer>::button::` form remains. (transfer-identity: Row action is addressed by canonical role)
- [x] 4.2 Migrate `components/table/table-row-actions.tsx.hbs:80` from `::button::${a.testId ?? a.id}` to `buildRowActionTestId` with the resolved role; emit `::action::overflow` for the dropdown trigger. (transfer-identity: Overflow trigger uses the reserved role)
- [x] 4.3 Implement the no-role fallback to the standalone `button::<id>::<actionType>` grammar, matching `RowActionCell.tsx:162`. Reject empty/`unknown` role segments. (transfer-identity: Role-less button keeps the standalone grammar)
- [x] 4.4 Update the `TableRowAction.testId` field documentation to state that it now carries a canonical action role, and note the breaking change for downstream overrides. (proposal: Impact)

## 5. DataGrid slot registration

- [x] 5.1 Add failing assertions that both `EagerTable.tsx.hbs` and `LazyTable.tsx.hbs` register `row`, `cell` and `filterPanel` slots. (data-tables: DataGrid slot registration for canonical test IDs)
- [x] 5.2 Implement the memoized `TestIdRow` wrapper forwarding all `GridRowProps` and adding `buildRowTestId(tableId, props.row, props.index)`, mirroring `TableRenderer.tsx:793`. (transfer-identity: Saved row and cell are uniquely addressable)
- [x] 5.3 Implement the memoized `TestIdCell` wrapper adding `buildCellTestId(tableId, props.row, props.column.field, <data index>)`, mirroring `TableRenderer.tsx:803` including its index source. Confirm the selection column's `__check__` field name flows through unchanged so the selection cell needs no special case. (transfer-identity: Canonical selection-cell test ID; design D3)
- [x] 5.4 Implement the memoized `TestIdFilterPanel` wrapper stamping the panel div and passing `columnInputProps`/`operatorInputProps`/`valueInputProps` through `filterFormProps`, preserving the existing `logicOperators: [GridLogicOperator.And]` restriction. (data-tables: Filter-panel slot stamps inputs through filter-form properties)
- [x] 5.5 Register all three slots in both components without disturbing the existing conditional `toolbar` slot and `panel` anchoring. (data-tables: Slot registration applies to both table variants)
- [ ] 5.6 Verify behavioral neutrality: sorting, filtering, pagination, inline editing, row selection and virtualization unchanged in the generated apps. (data-tables: Row and cell slots stamp IDs without changing behavior)
- [x] 5.7 Confirm both MUI license tiers still generate and build, since the Pro path adds pinned action columns. (data-tables: Pro and community license tiers both emit canonical IDs)

## 6. Integration build and snapshot refresh

- [x] 6.1 `mvn clean install -DskipPrepareNodeJS --fail-at-end`; catalogue which of the six itests drift. Expect all six (every table container changes). (proposal: Impact)
- [x] 6.2 Refresh snapshots with `-DforceSnapshotOverwrite=true`, then run the D6 audit: `grep -rc 'data-testid' … > /tmp/testid-counts.after.txt` and diff against the baseline. Row/cell/filter emission is additive and row actions are 1:1, so **no file's count may decrease**; a decrease blocks the commit. (design D6)
- [ ] 6.3 Spot-check refreshed snapshots for a table page: assert presence of `::row::`, `::cell::`, `::cell::__check__` (where checkbox selection is on), `::filter-panel::value`, and `::action::delete`; assert absence of any `::row::…::button::` form. (transfer-identity: all)
- [x] 6.4 `mvn clean install -DskipPrepareNodeJS` with no force flag — `checkDiffs` reports zero drift and all six Vite builds pass.

## 7. Validation and handoff

- [x] 7.1 `openspec validate emit-canonical-table-testids --strict --no-interactive`; repair any failures.
- [x] 7.2 Install this template locally as `1.0.0-SNAPSHOT` (`./mvnw install`) so the paired tatami change can consume it. (proposal: Impact)
- [x] 7.3 Record in `docs/DATA_IDENTITY.md` the canonical row/cell/selection/filter grammar, the row-action role vocabulary, the browser-state namespacing convention, and the two documented cross-engine divergences (OQ1 inline-edit controls, OQ2 temp-ID formats). (design OQ1, OQ2)
- [ ] 7.4 Hand off to `judo-tatami-tests@react-template-e2e-parity` for cross-engine execution. Cross-engine equality scenarios in this change's specs are *asserted* here and *executed* there; neither change is accepted alone. (proposal: Impact)
