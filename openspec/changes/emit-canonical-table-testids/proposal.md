# Emit canonical table `data-testid` attributes and namespace browser state

## Why

The JUDO platform renders the same application with two engines: this React template (static code
generation) and `judo-frontend-runtime` (model-driven runtime). The JNG-6391 goal is that **one**
Playwright suite in `judo-tatami-tests` runs unchanged against both, because both engines render the
same page — only the technology differs. Anything less than an identical selector surface means the
suite has to fork, which defeats the purpose.

Four prior JNG-6391 changes on `feature/JNG-6391_unified_data_id_and_testid` established the shared
grammar for **field-, button-, navigation-, dialog- and tab-level** elements
(`unify-data-id-and-testid-with-runtime`, `align-testids-with-runtime-package`,
`align-domain-testids-with-runtime`, `align-buttons-with-runtime`). Verified on that branch, the
following are already at parity: `field::<id>[::role]`, `button::<id>::<actionType>`,
`nav::item::<path>`, `dialog::<id>[::role]`, `tabs::<ctrl>[::<tab>]`, `breadcrumb::<index>`, and
`table::<id>`.

**Two gaps remain, and both block the tatami suite outright.**

### Gap 1 — the table interior emits no test IDs

`judo-frontend-runtime` stamps canonical IDs on every grid row, cell and filter-panel input through
MUI DataGrid slots (`packages/components/src/renderers/TableRenderer.tsx:1041`):

```
slots={{ row: TestIdRow, cell: TestIdCell, filterPanel: TestIdFilterPanel }}
```

producing `table::<id>::row::<transferId>`, `table::<id>::row::<transferId>::cell::<column>`
(hence also the selection cell `::cell::__check__`, which `getSelectionCellTestId` derives), and
`table::<id>::filter-panel[::column|operator|value]`.

This template defines the corresponding helpers in `utilities/transfer-id.ts.hbs`
(`buildRowTestId`, `buildCellTestId`) but calls them from only three places — a row-action button and
two binary-column buttons. Verified by grepping every call site on the branch:

| call site | emitted |
| --- | --- |
| `components/table/table-row-actions.tsx.hbs:80` | `…::row::<id>::button::<name>` |
| `fragments/relation/column.fragment.hbs:107` | `…::cell::<col>::button::download` |
| `fragments/relation/column.fragment.hbs:115` | `…::cell::<col>::button::view` |

No grid row, ordinary cell, selection checkbox, or filter input carries a `data-testid`. The
template's `getRowId={getRowIdentifier}` only sets MUI's internal row key; it emits no DOM
attribute. Consequently five of the tatami `TableHelper` primitives return zero matches against
this engine — `getRow`, `getCell`, `getSelectionCheckbox`, `getFilterValueInput`, and `expectEmpty`
(which counts `[role="row"][data-testid]`). Every table-driven spec fails at its first table step.

Both engines depend on **MUI x-data-grid v8** (this template `8.23.0` in
`actor/package.json.dependencies.fragment.hbs`; runtime `^8.0.0` in `packages/components`), so the
runtime's slot approach ports mechanically rather than needing a template-specific invention.

### Gap 2 — row-action grammar was left open, and the runtime has since settled it

`align-buttons-with-runtime` design D3 deliberately deferred the row-action grammar because the
runtime looked self-inconsistent. That is no longer the case. In
`packages/components/src/renderers/RowActionCell.tsx:161` the runtime uses
`getRowActionTestId(tableId, rowEntity, role, rowIndex)` — i.e.
`table::<id>::row::<transfer>::action::<role>` — and falls back to `getButtonTestId(button)` only
when no role resolves (line 162). The canonical form is therefore the row-scoped `::action::<role>`,
and this template's current `::button::<name>` form (using `a.testId ?? a.id`) does not match it.

### Gap 3 (dual-engine precondition) — browser state is shared across engines

Cross-engine acceptance serves both actors from **one origin** on distinct base paths
(`/RelationTest/Actor/` and `/RelationTest_runtime/Actor/`). `localStorage` and `sessionStorage` are
scoped to the origin, not the path, so unprefixed keys are shared. The most damaging collision is
`axiosInterceptor.storageKey()` → `oidc.user:<realm>:<clientId>`: identical realm and client on both
engines means each engine overwrites the other's session, and the two actors cannot be exercised
side by side at all. The runtime already namespaces every persisted key via
`@judo/core`'s `namespacedStorageKey` (`packages/core/src/utils/storage-namespace.ts`,
`judo:<namespace>:<key>` with the namespace derived from `document.baseURI`). This template does not,
so the convention must be ported for the two engines to coexist.

## What Changes

- **NEW** capability `transfer-identity` gains canonical table-interior emission: grid rows, cells,
  the selection cell, and filter-panel inputs, delegating construction to the existing
  `utilities/transfer-id.ts.hbs` helpers (no new grammar, no re-implementation of `::` joining).
- **MODIFIED** `data-tables`: `EagerTable.tsx.hbs` and `LazyTable.tsx.hbs` register `row`, `cell`
  and `filterPanel` DataGrid slots. The filter-panel wrapper stamps column/operator/value inputs
  through MUI `filterFormProps`, mirroring the runtime.
- **MODIFIED** `transfer-identity`: row-action buttons move from `::row::<id>::button::<name>` to the
  canonical `::row::<id>::action::<role>`, resolving D3 in the direction the runtime settled on.
  New helpers `buildRowActionTestId` and `buildFilterPanelTestId` (plus its column/operator/value
  variants) are added to `transfer-id.ts.hbs`.
- **NEW** capability `browser-state-isolation`: every persisted `localStorage`/`sessionStorage` key
  is namespaced by the base path the application is served on, matching `@judo/core`'s scheme
  byte-for-byte, so two engines on one origin keep independent session, locale and layout state.
- **BREAKING (test-surface only)**: any downstream test addressing a row-action button by
  `::button::<name>` must switch to `::action::<role>`. Persisted browser keys gain a
  `judo:<namespace>:` prefix, so existing users' stored preferences and sessions are not read on
  first load after upgrade (a one-time re-login, no data loss). No product behavior changes.

## Capabilities

### New Capabilities

- `browser-state-isolation`: base-path-derived namespacing of all persisted browser state, with a
  scoped clear that leaves a sibling frontend's state on the same origin intact.

### Modified Capabilities

- `transfer-identity`: canonical row/cell/selection-cell/filter-panel ID construction and the
  row-action grammar resolution.
- `data-tables`: DataGrid slot registration that emits the canonical IDs.

## Impact

- **Consumers of `TableRowAction`**: the `testId` field's meaning changes from a free-form button
  suffix to a canonical action role. Downstream overrides that set `testId` must be reviewed.
- **Snapshot churn**: every generated table container and page containing a table changes. All six
  itests (`ActionGroupTest`, `ActionGroupTestPro`, `CRUDActionsTest`, `OperationParametersTest`,
  `RelationTest`, `SimpleOrderManagement`) need a snapshot refresh via
  `judo-diff-checker-maven-plugin` (`-DforceSnapshotOverwrite=true`), and each refresh must be
  audited for silently *disappearing* IDs, not just added ones.
- **Branch precondition**: this change builds on the four code-complete JNG-6391 changes on
  `feature/JNG-6391_unified_data_id_and_testid`. That branch is a strict descendant of `develop`
  (0 behind / 27 ahead), so it fast-forwards; the working tree's in-flight `storage-namespace` work
  collides only in `ui-react.yaml` (both insert into the same `templates:` list at different
  offsets) and is folded into this change as Gap 3.
- **Verification depends on the paired tatami change** `react-template-e2e-parity`, which pins this
  template as `1.0.0-SNAPSHOT`, builds both engines into one Karaf, and runs the identical spec tree
  against each via `PLAYWRIGHT_BASE_URL`. Neither change can be accepted alone: this one has no E2E
  proof, and that one has nothing to prove until this lands.
- **Explicitly out of scope**: no change to `judo-frontend-runtime` (it is the reference
  implementation here), no change to `judo-ui-typescript-rest-template`, no new Playwright specs in
  this repo, and no addition of test IDs to elements that have none today beyond the four classes
  named above.

## Open Questions

- The runtime's `getCellTestId` receives `tableRows.indexOf(props.row)` as the positional fallback
  index, while its row slot receives MUI's `props.index`. Under virtualization these can differ for
  rows lacking any identity field. Both engines must agree; the pilot audit in the paired tatami
  change should confirm whether any RelationTest row actually reaches the `idx-<n>` fallback (no
  RelationTest table row is expected to, since every table element carries a `sourceId` and rows
  carry `__identifier`).
