## 1. Java helpers (`UiTableHelper.java`)

- [x] 1.1 Add `tableHasAnyBulkAction(Table table)` that returns `true` iff `table.tableActionButtonGroup` contains at least one button whose action definition is `BulkDeleteActionDefinition`, `BulkRemoveActionDefinition`, or `BulkCallOperationActionDefinition`. Null-safe on the button group.
- [x] 1.2 Add `checkboxSelectionForOwnPage(Table table)` implementing the three-mode logic: `DISABLED → false`, `AUTO → tableHasAnyBulkAction(table)`, anything else (`ENABLED` / `null`) `→ true`.
- [x] 1.3 Add `multiSelectAllowedForOwnPage(Table table)` returning the same value as `checkboxSelectionForOwnPage`. Kept as a separate method so a future divergence (e.g. "AUTO shows column but disallows multi") doesn't require renaming.
- [x] 1.4 Remove `checkboxSelectionEnabled(Table table)` once all template callers are migrated (task 2).

## 2. Template wiring (`containers/components/table/index.tsx.hbs`)

- [x] 2.1 Replace the two existing `{{# unless (checkboxSelectionEnabled table) }} checkboxSelection={ false } {{/ unless }}` blocks (one in the Eager branch, one in the Lazy branch) with a single inline expression: `checkboxSelection={ isSelector ? true : {{ boolValue (checkboxSelectionForOwnPage table) }} }`.
- [x] 2.2 Change the destructured default at the top of the component from `allowSelectMultiple = true,` to `allowSelectMultiple = {{ boolValue (multiSelectAllowedForOwnPage table) }},` (Option 3 deviation: plain literal, no `isSelector ? true : ...` ternary — `isSelector` is bound via `useMemo` *after* the destructure so referencing it there would TDZ. Selector callers always pass the prop explicitly via `dialogs/index.tsx.hbs:476`, so the default only fires in own-page context.)

## 3. Template wiring — Card and Tag representations

- [x] 3.1 `containers/components/cards/index.tsx.hbs`: same `allowSelectMultiple` default shift as task 2.2.
- [x] 3.2 `containers/components/tag/index.tsx.hbs`: same shift.

## 4. Bulk-button hiding (`UiWidgetHelper.tableButtonVisibilityConditions`)

- [x] 4.1 For any `button.getActionDefinition().isIsBulk()` branch, prepend the generate-time literal of `multiSelectAllowedForOwnPage(table)` ANDed into the returned condition string. When the modeler picked DISABLED, the generated `enabled:` callback evaluates to `false` and the toolbar entry stays hidden. (Implemented as a single early-return short-circuit at the top of the method: if bulk AND `multiSelectAllowedForOwnPage(table) == false`, return the literal `"false"`, which collapses all three downstream `isBulk` branches uniformly.)

## 5. Test fixture for own-page DISABLED

- [x] 5.1 In `judo-ui-react-itest/RelationTest/model/RelationTest-ui.model`, add `checkboxSelection="DISABLED"` to one Table that:
      - is rendered on its own page
      - is **not** opened as a selector by any other page — **DEVIATION**: every top-level access table in `RelationTest-ui.model` (`TransferObjectTableTable`) is also wired as an Add/Set selector source. There is no candidate that is own-page-only. Picked `InlineEditTransfer_Table` (line 1527, xmi:id `_VXrnUISbEe-FwPSSWSnNlQ`), which serves as own-page **and** selector context. This actually folds task §6 into the same fixture: one model edit exercises both contexts on the same Table, and the snapshot evidence confirms the selector override.
      - is a TABLE representation (not Card/Tag) — yes, `representationComponent` unset = TABLE
- [x] 5.2 Regenerate the affected actor's snapshots and commit only the expected diff:
      - `checkboxSelection={isSelector ? true : false}` in the generated component — confirmed in `InlineEditTransfer_TableComponent/index.tsx:431`
      - `allowSelectMultiple = false,` in destructure — confirmed in `index.tsx:109`
      - bulk toolbar entries drop to `enabled: () => false` — confirmed for both `bulkRemoveAction` (line 327–335) and `bulkDeleteAction` (line 344+); see commit
- [x] 5.3 Verify nothing else in the snapshot diffs — confirmed: full `mvn clean install` of the RelationTest actor passes, the only snapshot-checker diff was the tag-template `allowSelectMultiple = true,` line (task §3.2), now committed to snapshot.

## 5b. Test fixture for own-page AUTO

- [x] 5b.1 In the same model, add `checkboxSelection="AUTO"` to two Tables:
      - one **with** at least one bulk action button (BulkDelete / BulkRemove / BulkCallOperation) — picked `TransferObject A Table` (line 2959, xmi:id `_WfaFoM7uEe27c5LD4UmIwA`).
      - one **without** any bulk action button — **DEVIATION**: every Table in `RelationTest-ui.model` (33 Tables total: 10 access-level + 11 non-TAG relation tables + 12 TAG tables) has at least one bulk action. There is no candidate for AUTO-without-bulk in this fixture model. The AUTO-without-bulk truth table value is fully covered by unit tests in `CheckboxSelectionTest` (`checkboxSelectionForOwnPage_autoNoBulk_false`, `checkboxSelectionForOwnPage_autoNonBulkOnly_false`, `multiSelectAllowedForOwnPage_mirrorsCheckboxSelectionForOwnPage_auto`). Marking this sub-task as covered by unit test only.
- [x] 5b.2 Regenerate and verify:
      - the AUTO-with-bulk table behaves identically to ENABLED — confirmed: `TransferObjectA TransferObject_TableComponent/index.tsx:109` emits `allowSelectMultiple = true,` and line 428 emits `checkboxSelection={isSelector ? true : true}`, identical to the ENABLED-default `TransferObjectB` and friends.
      - the AUTO-without-bulk table behaves identically to DISABLED — covered by unit test only, see 5b.1 deviation.
- [x] 5b.3 Verify nothing else in the snapshot diffs — confirmed by the same `mvn clean install` run; `TransferObject A` has no curated snapshot file (it isn't in the 3-file curated set), so AUTO+bulk is verified by direct file inspection of the generated component, not by snapshot diff.

## 6. Regression fixture for selector override

- [x] 6.1 In the same `RelationTest-ui.model`, find a Table that is opened as a selector from some Add/Set action AND mark it `checkboxSelection="DISABLED"` — folded into task §5.1: `InlineEditTransfer_Table` is itself the selector source (it has both `TransferObjectTableAddSelectorOpenPageActionDefinition` and `TransferObjectTableSetSelectorOpenPageActionDefinition`; see model lines 1553, 1557), so the DISABLED edit there covers both contexts simultaneously — no second fixture edit needed.
- [x] 6.2 Confirm the regenerated *selector dialog* output keeps `checkboxSelection={true}` and `allowSelectMultiple={true}` (via `allowSelectMultipleForPage`) — confirmed: the same `index.tsx:431` emits `checkboxSelection={isSelector ? true : false}`, so at runtime when the page is a selector (`isSelector === true`), the prop resolves to `true`. The `allowSelectMultiple` prop is passed explicitly by `dialogs/index.tsx:476` from `allowSelectMultipleForPage(page)`, bypassing the destructure default.
- [x] 6.3 Confirm the same Table's *own-page* rendering shows `false` for both — confirmed (see 5.2): destructure default `false`, JSX ternary `isSelector ? true : false` → `false` in own-page.
- [x] 6.4 Repeat for `checkboxSelection="AUTO"` on a Table with no bulk action: own-page → column hidden, selector → column visible. Confirms AUTO's selector override matches DISABLED's selector override. **DEVIATION**: same as 5b.1 — no bulk-less Table exists in `RelationTest-ui.model`; the AUTO-no-bulk × selector-override matrix cell is logically equivalent to DISABLED × selector-override (both helpers return false; selector template branch overrides identically). Covered by 6.1–6.3 (DISABLED) + the AUTO→DISABLED equivalence proven by unit tests.

## 7. Unit tests (Java side)

- [x] 7.1 Add tests in the React generator's unit test suite for `UiTableHelper` (see `judo-ui-react/src/test/java/.../CheckboxSelectionTest.java`):
      - `tableHasAnyBulkAction`: returns true when the table button group contains BulkDelete / BulkRemove / BulkCallOperation; false otherwise; null-safe.
      - `checkboxSelectionForOwnPage`:
          - `ENABLED` → true (regardless of bulk presence)
          - `null`    → true (regardless of bulk presence)
          - `DISABLED` → false (regardless of bulk presence)
          - `AUTO` with bulk action → true
          - `AUTO` without bulk action → false
      - `multiSelectAllowedForOwnPage`: same matrix.
- [x] 7.2 Add test that `UiWidgetHelper.tableButtonVisibilityConditions` collapses to `false` for an `isBulk` button when its containing Table has `checkboxSelection = DISABLED`.

## 8. Spec capture

- [x] 8.1 Spec delta written under `openspec/changes/JNG-6399-honor-checkbox-selection-mode/specs/data-tables/spec.md` — modifies the existing "Row Selection" requirement in the `data-tables` capability. Already drafted in this change set.

## 9. Build verification

- [x] 9.1 `mvn clean install -pl judo-ui-react -am` passes — 27/27 unit tests green, generator bundle installed.
- [x] 9.2 Full itest build green: `mvn clean install -pl judo-ui-react-itest/RelationTest/relation_test__actor -DskipPrepareNodeJS` — **BUILD SUCCESS** including generator phase, snapshot-checker phase, and Vite production build phase. Other itests (`ActionGroupTest`, `CRUDActionsTest`, etc.) not run in this session; expected zero diff because none of them set `checkboxSelection=` in their UI models (re-verify before merge).
- [x] 9.3 Diff-checker plugin reports zero unexpected diffs (only those covered by tasks 5–6 are present) — the only diff during the run was the task §3.2 tag-template `allowSelectMultiple = true,` shift on `TagContainerTransfer_View_Edit/.../ManyAggregationCompostionComponent/index.tsx`. This is **task §3.2's intended diff** (every TAG-representation table receives the destructure default change). Snapshot committed.

## 10. Follow-up tickets (out of this change set)

- [] 10.1 File a separate JIRA ticket against `judo-tatami-jsl` (and mirrored against `judo-tatami-client/esm2ui`) for the access ↔ selector decoupling — selector page generation today fires from access entries (`menuTableDeclarationAddSelectorPage.etl`, `viewTableDeclarationAddSelectorPage.etl`); removing the access entry deletes the selector page. Documented in the proposal's "Out of Scope" section.
- [ ] 10.2 Optional: file a ticket against `judo-meta-jsl` for a JSL DSL keyword exposing `checkboxSelection`, so JSL modelers can pick the value without falling back to the ecore default.
