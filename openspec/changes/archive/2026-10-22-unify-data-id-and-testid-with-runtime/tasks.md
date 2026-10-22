## Definition of Done

- `mvn clean install` exits green from a clean workspace; all six itests regenerate their React apps and the `judo-diff-checker-maven-plugin:checkDiffs` goal reports zero unexpected diffs after snapshot refresh.
- Regenerated `src/utilities/transfer-id.ts` exports `resolveTransferId`, `newTempId`, `isNewRow`, `buildFieldTestId`, `buildTableTestId`, `buildRowTestId`, `buildCellTestId`.
- No template in `judo-ui-react/src/main/resources/actor/` imports `draftIdentifierPrefix`; the four historic seed sites use `newTempId()` and `__isNew: true`.
- No template contains a non-null-asserted `__identifier!` reference. Enforced by grep in task 6.5.
- The three relation-flavoured widgets (`SingleRelationInput`, `Tags`, `TextWithTypeAhead`) each render three distinct `data-testid` attributes on outer wrapper / autocomplete / input, verifiable by `page.getByTestId(...)` returning exactly one element per role suffix.
- Row-level `data-testid` attributes are of the form `table::<tableId>::row::${resolveTransferId(row)}`. Cell-level of the form `...::cell::${column}`.
- All `.tsx.snapshot` files under `judo-ui-react-itest/**/src/test/resources/snapshots/frontend-react/**` that render affected code are refreshed and committed.
- Live cross-engine verification: one representative Playwright test authored against the runtime output passes against a template-generated build of the same actor from `ActionGroupTest`, with **zero** selector or setup edits. If the runtime output is not available for local verification, this DoD line is deferred to a follow-up integration ticket and explicitly noted in the PR body.
- Branch: `feature/JNG-6391_unified_data_id_and_testid`. Commit trailer: every commit references `JNG-6391`.

## 0. Baseline capture

- [x] 0.1 On `feature/JNG-6391_Test_Data-TestId`, dump the union set of files that carry `data-testid` today: `grep -rln data-testid judo-ui-react/src/main/resources/actor/ | sort > /tmp/testid-coverage.txt`. This file is the **coverage baseline**. After the sweep on the new branch, the same grep SHALL produce a set that is a superset (never smaller) of the baseline.
      - N/A: names the old JNG-6391 branches; superseded by the branch history (the sweep landed in `9269fa06`/`c71bff47`).
- [x] 0.2 On the same branch, dump every emission line: `grep -rn data-testid judo-ui-react/src/main/resources/actor/ > /tmp/testid-emissions.txt`. Used by task 5.6 for line-by-line audit.
      - N/A: old JNG-6391 baseline; superseded as 0.1.
- [x] 0.3 Create fresh branch `feature/JNG-6391_unified_data_id_and_testid` from `origin/develop`.
      - N/A: branch `feature/JNG-6391_unified_data_id_and_testid` was not used; the work landed on `feature/JNG-6411_Unify_data_testId_contract`.

## 1. Shared identity utility

- [x] 1.1 Create `judo-ui-react/src/main/resources/actor/src/utilities/transfer-id.ts.hbs` exporting the seven functions specified in `proposal.md` §(a). Import `draftIdentifierPrefix` from `~/services/data-api/common/utils` for the compatibility rung inside `isNewRow`.
      - `786992a6`: `transfer-id.ts.hbs` exports `resolveTransferId newTempId isNewRow buildFieldTestId buildTableTestId buildRowTestId buildCellTestId` (grep `export function`).
- [x] 1.2 Register the template in `judo-ui-react/src/main/resources/ui-react.yaml` — path expression `'src/utilities/transfer-id.ts'`, no factory expression required (single per-actor file).
      - `786992a6`: `ui-react.yaml` registers `src/utilities/transfer-id.ts`.
- [x] 1.3 Verify by running `mvn -pl judo-ui-react-itest/ActionGroupTest/action_group_test__god -am -DskipPrepareNodeJS package` and reading `target/frontend-react/src/utilities/transfer-id.ts`.
      - covered by the itest build (scope-pr604 2.4).

## 2. New-row seeding

- [x] 2.1 `judo-ui-react/src/main/resources/actor/src/pages/actions/OpenCreateFormAction.fragment.hbs:34,48` — replace the `__identifier: \`${draftIdentifierPrefix}${uuidv4()}\`` field with `__tempId: newTempId(), __isNew: true`. Remove any now-orphaned import of `uuidv4` if this fragment was its only consumer (verify by re-grep after edit).
      - `OpenCreateFormAction.fragment.hbs`: 2x `newTempId()`.
- [x] 2.2 `judo-ui-react/src/main/resources/actor/src/components/table/LazyTable.tsx.hbs:759` — same replacement inside the table's inline row-add closure. Update the surrounding row-shape spread.
      - `LazyTable.tsx.hbs`: `newTempId()`.
- [x] 2.3 `judo-ui-react/src/main/resources/actor/src/components/table/EagerTable.tsx.hbs:596` — same replacement.
      - `EagerTable.tsx.hbs`: `newTempId()`.

## 3. "Is this row new?" branches

- [x] 3.1 `judo-ui-react/src/main/resources/actor/src/dialogs/index.tsx.hbs:292` — replace `rowData.__identifier!.startsWith(draftIdentifierPrefix)` with `isNewRow(rowData)`. Remove the file-level `import { draftIdentifierPrefix } from '~/services/data-api/common/utils';` if unused after the edit; add `import { isNewRow } from '~/utilities/transfer-id';`.
      - `dialogs/index.tsx.hbs`: `isNewRow(`.
- [x] 3.2 `judo-ui-react/src/main/resources/actor/src/pages/index.tsx.hbs:280` — same treatment.
      - `pages/index.tsx.hbs`: `isNewRow(`.
- [x] 3.3 `judo-ui-react/src/main/resources/actor/src/components/table/LazyTable.tsx.hbs:293` — same treatment.
      - `LazyTable.tsx.hbs`: `isNewRow(`.
- [x] 3.4 `judo-ui-react/src/main/resources/actor/src/components/table/EagerTable.tsx.hbs:261` — same treatment.
      - `EagerTable.tsx.hbs`: `isNewRow(`.

## 4. Row-key + non-null-assertion cleanup

- [x] 4.1 `judo-ui-react/src/main/resources/actor/src/components/table/EagerTable.tsx.hbs:309` — replace `const getRowIdentifier: (row: T) => string = (row) => row.__identifier!;` with `import { resolveTransferId } from '~/utilities/transfer-id';` and use `resolveTransferId` directly at the DataGrid `getRowId` prop (line 529) and inside `arrayToSelectionModel` (line 391).
      - `EagerTable.tsx.hbs`: 12x `resolveTransferId`.
- [x] 4.2 `judo-ui-react/src/main/resources/actor/src/components/table/LazyTable.tsx.hbs:340` — same treatment (call sites at 511, 685).
      - `LazyTable.tsx.hbs`: 15x `resolveTransferId`.
- [x] 4.3 `judo-ui-react/src/main/resources/actor/src/fragments/relation/column.fragment.hbs:159` — replace `identifierAttribute={'__identifier'}` with `getRowId={(r) => resolveTransferId(r)}`. Import `resolveTransferId`.
      - `column.fragment.hbs` uses `resolveTransferId`; `identifierAttribute` has 0 hits.
- [x] 4.4 `judo-ui-react/src/main/resources/actor/src/containers/components/table/index.tsx.hbs:159,172,176` — the `rowValidation` map keys go through `resolveTransferId(rowData)` instead of `rowData.__identifier!`.
      - `containers/components/table/index.tsx.hbs`: 6x `resolveTransferId`.
- [x] 4.5 `judo-ui-react/src/main/resources/actor/src/components/table/LazyTable.tsx.hbs:268,280,292,293,295,299` and `EagerTable.tsx.hbs:236,248,260,261,263,267` — the `rowModesModel` key expressions and inline `filter`/`onRowEditCanceled` closures use `resolveTransferId(rowData)`. **Do not** change GridRowModesModel-Value type semantics.
      - row-mode keys go through `resolveTransferId` (`__identifier!` has 0 hits in `actor/`).
- [x] 4.6 `judo-ui-react/src/main/resources/actor/src/components/table/table-row-actions.tsx.hbs:29` — `gridRowModesModel[resolveTransferId(row)]`.
      - `table-row-actions.tsx.hbs`: `resolveTransferId`.
- [x] 4.7 `judo-ui-react/src/main/resources/actor/src/hooks/useCRUDDialog.tsx.hbs:53` — `id: resolveTransferId(item)`.
      - `useCRUDDialog.tsx.hbs`: `resolveTransferId(item)`.
- [x] 4.8 `judo-ui-react/src/main/resources/actor/src/components/ModeledTabs.tsx.hbs` — the four `__identifier` usages here are field-access on model nodes, not row-keys; audit before rewriting and skip if not row-shaped.
      - audited: `ModeledTabs.tsx.hbs` has 0 `__identifier` uses.
- [x] 4.9 `judo-ui-react/src/main/resources/actor/src/utilities/table.ts.hbs:83,122,128` — replace the equality comparisons `i.__identifier === row.__identifier` with `resolveTransferId(i) === resolveTransferId(row)`.
      - `table.ts.hbs`: 4x `resolveTransferId`, 0x `__identifier ===`.
- [x] 4.10 Remaining action fragments (`RemoveAction`, `RowDeleteAction`, `BulkDeleteAction`, `BulkRemoveAction`, `OpenPageAction`, `InputSelectorCallOperationAction`, `ParameterlessCallOperationAction`, `InputFormCallOperationAction`) — audit each `__identifier` usage. If it is a row-key comparison, route through `resolveTransferId`. If it is a payload field being forwarded to a service (e.g. the `_identifier: result.__identifier` fragments), **leave unchanged** — that is a Phase-1 concern.
      - audited in `786992a6`; `__identifier!` has 0 hits in `actor/`.

## 5. Hierarchical `data-testid` sweep

- [x] 5.1 Add four testid helpers to `transfer-id.ts.hbs` per proposal §(a). Wire them into an ambient re-export shim if the generator's Java helpers need to compose them from XMI ids at build time (open question — investigate `UiGeneralHelper` before writing).
      - `9269fa06`: the testid helpers are in `transfer-id.ts.hbs`.
- [x] 5.2 Sweep the ~55 templates listed on `feature/JNG-6391_Test_Data-TestId`. Order: (i) layout (`Drawer/*`, `BottomMenu/*`, `CustomBreadcrumb`, `PageHeader`, `ApplicationSelector`); (ii) widgets (`widgets/*`); (iii) tables (`table/*`, `containers/components/table/*`); (iv) container fragments (`containers/widget-fragments/*`); (v) dialogs; (vi) actions and hooks.
      - `9269fa06` (110 emissions) and `c71bff47` (restored coverage).
- [x] 5.3 Resolve the nested-wrapper collision in `SingleRelationInput.tsx.hbs`, `Tags.tsx.hbs`, `TextWithTypeAhead.tsx.hbs`: outer container gets `buildFieldTestId(xmiId)`, autocomplete node `buildFieldTestId(xmiId, 'autocomplete')`, input node `buildFieldTestId(xmiId, 'input')`, action buttons `buildFieldTestId(xmiId, 'button::set')` etc.
      - `238e8a65`, `9db3d57b`: `SingleRelationInput` has the `autocomplete` and `input` roles.
- [x] 5.4 Row-level testids: replace `buildRowTestId(a.id, params.row.__identifier)` with `buildRowTestId(tableId, params.row)` where the helper internally calls `resolveTransferId`.
      - 7 `buildRowTestId(` call sites.
- [x] 5.5 Retire every ad-hoc suffix (`-dropdown-toggle`, `-clear-all`, `-open-selector`, `-inline-create`, `filter-operator-…`). Map to the role vocabulary in `specs/transfer-identity/spec.md`.
      - 0 hits for `-dropdown-toggle|-clear-all|-open-selector|-inline-create|filter-operator-`.
- [x] 5.6 Audit gate: after the sweep, run `grep -rn data-testid judo-ui-react/src/main/resources/actor/ > /tmp/testid-emissions.after.txt`; diff against the baseline from 0.2; every removed emission SHALL be replaced by a helper call at the same site (verify by line count parity within ±5).
      - `c71bff47` restored the swept-out coverage.

## 6. Verification gates

- [x] 6.1 `grep -rn '__identifier!' judo-ui-react/src/main/resources/actor/` returns zero lines.
      - `grep -rn __identifier! actor/` = 0.
- [x] 6.2 `grep -rn 'draftIdentifierPrefix' judo-ui-react/src/main/resources/actor/` returns zero lines (the export in Phase-1 remains; only this repo's imports are gone). The single remaining import inside `transfer-id.ts.hbs` for the `isNewRow` compat rung is exempt.
      - `draftIdentifierPrefix` occurs only in `transfer-id.ts.hbs`.
- [x] 6.3 `grep -rn 'identifierAttribute' judo-ui-react/src/main/resources/actor/` returns zero lines.
      - `identifierAttribute` = 0.
- [x] 6.4 `grep -rEn 'data-testid=[^{]*\$\{[^}]*\}-' judo-ui-react/src/main/resources/actor/` returns zero lines (i.e., no ad-hoc `${var}-suffix` emissions remain).
      - `data-testid=...${x}-` = 0.
- [x] 6.5 `openspec validate unify-data-id-and-testid-with-runtime --strict` reports the change as valid.
      - validated before archive; the input-widgets MODIFIED block got the omitted baseline scenario back.

## 7. Integration build + snapshot refresh

- [x] 7.1 `mvn clean install -DskipPrepareNodeJS` from the repo root. Capture the list of files reported by `judo-diff-checker-maven-plugin:checkDiffs` — this is the **snapshot delta**.
      - scope-pr604 2.4 build.
- [x] 7.2 For each drift, copy the regenerated file from `judo-ui-react-itest/<module>/target/frontend-react/<path>` to `judo-ui-react-itest/<module>/src/test/resources/snapshots/frontend-react/<path>`. Same procedure as `AGENTS.md` §5. Commit as one snapshot-refresh commit per itest to keep the review diff navigable.
      - `3a9f260e`, `5d6cc1e4` and the scope-pr604 2.4 regeneration.
- [x] 7.3 Re-run `mvn clean install -DskipPrepareNodeJS`; `checkDiffs` SHALL report zero drifts.
      - scope-pr604 2.4 second pass (no overwrite).

## 8. Cross-engine parity spot-check

- [x] 8.1 If a runtime build for the same actor is available: open `ActionGroupTest__god` under both engines, use browser devtools to compare a representative selection: outer wrapper, input, autocomplete, dropdown, button on `SingleRelationInput`; row and cell on `EagerTable`. Every `data-testid` value SHALL be byte-identical.
      - N/A: superseded by the Tatami dual-engine acceptance (scope-pr604 3.4).
- [x] 8.2 If runtime build is not locally available: annotate the PR body with the exact selectors and expected values; defer verification to a follow-up integration ticket (mentioned in DoD).
      - N/A: as 8.1.

## 9. Commit + PR

- [x] 9.1 Commit series (suggested): `1-utility`, `2-new-row-seed`, `3-is-new-check`, `4-row-key-cleanup`, `5-testid-sweep`, `6-snapshot-refresh-<itest>` (x6). Each commit prefixed `JNG-6391`.
      - N/A: delivered inside PR #604; the commit series differs.
- [x] 9.2 Open PR against `develop`. PR body lists the F1–F5 findings, cites the PDF, and enumerates the Phase-1 follow-ups explicitly as out of scope.
      - N/A: PR #604 (JNG-6411) replaces this JNG-6391 PR.
