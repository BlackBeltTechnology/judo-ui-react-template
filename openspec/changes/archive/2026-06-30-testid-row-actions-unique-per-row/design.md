## Context

Two coupled audit findings (F11 and the widened scope of F10) both stem from the same root cause: testids that should vary per DOM occurrence are computed as static strings outside the render-time iteration.

| Symptom | Root cause | Mechanism |
|---|---|---|
| F11 (edit/save/cancel) | `editActions[].id` computed once per `useMemo`, then `getActions(params)` renders the same `id` per row | `<Button data-testid={a.id}>` inside a closure over a stable array, called N times |
| F11 (other row actions) | Same — every `tableRowActions[].id` is similarly N-duplicated | Same render-site, identical mechanism |
| F10 narrow scope | `containerIsSelector` flag only filters Create button; other toolbar / row / checkbox testids unchanged | MUI dialog portals do not unmount the underlying page, so both mounts emit identical testids |

The N-duplication for row actions is a strictly worse case than F10's: F10 produces 2 duplicates (page + selector), F11 produces N×2 (N rows × 2 mounts) in the worst case.

## Design Decisions

### 1. Render-site composition (Phase A)

**Decision**: row-suffix happens in `table-row-actions.tsx.hbs`, not in `LazyTable`/`EagerTable`'s `editActions` arrays.

**Why**: the action arrays are computed once with `useMemo`; composing the suffix there is impossible because `params.row` doesn't exist yet. The render-site (`getActions(params)`) is where the row identifier becomes available.

**Alternative considered & rejected**: change `editActions[].id` to `editActions[].idFactory: (row) => string`. Rejected because (a) it requires every external caller of `columnsActionCalculator` (currently any container generating `tableRowActions`) to also use the factory shape, doubling the migration surface, and (b) `tableRowActions` come from containers we don't want to thread row state through.

The render-site approach gives us **one place** to compose the suffix and one type addition (`isSelectorMount: boolean`) on the calculator signature. Container-generated `tableRowActions` get the fix for free with no upstream changes.

### 2. `testId` + `suppressTestId` on DropdownButton (not overloading `id`)

**Decision**: DropdownButton gets two new optional props rather than re-typing `id` to allow undefined.

**Why**: `id` doubles as React `key` for menu items (`<Tooltip key={menuItem.id}>`) and key collisions cause subtle React rendering bugs. Keeping `id` as a stable scalar string preserves keying; the new `testId` field decouples the testid contract from the keying contract.

`suppressTestId: boolean` exists as a third state alongside `testId: string | undefined` because TypeScript destructuring loses the "key was present in props but value was undefined" signal. An explicit boolean is unambiguous, type-safe, and self-documenting at call sites.

### 3. Strip from selector mount, not page mount (Phase B, Option 1)

**Decision**: when `containerIsSelector` / `isSelectorTable` is true, omit `data-testid` attributes inside the TableComponent body.

**Why this is the page mount that becomes canonical**:
- The page mount is the long-lived, addressable-via-URL state of the table; testids on it are referenced by specs that navigate to the page.
- The selector mount is transient, dialog-scoped, and (per tatami-tests survey, e.g. `CrudActionsOnSingleAndManyRelationsTest.spec.ts:542`) already approached via role-based selectors in current specs.
- The MUI Dialog containing the selector applies `aria-hidden`/`inert` to the underlying page, so background interactivity is already blocked at the DOM-event level — but the page tree remains in the DOM (portal), so testids on it are still queryable when no dialog is open.

**Alternative considered & rejected**: introduce an `<ActiveOverlayContext>` provided by every Dialog wrapper and consumed by page TableComponents to suppress their testids while an overlay is open. Rejected because:
- Requires touching `dialog.tsx`, `page.tsx`, both Table components, and every dialog opener — high blast radius.
- Page testids become context-conditional, surprising for debugging in DevTools and breaking the simple "this testid always exists" mental model that current specs rely on.
- No current spec failure justifies this complexity; the selector-mount-strip option achieves the same uniqueness invariant with strictly less coupling.

**Alternative considered & rejected**: re-namespace selector-mount testids with a `-in-selector` suffix instead of stripping. Rejected because:
- The downstream catalogue would need to expose dual identities for every per-Table testid, doubling its surface area.
- Current specs already use role-based selectors inside selector dialogs, so the additional addressability has no concrete consumer.

### 4. Type signature: optional with default, not required

**Decision**: `isSelectorMount?: boolean = false` is optional with a `false` default at the function signature; the type interface marks it `?`.

**Why**: every existing caller of `columnsActionCalculator` is internal (only `LazyTable` and `EagerTable` invoke it). Both are updated in this change. Marking the parameter optional with `false` default means any downstream caller that ever extends or wraps the calculator continues to compile without source changes.

## Backwards Compatibility

| Surface | Before | After | Compat |
|---|---|---|---|
| `DropdownButton({id})` callers (any prior call) | `data-testid={id}` emitted on trigger | `data-testid={id}` emitted on trigger (`testId === undefined`, `suppressTestId === false`) | ✓ identical |
| `DropdownButton menuItems[].id` (prior shape) | `data-testid={menuItem.id}` on MenuItem | `data-testid={menuItem.id}` on MenuItem (no `testId` / `suppressTestId` on item) | ✓ identical |
| `columnsActionCalculator(... 9 args)` callers | 9-ary call | 9-ary call (`isSelectorMount` defaults to `false`) | ✓ identical |
| `CustomCheckbox` with `testIdPrefix: uniqueId` (any prior call) | composes prefix + suffix | composes prefix + suffix | ✓ identical |
| `CustomCheckbox` with `testIdPrefix: undefined` (new selector-mode call from LazyTable/EagerTable) | (previously: emitted `-select-all` with empty prefix → `"-select-all"`) | omits `data-testid` entirely | **intentional behavioural change scoped to new caller** |

The only intentional behaviour change is `CustomCheckbox` when receiving `testIdPrefix === undefined`. Before this change, `prefix = testIdPrefix ?? ''` produced testids like `"-select-all"`; the LazyTable/EagerTable templates never passed `undefined` previously, so no existing caller observed that branch. The new selector-mount call sites are the first to set `undefined`.

## Validation Gates

1. Generator unit tests (`mvn test` on `judo-ui-react`) — pass.
2. `ActionGroupTestPro/action_group_test_pro__god` itest with `-DforceSnapshotOverwrite=true` — only `LazyTable.tsx.snapshot` and `EagerTable.tsx.snapshot` change (verified via `git diff --stat`).
3. Same itest without `-DforceSnapshotOverwrite=true` — snapshot diff-checker passes (exit 0).
4. Same itest with frontend build (no `-DskipExecuteFrontendBuild`) — Biome formatter + Vite TypeScript compile both pass (exit 0).
5. Full reactor build across all 6 itests — green (verification in progress when this design.md was authored).
6. Downstream tatami-tests `mvn install` — no spec change required (zero references to `editActions.*.id` outside the auto-generated catalogue; selector-mount testid presence not assumed by current specs).
