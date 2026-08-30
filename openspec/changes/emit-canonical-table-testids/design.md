# Design

## Context

`judo-frontend-runtime` is the reference implementation of the unified `data-testid` contract. This
change closes the last three deltas between it and this template so one Playwright suite in
`judo-tatami-tests` runs unchanged on both engines. Verified facts the design rests on:

| Fact | Evidence |
| --- | --- |
| Both engines use MUI x-data-grid v8 | template `8.23.0` (`actor/package.json.dependencies.fragment.hbs:12`); runtime `^8.0.0` (`packages/components/package.json:75`) |
| Runtime stamps row/cell/filter IDs via slots | `TableRenderer.tsx:1041` `slots={{ row, cell, filterPanel }}` |
| Runtime settled row actions on `::action::<role>` | `RowActionCell.tsx:161` uses `getRowActionTestId`; `:162` `getButtonTestId` is the no-role fallback |
| Runtime reserves `overflow` as a role | `RowActionCell.tsx:164` |
| Selection cell derives from the cell grammar | `test-ids/src/table.ts:56` `getSelectionCellTestId → getCellTestId(…, "__check__")` |
| Runtime namespaces browser state | `core/src/utils/storage-namespace.ts` |
| Every RelationTest table and field element carries a `sourceId` | model scan: `ui:Table` 33/33, all input widgets 48/48 |
| No `xmi:id` in the model contains `@` | model scan: 0 of 2909 |

## Goals / Non-Goals

**Goals.** Emit canonical row, cell, selection-cell and filter-panel IDs. Resolve the D3 row-action
grammar. Namespace persisted browser state. Keep grid behavior bit-identical.

**Non-Goals.** No change to `judo-frontend-runtime` or `judo-ui-typescript-rest-template`. No new
Playwright specs here. No new test IDs beyond the four classes above. No rename of `build*TestId`
helpers to `get*TestId` (still deferred per `align-testids-with-runtime-package`).

## Decisions

### D1. Port the runtime's slot mechanism verbatim rather than inventing a template-specific one

Both engines are on MUI x-data-grid v8, so `slots={{ row, cell, filterPanel }}` behaves identically.
Copying the runtime's structure — memoized wrapper components that forward all props and add only
`data-testid` — makes divergence a diff rather than an interpretation.

*Rejected:* `getRowClassName`/`getCellClassName` to smuggle identity into class names. Reason: the
contract is explicitly `data-testid`-based; class-name locators are what this whole effort removes.

*Rejected:* a post-render `useEffect` DOM sweep stamping attributes. Reason: races virtualization,
and re-stamping on every scroll is both slow and unobservable to tests deterministically.

### D2. `row` and `cell` slots receive the table identity, not the model element

The runtime computes `_tableId = element.sourceId ?? element["xmi:id"] ?? "unknown"` once
(`TableRenderer.tsx:784`) and passes the string into the slots. This template already threads a
`tableTestId` prop into `EagerTable`/`LazyTable` (`EagerTable.tsx.hbs:84`), currently rendered as
`'{{ getXMIID table }}'`. That prop must switch to `{{ getElementId table }}` so it yields the
`sourceId` when present — otherwise rows would be keyed on the long `Actor/(esm/…)/…` path while the
container div's `buildTableTestId('{{ getElementId table }}')` uses the short form, and the row IDs
would not be descendants of the advertised table ID.

This is a real inconsistency in the current branch state and is the highest-risk item in the change:
`tableTestId` is presently used only for row-action IDs, so nothing has yet exposed the mismatch.

*Rejected:* keep `getXMIID` for `tableTestId` and rewrite the container div to match. Reason: it
would diverge from the runtime, which prefers `sourceId` everywhere.

### D3. Positional fallback index: prefer MUI's own index for rows, the data index for cells

Mirror the runtime exactly, including its asymmetry: the row slot passes `props.index`, the cell slot
passes `tableRows.indexOf(props.row)`. The asymmetry only becomes observable for a row with no
identity field at all, which cannot occur for a backend-returned transfer (always has
`__identifier`) nor a client-created one (seeded `__tempId`). Matching the runtime's behavior —
including its quirk — is safer than "fixing" it here and creating a cross-engine difference.

Flagged as an open question in the proposal for the tatami pilot audit to confirm empirically.

### D4. Row-action grammar switches to `::action::<role>`, with a role-resolution helper

The runtime derives the role via `getButtonRole` (`test-ids/src/element.ts`), which maps action
types to stable cross-engine roles: `opensetselector→set`, `opencreateform→create`,
`openpage`/`rowopenpage→view`, `rowdelete→delete`, otherwise the raw action type. This template
already has `UiActionsHelper.getButtonActionType` (added by `align-buttons-with-runtime`) producing
the un-mapped action type, so a role mapping must be added alongside it to avoid emitting
`::action::rowdelete` where the runtime emits `::action::delete`.

The existing `TableRowAction.testId` field currently feeds `a.testId ?? a.id`. Its meaning narrows to
"canonical action role", which is the breaking surface named in the proposal.

*Rejected:* leave D3 open again. Reason: the tatami suite must address row actions, and the runtime
evidence is now unambiguous.

### D5. Browser-state namespacing lives in its own template file, exported through the barrel

`storage-namespace.ts.hbs` is a leaf module with no imports, so `axiosInterceptor` (which is imported
very early during auth bootstrap) can depend on it without a cycle. It is registered in
`ui-react.yaml` and re-exported from `utilities/index.tsx.hbs` for ordinary consumers, while the five
call sites import it directly by path to keep the early-bootstrap path barrel-free.

The derivation reads `document.baseURI` rather than a generated constant, so the namespace is
automatically correct in production, in single-actor mode (application owns the root path), and in
the dev server — no build-time coupling to the deployment path.

*Rejected:* namespace by generated model/actor name. Reason: the collision is caused by the *serving
path*, and two deployments of the same actor on different paths must not share state.

### D6. Snapshot refresh is audited for disappearing IDs, not just added ones

Every table container changes, so the refresh is large. Bounding rule, following
`align-buttons-with-runtime` §5.4: capture per-file `data-testid` occurrence counts before and after.
This change is purely additive for rows/cells/filters (counts strictly increase) and 1:1 for
row actions (count unchanged). Any file whose count *decreases* blocks the commit.

## Risks / Trade-offs

| Risk | Mitigation |
| --- | --- |
| `tableTestId` switch from `getXMIID` to `getElementId` (D2) silently changes existing row-action IDs. | Intended and simultaneous with the D4 grammar change, so no consumer observes an intermediate state. Verified by grepping generated output for `::row::` segments carrying an `Actor/(esm/…)` prefix — expected zero. |
| Slot wrappers break grid virtualization or editing. | Wrappers forward all props and add one attribute; memoized on identity. Guarded by the existing six-itest Vite build plus inline-edit and selection scenarios in the paired tatami run. |
| MUI changes slot prop shapes in a minor release. | Both engines pin the same major (v8) and would move together; a divergence surfaces as a cross-engine test failure, which is exactly what the suite detects. |
| Namespacing strands existing users' stored state. | One-time re-login and reset preferences; no server-side data affected. Called out as breaking in the proposal. |
| Filter-panel input IDs collide when several filter rows are open. | The runtime accepts the same constraint (one filter row edited per open/close cycle). Documented rather than engineered around, to stay engine-identical. |
| Cross-engine equality is asserted here but provable only in the paired tatami change. | Sequencing is explicit: this change's scenarios state the equality requirement; `react-template-e2e-parity` executes it. Neither is accepted alone. |

## Migration Plan

1. Fast-forward the working branch onto the four code-complete JNG-6391 changes; resolve the single
   `ui-react.yaml` insertion conflict (both sides add distinct entries to the same `templates:` list).
2. Land the browser-state namespacing (already drafted in the working tree) — it is a precondition
   for any two-engine run, and independently testable.
3. Add the helpers (`buildRowActionTestId`, `buildFilterPanelTestId` + variants) and the Java role
   mapping, test-first per repo rule 7.
4. Switch `tableTestId` to `getElementId` and the row-action grammar together (D2 + D4).
5. Register the three slots in both table components.
6. Refresh snapshots with the D6 audit; verify all six itests build.
7. Hand off to `react-template-e2e-parity` for cross-engine execution.

## Open Questions

- Does any RelationTest table row reach the `idx-<n>` fallback in practice? Expected no; the tatami
  pilot audit answers it and either closes D3's asymmetry concern or turns it into a real defect.

### OQ1. Inline-edit controls have no counterpart in the runtime (resolved)

This template renders `edit`/`save`/`cancel` row controls through `TableRowAction.testId` for inline
editing. These are UI-only pseudo-actions with no model action definition, so `getButtonRole` cannot
produce a role for them and the runtime has no equivalent emission: its `RowActionCell` only handles
model-derived buttons, and inline row editing is driven purely through
`rowModesModel`/`processRowUpdate` (`TableRenderer.tsx:1011`, `:1013`) with no per-control test ID.

Consequences: `::action::<role>` cannot be derived for these three controls from the shared
vocabulary, and no cross-engine equality can be asserted because one side emits nothing.

Options: (a) keep the literal `edit`/`save`/`cancel` tokens as reserved roles, documented as
template-only until the runtime adopts them; (b) fall back to the standalone `button::` grammar;
(c) raise a runtime ticket to emit matching IDs and block on it. Option (a) is the working
assumption — it is additive, keeps the tatami inline-edit specs runnable on this engine, and does not
pre-empt the runtime's choice — but it must be confirmed against a live runtime DOM before task 4
closes, and any spec relying on it is engine-specific until the runtime catches up.

Resolution: a live `RelationTest_runtime/Actor` inspection exercised the nested
`ManyAggregationAssociation` table's `InlineCreateRowActionDefinition` and found no `edit`, `save`,
or `cancel` controls or test IDs. The template therefore reserves these three roles; they remain
template-only until the runtime emits equivalent controls.

### OQ2. Temp-ID formats differ between engines (affects unsaved-row scenarios)

The two engines mint different `__tempId` values:

| engine | format | source |
| --- | --- | --- |
| this template | `temp::<ms>::<7-hex>` | `transfer-id.ts.hbs` `newTempId` |
| runtime, CRUD path | `temp::<ms>::<base36>` | `actions/src/handlers/crud/crud-handlers.ts:664` |
| runtime, inline-create path | `inline-<n>` | `TableRenderer.tsx:337` |

Since a row's test ID embeds the resolved transfer ID, an unsaved row's canonical selector is not
cross-engine predictable — and the runtime is not even self-consistent between its own two paths.
A test must therefore discover an unsaved row's identity from the DOM rather than construct it, or
assert only on the `temp::`/prefix shape. Worth noting that this template's `temp::<ms>::<hex>` form
matches the runtime's *CRUD* path, so the divergence is specifically the runtime's inline-create
path. Out of scope to fix here (it is runtime-side), but it constrains how the paired tatami change
writes unsaved-row assertions.
