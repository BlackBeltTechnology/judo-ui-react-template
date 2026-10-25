## Why

Generated MUI DataGrid tables enable row selection via `checkboxSelection={checkboxSelection !== false}` (`actor/src/components/table/LazyTable.tsx.hbs:713`, `actor/src/components/table/EagerTable.tsx.hbs:557`). The grid renders its built-in `__check__` column whose checkboxes (one per row + one in the header for select-all) emit ONLY ARIA labels — `aria-label="Select row"` and `aria-label="Select all rows"` — and carry NO `data-testid`. There is no custom selection column in the generated code; MUI renders the cells itself.

This is audit finding F3 (`/home/balazs/Asztal/prompts/reports/canonize-e2e-locators-to-testid.md` §F3, ~120 conversion sites blocked). Playwright specs cannot address row-select / select-all checkboxes by testid; they fall back to brittle label-based locators (`getByLabel('Select row')` matches ALL row checkboxes in the grid, forcing `nth(i)` indexing tied to row order — which breaks when default sort changes or rows are filtered).

The outer table wrapper at `actor/src/containers/components/table/index.tsx.hbs:282` already carries `data-testid={getXMIID(table)}`, so spec authors COULD scope with `within(...)` — but per-row uniqueness still requires checkbox-level testids that encode the row identifier. Without them, a spec cannot say "select the row whose `__identifier === 'r-42'`" by testid.

This is a Class II paired change: the DOM-side fix lives here; the catalogue companion (exposing the new testid prefixes under `<table>.rowSelect.*`) lives at `judo-ui-e2e-template/openspec/changes/expose-datagrid-select-actions-catalogue/`. Without the DOM fix, the catalogue has nothing to point at; without the catalogue, spec authors get no autocomplete. The two MUST ship together.

## What Changes

Single Class II (react-template-side) change. Touches at most three files (two render sites + one new shared component). No model change, no Java helper change.

### (a) Introduce a slot-level `baseCheckbox` override on `<DataGrid>`

MUI X DataGrid v8.23.0 supports `slots={{ baseCheckbox: CustomCheckbox }}` / `slotProps={{ baseCheckbox: {...} }}` for customizing every checkbox the grid renders (row cells + header cell). A custom wrapper component receives MUI's render context — including a `rowId` prop on row cells and an absent `rowId` on the header cell — and can compute a discriminating `data-testid` per call.

### (b) Add a small wrapper component `SelectCheckbox.tsx.hbs`

- New file `actor/src/components/table/SelectCheckbox.tsx.hbs`. Exports `CustomCheckbox` (or equivalently named) that:
  - Accepts `testIdPrefix: string` (the enclosing Table's XMI ID, threaded in from the existing `uniqueId` prop).
  - Resolves the rendered `data-testid` to `<testIdPrefix>-select-row-<rowId>` when MUI passes a `rowId` prop (i.e. row cell).
  - Resolves the rendered `data-testid` to `<testIdPrefix>-select-all` when `rowId` is absent (i.e. header cell).
  - Falls back to the row's `__identifier` if `rowId` is undefined and `__identifier` is available (defensive guard against custom row models).
  - Otherwise delegates rendering entirely to MUI's `<Checkbox>` so ARIA, keyboard, and focus behavior remain untouched.

### (c) Wire `slots.baseCheckbox` into both DataGrid render sites

- In `actor/src/components/table/LazyTable.tsx.hbs` (line ~675–680, existing `slotProps={{...}}` block adjacent to line 713's `checkboxSelection`), pass `slots={{ baseCheckbox: CustomCheckbox }}` and `slotProps={{ baseCheckbox: { testIdPrefix: uniqueId } }}`. Merge into any pre-existing `slots` / `slotProps` props rather than replacing them.
- In `actor/src/components/table/EagerTable.tsx.hbs` (line ~520–525), apply the same wiring. The `uniqueId` prop already flows from `containers/components/table/index.tsx.hbs` into both tables.

### What this change does NOT do

- Does **not** touch the e2e-template catalogue. The companion catalogue change lives at `judo-ui-e2e-template/openspec/changes/expose-datagrid-select-actions-catalogue/`. This change cross-references it; both ship together per the Class II paired-change protocol.
- Does **not** modify MUI's ARIA, keyboard, or focus behavior. The slot-injected `data-testid` is purely additive; the rendered DOM still carries the original `aria-label`.
- Does **not** change `checkboxSelection` semantics. Tables with `checkboxSelection={false}` (the default for non-selection-enabled tables) render no `__check__` column and therefore no checkbox testids — the change has zero DOM impact in that case.
- Does **not** introduce row-level testids on any cell other than the selection checkbox. Per-cell testids on data cells are a separate cluster (out of scope; not part of F3).
- Does **not** address custom selection columns added via `customImplementation`. Models that swap out the built-in `__check__` column for a hand-rolled one bypass `slots.baseCheckbox` and SHALL be documented as a Non-Goal.
- Does **not** address F10 (already shipped as `dedupe-create-button-in-selector`), F1 / F5 / F9 / F11 / F14 / F15 (separate clusters).

## Capabilities

### Added Capabilities

- **`datagrid-row-select-testids`** — One new ADDED requirement ("DataGrid row-select and header-select checkboxes carry per-row data-testid attributes") locking the slot-override semantics and the testid suffix grammar.

## Impact

- **`actor/src/components/table/SelectCheckbox.tsx.hbs`** (new file): ~25–35 lines. Exports the wrapper component.
- **`actor/src/components/table/LazyTable.tsx.hbs`** (line ~675–680 + import line): add import, add `slots` / merge `slotProps`. ~3–4 lines.
- **`actor/src/components/table/EagerTable.tsx.hbs`** (line ~520–525 + import line): analogous. ~3–4 lines.
- **Generated TypeScript**: every `<DataGrid>` whose generated `checkboxSelection` resolves truthy now receives a `slots.baseCheckbox` override and emits `data-testid="<tableXmiId>-select-row-<rowId>"` on each row's selection checkbox plus `data-testid="<tableXmiId>-select-all"` on the header. Tables with `checkboxSelection={false}` are unaffected.
- **Integration test (`judo-ui-react-itest`)**: `./mvnw clean install` regenerates and runs Vitest/Playwright on fixture frontends. The change is purely additive at the DOM level; no existing assertion should break.
- **Downstream consumer (`BlackBeltTechnology/judo-tatami-tests`)**: after both the react-template change and the paired catalogue change land + are bumped, the ~120 conversion sites blocked on F3 can be migrated from `getByLabel('Select row').nth(i)` to `getByTestId(<table>.rowSelect.row(rowId).id)` and `getByTestId(<table>.rowSelect.all.id)`.
