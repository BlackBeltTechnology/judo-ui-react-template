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

- [ ] 0.1 On `feature/JNG-6391_Test_Data-TestId`, dump the union set of files that carry `data-testid` today: `grep -rln data-testid judo-ui-react/src/main/resources/actor/ | sort > /tmp/testid-coverage.txt`. This file is the **coverage baseline**. After the sweep on the new branch, the same grep SHALL produce a set that is a superset (never smaller) of the baseline.
- [ ] 0.2 On the same branch, dump every emission line: `grep -rn data-testid judo-ui-react/src/main/resources/actor/ > /tmp/testid-emissions.txt`. Used by task 5.6 for line-by-line audit.
- [ ] 0.3 Create fresh branch `feature/JNG-6391_unified_data_id_and_testid` from `origin/develop`.

## 1. Shared identity utility

- [ ] 1.1 Create `judo-ui-react/src/main/resources/actor/src/utilities/transfer-id.ts.hbs` exporting the seven functions specified in `proposal.md` §(a). Import `draftIdentifierPrefix` from `~/services/data-api/common/utils` for the compatibility rung inside `isNewRow`.
- [ ] 1.2 Register the template in `judo-ui-react/src/main/resources/ui-react.yaml` — path expression `'src/utilities/transfer-id.ts'`, no factory expression required (single per-actor file).
- [ ] 1.3 Verify by running `mvn -pl judo-ui-react-itest/ActionGroupTest/action_group_test__god -am -DskipPrepareNodeJS package` and reading `target/frontend-react/src/utilities/transfer-id.ts`.

## 2. New-row seeding

- [ ] 2.1 `judo-ui-react/src/main/resources/actor/src/pages/actions/OpenCreateFormAction.fragment.hbs:34,48` — replace the `__identifier: \`${draftIdentifierPrefix}${uuidv4()}\`` field with `__tempId: newTempId(), __isNew: true`. Remove any now-orphaned import of `uuidv4` if this fragment was its only consumer (verify by re-grep after edit).
- [ ] 2.2 `judo-ui-react/src/main/resources/actor/src/components/table/LazyTable.tsx.hbs:759` — same replacement inside the table's inline row-add closure. Update the surrounding row-shape spread.
- [ ] 2.3 `judo-ui-react/src/main/resources/actor/src/components/table/EagerTable.tsx.hbs:596` — same replacement.

## 3. "Is this row new?" branches

- [ ] 3.1 `judo-ui-react/src/main/resources/actor/src/dialogs/index.tsx.hbs:292` — replace `rowData.__identifier!.startsWith(draftIdentifierPrefix)` with `isNewRow(rowData)`. Remove the file-level `import { draftIdentifierPrefix } from '~/services/data-api/common/utils';` if unused after the edit; add `import { isNewRow } from '~/utilities/transfer-id';`.
- [ ] 3.2 `judo-ui-react/src/main/resources/actor/src/pages/index.tsx.hbs:280` — same treatment.
- [ ] 3.3 `judo-ui-react/src/main/resources/actor/src/components/table/LazyTable.tsx.hbs:293` — same treatment.
- [ ] 3.4 `judo-ui-react/src/main/resources/actor/src/components/table/EagerTable.tsx.hbs:261` — same treatment.

## 4. Row-key + non-null-assertion cleanup

- [ ] 4.1 `judo-ui-react/src/main/resources/actor/src/components/table/EagerTable.tsx.hbs:309` — replace `const getRowIdentifier: (row: T) => string = (row) => row.__identifier!;` with `import { resolveTransferId } from '~/utilities/transfer-id';` and use `resolveTransferId` directly at the DataGrid `getRowId` prop (line 529) and inside `arrayToSelectionModel` (line 391).
- [ ] 4.2 `judo-ui-react/src/main/resources/actor/src/components/table/LazyTable.tsx.hbs:340` — same treatment (call sites at 511, 685).
- [ ] 4.3 `judo-ui-react/src/main/resources/actor/src/fragments/relation/column.fragment.hbs:159` — replace `identifierAttribute={'__identifier'}` with `getRowId={(r) => resolveTransferId(r)}`. Import `resolveTransferId`.
- [ ] 4.4 `judo-ui-react/src/main/resources/actor/src/containers/components/table/index.tsx.hbs:159,172,176` — the `rowValidation` map keys go through `resolveTransferId(rowData)` instead of `rowData.__identifier!`.
- [ ] 4.5 `judo-ui-react/src/main/resources/actor/src/components/table/LazyTable.tsx.hbs:268,280,292,293,295,299` and `EagerTable.tsx.hbs:236,248,260,261,263,267` — the `rowModesModel` key expressions and inline `filter`/`onRowEditCanceled` closures use `resolveTransferId(rowData)`. **Do not** change GridRowModesModel-Value type semantics.
- [ ] 4.6 `judo-ui-react/src/main/resources/actor/src/components/table/table-row-actions.tsx.hbs:29` — `gridRowModesModel[resolveTransferId(row)]`.
- [ ] 4.7 `judo-ui-react/src/main/resources/actor/src/hooks/useCRUDDialog.tsx.hbs:53` — `id: resolveTransferId(item)`.
- [ ] 4.8 `judo-ui-react/src/main/resources/actor/src/components/ModeledTabs.tsx.hbs` — the four `__identifier` usages here are field-access on model nodes, not row-keys; audit before rewriting and skip if not row-shaped.
- [ ] 4.9 `judo-ui-react/src/main/resources/actor/src/utilities/table.ts.hbs:83,122,128` — replace the equality comparisons `i.__identifier === row.__identifier` with `resolveTransferId(i) === resolveTransferId(row)`.
- [ ] 4.10 Remaining action fragments (`RemoveAction`, `RowDeleteAction`, `BulkDeleteAction`, `BulkRemoveAction`, `OpenPageAction`, `InputSelectorCallOperationAction`, `ParameterlessCallOperationAction`, `InputFormCallOperationAction`) — audit each `__identifier` usage. If it is a row-key comparison, route through `resolveTransferId`. If it is a payload field being forwarded to a service (e.g. the `_identifier: result.__identifier` fragments), **leave unchanged** — that is a Phase-1 concern.

## 5. Hierarchical `data-testid` sweep

- [ ] 5.1 Add four testid helpers to `transfer-id.ts.hbs` per proposal §(a). Wire them into an ambient re-export shim if the generator's Java helpers need to compose them from XMI ids at build time (open question — investigate `UiGeneralHelper` before writing).
- [ ] 5.2 Sweep the ~55 templates listed on `feature/JNG-6391_Test_Data-TestId`. Order: (i) layout (`Drawer/*`, `BottomMenu/*`, `CustomBreadcrumb`, `PageHeader`, `ApplicationSelector`); (ii) widgets (`widgets/*`); (iii) tables (`table/*`, `containers/components/table/*`); (iv) container fragments (`containers/widget-fragments/*`); (v) dialogs; (vi) actions and hooks.
- [ ] 5.3 Resolve the nested-wrapper collision in `SingleRelationInput.tsx.hbs`, `Tags.tsx.hbs`, `TextWithTypeAhead.tsx.hbs`: outer container gets `buildFieldTestId(xmiId)`, autocomplete node `buildFieldTestId(xmiId, 'autocomplete')`, input node `buildFieldTestId(xmiId, 'input')`, action buttons `buildFieldTestId(xmiId, 'button::set')` etc.
- [ ] 5.4 Row-level testids: replace `buildRowTestId(a.id, params.row.__identifier)` with `buildRowTestId(tableId, params.row)` where the helper internally calls `resolveTransferId`.
- [ ] 5.5 Retire every ad-hoc suffix (`-dropdown-toggle`, `-clear-all`, `-open-selector`, `-inline-create`, `filter-operator-…`). Map to the role vocabulary in `specs/transfer-identity/spec.md`.
- [ ] 5.6 Audit gate: after the sweep, run `grep -rn data-testid judo-ui-react/src/main/resources/actor/ > /tmp/testid-emissions.after.txt`; diff against the baseline from 0.2; every removed emission SHALL be replaced by a helper call at the same site (verify by line count parity within ±5).

## 6. Verification gates

- [ ] 6.1 `grep -rn '__identifier!' judo-ui-react/src/main/resources/actor/` returns zero lines.
- [ ] 6.2 `grep -rn 'draftIdentifierPrefix' judo-ui-react/src/main/resources/actor/` returns zero lines (the export in Phase-1 remains; only this repo's imports are gone). The single remaining import inside `transfer-id.ts.hbs` for the `isNewRow` compat rung is exempt.
- [ ] 6.3 `grep -rn 'identifierAttribute' judo-ui-react/src/main/resources/actor/` returns zero lines.
- [ ] 6.4 `grep -rEn 'data-testid=[^{]*\$\{[^}]*\}-' judo-ui-react/src/main/resources/actor/` returns zero lines (i.e., no ad-hoc `${var}-suffix` emissions remain).
- [ ] 6.5 `openspec validate unify-data-id-and-testid-with-runtime --strict` reports the change as valid.

## 7. Integration build + snapshot refresh

- [ ] 7.1 `mvn clean install -DskipPrepareNodeJS` from the repo root. Capture the list of files reported by `judo-diff-checker-maven-plugin:checkDiffs` — this is the **snapshot delta**.
- [ ] 7.2 For each drift, copy the regenerated file from `judo-ui-react-itest/<module>/target/frontend-react/<path>` to `judo-ui-react-itest/<module>/src/test/resources/snapshots/frontend-react/<path>`. Same procedure as `AGENTS.md` §5. Commit as one snapshot-refresh commit per itest to keep the review diff navigable.
- [ ] 7.3 Re-run `mvn clean install -DskipPrepareNodeJS`; `checkDiffs` SHALL report zero drifts.

## 8. Cross-engine parity spot-check

- [ ] 8.1 If a runtime build for the same actor is available: open `ActionGroupTest__god` under both engines, use browser devtools to compare a representative selection: outer wrapper, input, autocomplete, dropdown, button on `SingleRelationInput`; row and cell on `EagerTable`. Every `data-testid` value SHALL be byte-identical.
- [ ] 8.2 If runtime build is not locally available: annotate the PR body with the exact selectors and expected values; defer verification to a follow-up integration ticket (mentioned in DoD).

## 9. Commit + PR

- [ ] 9.1 Commit series (suggested): `1-utility`, `2-new-row-seed`, `3-is-new-check`, `4-row-key-cleanup`, `5-testid-sweep`, `6-snapshot-refresh-<itest>` (x6). Each commit prefixed `JNG-6391`.
- [ ] 9.2 Open PR against `develop`. PR body lists the F1–F5 findings, cites the PDF, and enumerates the Phase-1 follow-ups explicitly as out of scope.
