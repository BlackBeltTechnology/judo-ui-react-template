## Why

Two independent code paths generate the same JUDO frontend: this repo (the React **template**, a static code generator) and the **runtime** (`judo-frontend-runtime`, a model-driven engine that renders dynamically at runtime). Both engines are supposed to be behaviourally equivalent from an end-user *and* a test-automation perspective — i.e., a Playwright test authored against one output should pass unchanged on the other.

Today they diverge on the observable identity of a data row and on the `data-testid` attributes rendered into the DOM. Five concrete diagnoses were documented in the internal 2026-07-08 audit (`dataid-template-runtime.pdf`). The audit's runtime findings are outside this repo, but four of the five reproduce in this template today, verified 2026-07-09 by direct source inspection.

### F1 — New-row identifier scheme

When the user creates a row that has not yet been saved, this template writes:

```ts
{ ...returnedData, __identifier: `${draftIdentifierPrefix}${uuidv4()}` }
```

Confirmed at:
- `judo-ui-react/src/main/resources/actor/src/pages/actions/OpenCreateFormAction.fragment.hbs:34`
- `judo-ui-react/src/main/resources/actor/src/pages/actions/OpenCreateFormAction.fragment.hbs:48`
- `judo-ui-react/src/main/resources/actor/src/components/table/LazyTable.tsx.hbs:759`
- `judo-ui-react/src/main/resources/actor/src/components/table/EagerTable.tsx.hbs:596`

The runtime instead sets two dedicated fields (`__tempId: "temp::<ts>::<rand>"`, `__isNew: true`) and leaves `__identifier` empty. Test IDs built from the row's identity therefore differ between engines.

### F2 — Row-key resolution is unconditional and unsafe

The template's row-key path is `(row) => row.__identifier!` — the non-null assertion asserts a value the row may not have. Confirmed at:

- `judo-ui-react/src/main/resources/actor/src/components/table/EagerTable.tsx.hbs:309`
- `judo-ui-react/src/main/resources/actor/src/components/table/LazyTable.tsx.hbs:340`

The runtime uses a defensive fallback chain:

```ts
row.__signedIdentifier ?? row.__identifier ?? row.__tempId ?? row.id ?? "idx-<n>"
```

Approximately twenty non-null-asserted references (`row.__identifier!`, `rowData.__identifier!`, `item.__identifier!`) are scattered across `LazyTable`, `EagerTable`, `containers/components/table/index.tsx.hbs`, `table-row-actions.tsx.hbs:29`, `hooks/useCRUDDialog.tsx.hbs:53`, and `ModeledTabs.tsx.hbs`. Each is a latent runtime error surface that the runtime does not have.

### F3 — "Is this row new?" test is a string-prefix check

Four sites branch on the draft prefix directly:

- `judo-ui-react/src/main/resources/actor/src/dialogs/index.tsx.hbs:292`
- `judo-ui-react/src/main/resources/actor/src/pages/index.tsx.hbs:280`
- `judo-ui-react/src/main/resources/actor/src/components/table/LazyTable.tsx.hbs:293`
- `judo-ui-react/src/main/resources/actor/src/components/table/EagerTable.tsx.hbs:261`

The runtime consults `rowData.__isNew` — a boolean, no string arithmetic. The template must adopt the boolean check.

### F4 — Hierarchical `data-testid` scheme

The existing branch `feature/JNG-6391_Test_Data-TestId` (13 commits, 55 templates, 110 `data-testid` emissions) covers the *breadth* of which elements should carry test IDs. It does **not** yet unify the *format*. Three defects remain:

1. **Ad-hoc suffixes rather than a role hierarchy.** Emissions such as `${id}-dropdown-toggle`, `${id}-clear-all`, `${id}-open-selector`, `${id}-inline-create`, `filter-operator-${item}` do not share a suffix vocabulary with the runtime.
2. **Nested-wrapper collisions unresolved.** For `SingleRelationInput`, `Tags`, and `TextWithTypeAhead`, the outer container, the input, and the autocomplete all render with the same raw XMI-id, so `page.getByTestId(...)` resolves to three elements. Documented as the flagship symptom in `dataid-template-runtime.pdf` §2.5.
3. **Row `data-testid` uses raw `__identifier`.** `buildRowTestId(a.id, params.row.__identifier)` cannot align with the runtime's row IDs until it routes through the shared `resolveTransferId` chain (F2).

The runtime's scheme, which this change adopts, is:

```
field::<xmiId>                             ← outer wrapper
field::<xmiId>::input                      ← underlying input element
field::<xmiId>::autocomplete               ← autocomplete wrapper
field::<xmiId>::dropdown                   ← dropdown/popper
field::<xmiId>::button::set                ← set/create/view/clear/selector
field::<xmiId>::button::clear
field::<xmiId>::button::create
field::<xmiId>::button::selector

table::<tableId>                           ← the table itself
table::<tableId>::row::<resolveTransferId(row)>
table::<tableId>::row::<id>::cell::<column>
```

### F5 — Out of scope: Phase-1 concerns

Two related audit findings are physically located in the sister repo `judo-ui-typescript-rest-template` and are explicitly **not** part of this change:

- The payload whitelist in `judo-ui-typescript-rest-template/judo-ui-typescript-rest-api/src/main/resources/common/utils.ts.hbs` (`applyStoredMembers`) copies six internal members onto every serialized payload; the runtime sends only `__signedIdentifier`. Playwright never inspects POST bodies, so this discrepancy does not block test parity.
- The same file line 15 contains a latent bug: `if (typeof instance.__version === 'string')` — but `JudoStored<T>.__version` is typed `number`, so the branch never fires. This template has zero references to `__version`; the defect has no observable effect on React-template output.

Both are recorded in `design.md` §Non-goals and should be raised as follow-up JIRA tickets against Phase 1.

## What Changes

Single Class III (react-template-only) change. Adds one new template file (a shared identity utility), refactors ~14 templates to use it, ports the coverage of `feature/JNG-6391_Test_Data-TestId` to a new role-suffixed format on a fresh branch, and refreshes snapshots across all six itests.

### (a) New shared identity utility

New file `judo-ui-react/src/main/resources/actor/src/utilities/transfer-id.ts.hbs` exports:

- `resolveTransferId(row, index?)` — fallback chain `__signedIdentifier ?? __identifier ?? __tempId ?? id ?? "idx-<index>"`. When `__identifier` starts with the legacy `draft:` prefix (residual seed from Phase 1's serializer at `judo-ui-typescript-rest-template/.../rest/serializer.ts.hbs:92-93`), the resolver SHALL still return that value verbatim so ids remain stable across renders; the runtime treats such rows identically.
- `newTempId()` — returns `` `temp::${Date.now()}::${randomHex(7)}` `` (matches the runtime's format documented in `dataid-template-runtime.pdf` §3).
- `isNewRow(row)` — returns `row.__isNew === true || (typeof row.__identifier === 'string' && row.__identifier.startsWith(DRAFT_PREFIX))`. The second clause is the F5 compatibility rung and SHALL be removed once Phase 1 stops seeding `draft:` prefixes (follow-up JIRA).
- `buildFieldTestId(xmiId, role?)`, `buildTableTestId(tableId)`, `buildRowTestId(tableId, row)`, `buildCellTestId(tableId, row, columnName)` — the four helpers that every `data-testid` emission SHALL route through.

Registered in `judo-ui-react/src/main/resources/ui-react.yaml` so the generator emits it once per actor at `src/utilities/transfer-id.ts`.

### (b) New-row seeding

The four seed sites (`OpenCreateFormAction.fragment.hbs:34,48`; `LazyTable.tsx.hbs:759`; `EagerTable.tsx.hbs:596`) SHALL write:

```ts
{ ...returnedData, __tempId: newTempId(), __isNew: true }
```

and SHALL NOT write `__identifier`. The imports of `draftIdentifierPrefix` and `uuidv4` at these sites are removed. `draftIdentifierPrefix` is still exported by Phase 1 for the compatibility rung inside `isNewRow`, but is no longer imported by any template in this repo.

### (c) "Is this row new?" branches

The four `rowData.__identifier!.startsWith(draftIdentifierPrefix)` branches (`dialogs/index.tsx.hbs:292`; `pages/index.tsx.hbs:280`; `LazyTable.tsx.hbs:293`; `EagerTable.tsx.hbs:261`) SHALL be replaced with `isNewRow(rowData)`.

### (d) Row-key resolver

The two `getRowIdentifier` closures (`EagerTable.tsx.hbs:309`, `LazyTable.tsx.hbs:340`) SHALL be replaced by direct use of `resolveTransferId`. The DataGrid `getRowId` prop points at `resolveTransferId`.

The `identifierAttribute={'__identifier'}` on the inline relation-column DataGrid (`fragments/relation/column.fragment.hbs:159`) SHALL be replaced by `getRowId={(r) => resolveTransferId(r)}`.

Every remaining non-null-asserted reference (`row.__identifier!`, `rowData.__identifier!`, `item.__identifier!`) SHALL be rewritten to `resolveTransferId(row)`. Approximately twenty sites; enumerated in `tasks.md`.

### (e) Hierarchical `data-testid` sweep

Every existing `data-testid` emission in the ~55 templates listed on `feature/JNG-6391_Test_Data-TestId` (110 sites total) SHALL be re-expressed through the four `build*TestId` helpers. Every ad-hoc suffix pattern is retired in favour of the role-suffixed hierarchy. The three nested-wrapper collision sites (`SingleRelationInput`, `Tags`, `TextWithTypeAhead`) SHALL receive distinct role suffixes on outer wrapper, autocomplete node, and input node — resolving `dataid-template-runtime.pdf` §2.5.

Row-level test IDs SHALL be `table::<tableId>::row::${resolveTransferId(row)}` — using the shared resolver — replacing the current `buildRowTestId(a.id, params.row.__identifier)` sites.

### (f) Snapshot refresh

Every committed `.tsx.snapshot` under `judo-ui-react-itest/**/src/test/resources/snapshots/frontend-react/**` that renders a re-formatted `data-testid` or new-row seed SHALL be refreshed by copying the regenerated file. Estimated ~50–150 snapshot files across the six itests based on the file-touch surface.

### What this change does NOT do

- Does **not** modify anything in `judo-ui-typescript-rest-template`. The payload-whitelist trim and the `__version` typeof bug are separately raised as follow-up JIRA tickets.
- Does **not** add `data-testid` coverage on any element that does not already carry one on `feature/JNG-6391_Test_Data-TestId`. This change reformats existing coverage; it does not extend it. If a Playwright test needs a new addressable node, that is a separate ticket.
- Does **not** change the runtime. The runtime is treated as the specification; this template conforms to it.
- Does **not** rename any existing generated exports outside `transfer-id.ts`. `resolveTransferId` / `newTempId` / `isNewRow` / `build*TestId` are new names; nothing existing is renamed.
- Does **not** introduce new template parameters, model attributes, or Java helper classes.

## Capabilities

### Added Capabilities

- **`transfer-identity`** — a new capability that owns the client-side identity contract for transfer objects: which field a row's identity comes from, how new rows are seeded, and how that identity threads into `data-testid` attributes. Owns three Requirements: (1) the row-key fallback chain, (2) the new-row `__tempId`/`__isNew` scheme, (3) the hierarchical `data-testid` scheme derived from (1).

### Modified Capabilities

- **`data-tables`** — refers row-key contract and row-level `data-testid` construction to `transfer-identity`; removes prescriptive mentions of the raw `__identifier` field.
- **`relation-management`** — the inline relation-column DataGrid uses `getRowId={resolveTransferId}` instead of `identifierAttribute={'__identifier'}`. Selector-mode row-testid derivation refers to `transfer-identity`.
- **`input-widgets`** — the three relation-flavoured widgets (`SingleRelationInput`, `Tags`, `TextWithTypeAhead`) SHALL emit distinct `data-testid` attributes on outer wrapper, autocomplete, and input DOM nodes.

## Impact

- **`judo-ui-react/src/main/resources/actor/src/utilities/transfer-id.ts.hbs`** — NEW file, ~80 lines.
- **`judo-ui-react/src/main/resources/ui-react.yaml`** — one new registry entry. 3 lines.
- **New-row seeding, ~4 files** — `OpenCreateFormAction.fragment.hbs`, `LazyTable.tsx.hbs`, `EagerTable.tsx.hbs`. ~10 lines net.
- **"Is new?" branch, ~4 files** — `dialogs/index.tsx.hbs`, `pages/index.tsx.hbs`, `LazyTable.tsx.hbs`, `EagerTable.tsx.hbs`. ~8 lines.
- **Row-key resolver, ~2 files** — `EagerTable.tsx.hbs`, `LazyTable.tsx.hbs`. ~6 lines.
- **Non-null-assertion cleanup, ~7 files** — `LazyTable.tsx.hbs`, `EagerTable.tsx.hbs`, `containers/components/table/index.tsx.hbs`, `components/table/table-row-actions.tsx.hbs`, `hooks/useCRUDDialog.tsx.hbs`, `components/ModeledTabs.tsx.hbs`, `fragments/relation/column.fragment.hbs`. ~25 lines.
- **`data-testid` format sweep, ~55 templates, 110 emission sites.** Bulk mechanical rewrite through the four `build*TestId` helpers. Ordering roughly follows the file list on `feature/JNG-6391_Test_Data-TestId`.
- **Snapshot refresh** — every affected `.tsx.snapshot` and any `system_*.json.snapshot` files that pick up unrelated hashes. Estimated 50–150 files across the six itests. Mechanical procedure per `AGENTS.md` §5.
- **Downstream consumers** — no external consumer imports the current `getRowIdentifier` closure. The new exports from `transfer-id.ts` are additive. Runtime extensibility hooks are unaffected.
