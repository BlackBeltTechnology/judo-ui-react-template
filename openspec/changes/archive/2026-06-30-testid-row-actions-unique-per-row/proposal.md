## Why

Audit finding **F11** (2026-06-26): `LazyTable.tsx.hbs` and `EagerTable.tsx.hbs` define three `editActions` objects with `id: \`${uniqueId}-row-edit\`` / `-row-save` / `-row-cancel`, and pass them through `columnsActionCalculator(...)` which renders the same `id` on the action button **once per row** (`table-row-actions.tsx.hbs:79` — `<Button data-testid={a.id}>`). Result: a table with N rows has N copies of the same `data-testid` in the DOM.

This is the **tip of an iceberg**. The same N-copy duplication structurally applies to **every** `tableRowActions` entry (delete, view, custom operations, dropdown menu items, dropdown trigger button) — not just edit/save/cancel — because `getActions(params)` is invoked once per row and the action `id` is computed once outside the row loop.

The downstream catalogue (`judo-ui-e2e-template`'s generated `VisualElementIds.ts`) exposes these as scalar `editActions.{edit,save,cancel}.id` and `tableRowActions[].id`, **misleading spec authors** into `getByTestId(...editActions.edit.id)` — that selector hits N elements and Playwright strict-mode fails.

Additionally, audit finding **F10's narrow scope** — only the form-opening Create button is hidden in selector-overlay context — means the rest of the same TableComponent (rows, headers, checkboxes, edit/save/cancel buttons, row-action dropdowns, every toolbar button except Create) still duplicates when an AddSelector dialog overlays a page with the same XMI table. F10's commit explicitly notes "the same TableComponent gets mounted twice in the DOM" because MUI dialogs are portals and do not unmount the underlying page. F10 only fixes one symptom.

Tatami-tests survey (2026-06-26) confirms current Playwright specs **already defend** against this duplication by using `getByRole(...)`/label-based selectors inside selector dialog overlays (e.g. `models/RelationTest/.../CrudActionsOnSingleAndManyRelationsTest.spec.ts:542` *"dialog-overlay Create; testid uncatalogued at selector-dialog scope"*). The page mount is the canonical testid carrier; the selector mount is reached via role-based selectors.

## What Changes

Single Class III (react-template-only) change. No model change, no Java helper change, no catalogue contract change (the catalogue regenerates downstream against the new DOM emission).

### (a) Row-unique testids on per-row action buttons (F11 broad)

In `actor/src/components/table/table-row-actions.tsx.hbs`:

- Add an optional last parameter `isSelectorMount: boolean = false` to `columnsActionCalculator`.
- Add a helper `buildRowTestId(actionId, rowIdentifier) → \`${actionId}-${rowIdentifier}\`` (fallback to bare `actionId` if no rowIdentifier).
- At the three render sites (split-action Button line ~79, DropdownButton trigger, dropdown MenuItems), pass the row-suffixed testid via:
  - `data-testid={isSelectorMount ? undefined : buildRowTestId(a.id, params.row.__identifier)}` on the split Button.
  - New `testId` + `suppressTestId` props on DropdownButton (composed from row identifier).
- React `key`s continue to use the scalar `action.id` for stability — only the emitted `data-testid` is row-suffixed.

### (b) DropdownButton: explicit `testId` / `suppressTestId` props

In `actor/src/components/DropdownButton.tsx.hbs`:

- Add optional `testId?: string` and `suppressTestId?: boolean` props to both `DropdownButtonProps` and `DropdownMenuItem`.
- Trigger button: `data-testid={suppressTestId ? undefined : (testId ?? id)}` — preserves legacy `id`-as-testid when neither new prop is set.
- Menu items: `data-testid={menuItem.suppressTestId ? undefined : (menuItem.testId ?? menuItem.id)}` — same fallback.
- Backwards compatible: every existing caller that passes only `id` continues to emit `data-testid={id}` unchanged.

### (c) Strip testids in selector-overlay mount (F10 widening, Option 1)

In `actor/src/components/table/SelectCheckbox.tsx.hbs`:

- When `testIdPrefix` is `undefined` or `null`, return `<Checkbox>` with no `data-testid` attribute (the page mount is canonical).

In `actor/src/components/table/LazyTable.tsx.hbs`:

- Pass `containerIsSelector` as the new `isSelectorMount` argument to `columnsActionCalculator`.
- `baseCheckbox: { testIdPrefix: containerIsSelector ? undefined : uniqueId }` — suppress checkbox testids in selector mount.
- `data-testid={containerIsSelector ? undefined : toolBarAction.id}` on each toolbar Button — suppress toolbar testids in selector mount.

In `actor/src/components/table/EagerTable.tsx.hbs`:

- Symmetric to LazyTable using the existing `isSelectorTable` prop (already destructured at line 137 by F10).

### (d) Type signature update

In `actor/src/utilities/interfaces.ts.hbs`:

- Append optional `isSelectorMount?: boolean` parameter to `ColumnActionsProvider<R, RStored>`. Documented inline.

### What this change does NOT do

- Does **not** modify the EMF UI model or any generator Java helper.
- Does **not** change the catalogue contract. The downstream `judo-ui-e2e-template` regenerator may opt to reshape `editActions.{edit,save,cancel}` from scalar `id` to `idPrefix` (matching the existing `selectActions.selectRow` precedent at `VisualElementIds.ts:3831`), but that is a downstream concern and out of scope here.
- Does **not** introduce a React context for cross-mount coordination — selector-mount suppression uses the existing `containerIsSelector` / `isSelectorTable` props already threaded by F10. Stripping happens in the **selector mount** (where it duplicates from the page mount); the page mount keeps its testids and is the canonical carrier.
- Does **not** break existing Playwright specs. Tatami-tests survey confirms current specs use `getByRole(...)`/label selectors inside selector dialog overlays already (see `CrudActionsOnSingleAndManyRelationsTest.spec.ts:542`); zero specs reference `editActions.{edit,save,cancel}.id` outside the auto-generated catalogue.
- Does **not** address the cross-mount risk for stacked-modal flows (F15 low-priority finding) or platform-fixed singleton testids (F2/F8). Those remain documented constraints.

## Capabilities

### Modified Capabilities

- **`table-toolbar-rendering`** — Existing requirement (from `dedupe-create-button-in-selector`) is extended: in selector-mount context, the table additionally suppresses `data-testid` on (a) all toolbar Buttons, (b) header and per-row selection checkboxes, (c) per-row action Buttons and dropdown menu items.

- **`table-row-actions-testids`** (new capability) — One new ADDED requirement: per-row action buttons in non-selector mounts MUST emit `data-testid` composed as `${actionId}-${rowIdentifier}` so every rendered button is row-unique in the DOM, satisfying Playwright strict-mode locators.

## Impact

- Affected templates: `DropdownButton.tsx.hbs`, `SelectCheckbox.tsx.hbs`, `LazyTable.tsx.hbs`, `EagerTable.tsx.hbs`, `table-row-actions.tsx.hbs`, `interfaces.ts.hbs` (6 files).
- Affected snapshots: `ActionGroupTestPro/action_group_test_pro__god/.../LazyTable.tsx.snapshot` and `EagerTable.tsx.snapshot` (2 files) regenerate. No other itests carry committed snapshots for these files.
- Generated React app: per-row action testids change from scalar `<id>` to `<id>-<rowIdentifier>`. Toolbar / checkbox / row-action testids are absent (rather than duplicated) in selector dialog mounts. The Vite+TypeScript build remains green; verified end-to-end against `ActionGroupTestPro` (full pnpm/Biome/Vite pipeline, exit 0).
- Downstream `judo-ui-e2e-template`: catalogue regenerator should reshape `editActions.{edit,save,cancel}` from `id:` to `idPrefix:` to match new DOM emission. No spec migration required (zero existing references to these ids).
