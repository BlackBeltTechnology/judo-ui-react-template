## Context

Audit finding F3 (`/home/balazs/Asztal/prompts/reports/canonize-e2e-locators-to-testid.md` §F3): MUI X DataGrid v8.23.0 renders its built-in `__check__` column (when `checkboxSelection` is truthy) using its internal `GridCellCheckboxRenderer` / `GridHeaderCheckbox`. Those renderers emit `<Checkbox>` cells with `aria-label="Select row"` / `aria-label="Select all rows"` and NO `data-testid`. Spec authors are forced to `getByLabel('Select row').nth(i)`, which:

- Couples the locator to the row's current visual index (broken by sort / filter / pagination).
- Cannot disambiguate between multiple grids on the same page.
- Cannot address the header's select-all in a way that scopes to a specific grid.

The Explore agent (2026-06-10) confirmed:

- Render sites are exactly two: `LazyTable.tsx.hbs:713` and `EagerTable.tsx.hbs:557`.
- NO custom selection column exists in the generated code; MUI's built-in column does all the work.
- `@mui/x-data-grid` is pinned at v8.23.0 in the generated `package.json` template.
- The outer wrapper `<div data-testid={getXMIID(table)}>` at `containers/components/table/index.tsx.hbs:282` provides per-table scoping but does not solve per-row uniqueness.
- The `uniqueId` prop (Table's XMI ID) is already threaded from `containers/components/table/index.tsx.hbs` into both LazyTable and EagerTable.

This is the DOM half of a Class II paired change. The catalogue half — exposing the new testid prefixes under `<table>.rowSelect.*` — lives in `judo-ui-e2e-template/openspec/changes/expose-datagrid-select-actions-catalogue/`. Without the DOM half, the catalogue points at nothing; without the catalogue half, spec authors get no autocomplete. Both MUST ship together.

## Render chain (file:line)

1. **TableComponent emits the DataGrid host props** — `actor/src/containers/components/table/index.tsx.hbs:~282` wraps in `<div data-testid={getXMIID(table)}>` and instantiates `<LazyTable />` or `<EagerTable />` with `uniqueId={getXMIID(table)}`.
2. **LazyTable / EagerTable render the `<DataGrid>`** — `LazyTable.tsx.hbs:713` and `EagerTable.tsx.hbs:557` pass `checkboxSelection={checkboxSelection !== false}` and an existing `slotProps={{...}}` block (already present at `LazyTable.tsx.hbs:~675–680` and `EagerTable.tsx.hbs:~520–525`).
3. **MUI renders the `__check__` column** — when `checkboxSelection` is truthy, MUI internally instantiates `GridCellCheckboxRenderer` (row cells) and `GridHeaderCheckbox` (header cell). Both use `slots.baseCheckbox` as the underlying component. MUI passes the row's `id` as a `rowId` prop on row cells; the header cell receives no `rowId`.
4. **Default `baseCheckbox`** is MUI's plain `<Checkbox>`. The fix replaces it with a thin wrapper that computes a `data-testid` from `rowId` + a `testIdPrefix` passed via `slotProps.baseCheckbox`.

## Goals

1. Spec authors can address each row's selection checkbox by `getByTestId(<table>.rowSelect.row(rowId).id)` without strict-mode violations.
2. Spec authors can address the header's select-all checkbox by `getByTestId(<table>.rowSelect.all.id)`.
3. Zero impact on tables with `checkboxSelection={false}` — the `__check__` column is not rendered, so the wrapper is never invoked.
4. Zero impact on ARIA, keyboard, focus, indeterminate state — the wrapper delegates to MUI's `<Checkbox>` for all rendering concerns other than the added `data-testid`.
5. Implementation surface under ~50 lines across at most 3 files (one new wrapper component + two existing render sites).

## Non-Goals

1. NOT addressing custom selection columns added via `customImplementation`. Models that hand-roll a selection column bypass `slots.baseCheckbox`; their testids are a separate concern.
2. NOT introducing per-data-cell testids. Per-cell testids on non-selection columns are a separate audit cluster.
3. NOT renaming or restructuring the existing `slotProps` block. The fix merges the `baseCheckbox` key into the existing block.
4. NOT extending the EMF model. The testid grammar is intrinsic to the runtime DOM, not a per-table user-configurable setting.
5. NOT consolidating `LazyTable.tsx.hbs` and `EagerTable.tsx.hbs` into a shared abstraction. The two have legitimate behavioral differences (lazy fetch vs eager preload); merging them is out of scope.
6. NOT unifying with F10's `containerIsSelector` / `isSelectorTable` naming inconsistency. Pre-existing, separate cleanup.

## Decisions

### D1 — Use `slots.baseCheckbox` (component override) rather than `slotProps.baseCheckbox['data-testid']` (prop override)

**Decision**: Replace MUI's default `baseCheckbox` with a custom wrapper component via `slots={{ baseCheckbox: CustomCheckbox }}`. Pass the static `testIdPrefix` via `slotProps={{ baseCheckbox: { testIdPrefix: uniqueId } }}`.

**Alternatives considered**:
- Pass a single `data-testid` literal via `slotProps={{ baseCheckbox: { 'data-testid': '...' } }}`. Rejected: MUI passes the SAME `baseCheckbox` props to BOTH row cells AND the header cell, so we cannot discriminate the per-row identifier from outside the component. A static string would either collide across all row cells or fail to distinguish header from rows.
- Wrap the entire `__check__` column with a custom `renderCell` / `renderHeader`. Rejected: requires intercepting `columns` at construction time, increasing diff surface and risking breakage of MUI's internal selection-state wiring (indeterminate header state, keyboard nav).
- Use MUI's `componentsProps` (v5 API). Rejected: deprecated in v8; `slots` / `slotProps` is the v8-canonical API.

A component-level override is the only way to read MUI's per-render `rowId` context and emit a discriminating `data-testid`.

### D2 — Discriminate header vs row by presence of the `rowId` prop

**Decision**: In `CustomCheckbox`, if `rowId !== undefined` (and not the empty string), emit `<testIdPrefix>-select-row-<rowId>`; otherwise emit `<testIdPrefix>-select-all`.

**Alternatives considered**:
- Inspect `props.className` for MUI's `MuiDataGrid-checkboxInputCell` vs `MuiDataGrid-columnHeaderCheckbox` markers. Rejected: couples the wrapper to internal class names that MUI may rename in minor releases.
- Use a separate `slot` for the header. Rejected: MUI v8 does not expose a distinct `headerCheckbox` slot — `baseCheckbox` is the single seam.

The `rowId` prop is part of MUI's documented contract for `GridCellCheckboxRenderer` (it forwards the row's `id` to the checkbox slot).

### D3 — Fall back to row's `__identifier` if `rowId` is undefined on a row cell

**Decision**: If MUI omits `rowId` on a row cell (defensive guard — could happen if a future custom row model breaks MUI's contract), inspect the surrounding row data for an `__identifier` field (the JUDO-canonical row id) and use that. If both are missing, emit `<testIdPrefix>-select-row-unknown` and log a dev-mode warning.

**Alternatives considered**:
- Throw / refuse to render. Rejected: would break the grid entirely for an edge case that may never materialize.
- Silently fall back to the row index. Rejected: index-coupled testids are exactly the brittleness F3 is trying to eliminate.

### D4 — Co-locate the wrapper in a new file rather than inlining in both tables

**Decision**: Create `actor/src/components/table/SelectCheckbox.tsx.hbs`. Both `LazyTable.tsx.hbs` and `EagerTable.tsx.hbs` import the exported component.

**Alternatives considered**:
- Inline the wrapper as a local `const CustomCheckbox = ...` inside each table file. Rejected: duplicates ~25 lines across two files and invites drift (see D5 from F10 — `containerIsSelector` vs `isSelectorTable` is exactly the kind of drift that arose from not centralizing).
- Put the wrapper in `actor/src/utilities/`. Rejected: the file is a React component, not a utility function; the `components/table/` directory is its semantic home.

### D5 — Merge `slots` / `slotProps` into pre-existing blocks rather than introducing new prop spreads

**Decision**: The implementer SHALL grep `LazyTable.tsx.hbs` and `EagerTable.tsx.hbs` for any existing `slots={{` and `slotProps={{` props on the `<DataGrid>` and merge the new `baseCheckbox` key into those existing blocks. If no such block exists, add a new one at the same indentation level as `checkboxSelection`.

**Alternatives considered**:
- Add a second `slots={{...}}` JSX prop. Rejected: React's last-wins semantics would silently drop the earlier block; would be a footgun for downstream contributors.

### D6 — Tie the `testIdPrefix` to the existing `uniqueId` prop

**Decision**: Pass `uniqueId` (already destructured in both tables; resolves to `getXMIID(table)`) as `testIdPrefix`. Do NOT introduce a new prop name.

**Alternatives considered**:
- Introduce a new prop `selectionTestIdPrefix`. Rejected: `uniqueId` already serves exactly this role (it's the Table's XMI ID and is already used for other testid-derived strings).

## Source-mapping table

| File | Line (approx) | Current state | After this change |
|---|---|---|---|
| `actor/src/components/table/LazyTable.tsx.hbs` | top of file (imports) | no `SelectCheckbox` import | adds `import { CustomCheckbox } from './SelectCheckbox';` |
| `actor/src/components/table/LazyTable.tsx.hbs` | ~675–680 | existing `slotProps={{...}}` block | adds `baseCheckbox: { testIdPrefix: uniqueId }` key; adds sibling `slots={{ baseCheckbox: CustomCheckbox, ... }}` (merging) |
| `actor/src/components/table/LazyTable.tsx.hbs` | 713 | `checkboxSelection={checkboxSelection !== false}` | unchanged |
| `actor/src/components/table/EagerTable.tsx.hbs` | top of file (imports) | no `SelectCheckbox` import | adds the same import |
| `actor/src/components/table/EagerTable.tsx.hbs` | ~520–525 | existing `slotProps={{...}}` block | analogous merge |
| `actor/src/components/table/EagerTable.tsx.hbs` | 557 | `checkboxSelection={checkboxSelection !== false}` | unchanged |
| `actor/src/components/table/SelectCheckbox.tsx.hbs` | — | does not exist | new file, ~25–35 lines |
| `actor/src/containers/components/table/index.tsx.hbs` | 282 | `<div data-testid={getXMIID(table)}>` wrapper | unchanged |

## Risks

| Risk | Mitigation |
|---|---|
| MUI version drift: future upgrade past v8 might rename `slots.baseCheckbox` or change how `rowId` is forwarded. | Pin an inline `// F3: MUI v8.23.0 API — see https://mui.com/x/react-data-grid/components/#slots` comment in `SelectCheckbox.tsx.hbs`. CI integration build will surface a TypeScript or render error on upgrade; the wrapper is a single ~30-line file, trivial to re-target. |
| `rowId` undefined on row cells in custom row models. | D3 fallback to `__identifier`, then to literal `-unknown` with a dev-mode warning. Documented in spec. |
| Custom selection columns added via `customImplementation` bypass `slots.baseCheckbox`. | Documented as a Non-Goal. Models that swap the `__check__` column are responsible for their own testids. |
| Header's `rowId` accidentally truthy in some MUI internal — would emit `-select-row-undefined` instead of `-select-all`. | The wrapper SHALL check `typeof rowId === 'undefined' || rowId === null || rowId === ''` and treat all three as "header cell". Add a unit-test in the wrapper itself if a vitest harness exists; otherwise rely on the itest fixture to surface mis-classification. |
| Spec authors confuse the wrapper testid (`-select-row-<rowId>`) with the outer wrapper testid (`getXMIID(table)`) and try to use the latter for row selection. | The paired catalogue change exposes ONLY the per-row / select-all leaves under `<table>.rowSelect.*`; the outer wrapper testid stays under its existing key. Mis-use is avoidable by following the catalogue. |
| The new wrapper component re-renders on every grid cell render, marginally hurting large-grid perf. | The wrapper is a single `React.forwardRef(({ rowId, testIdPrefix, ...rest }) => <Checkbox {...rest} data-testid={...} />)`. No state, no hooks — equivalent perf to MUI's default. |
| `hiddenInSelectorMode` semantics from F10 interact poorly with selector-mounted grids' row-select checkboxes. | F10 hides toolbar buttons; this change touches the `__check__` column body. The two are orthogonal. No interaction expected. |

## Implementation sketch

**`actor/src/components/table/SelectCheckbox.tsx.hbs`** (new file):

```tsx
import { forwardRef } from 'react';
import { Checkbox, CheckboxProps } from '@mui/material';

// F3 (audit 2026-06-10): emit data-testid on every selection checkbox MUI renders
// for a DataGrid's built-in __check__ column. The same component is used for
// row cells (rowId present) AND the header cell (rowId absent), so we
// discriminate at render time.
//
// MUI API: see https://mui.com/x/react-data-grid/components/#slots (v8.x).

export interface SelectCheckboxProps extends CheckboxProps {
  testIdPrefix?: string;
  rowId?: string | number;
  // Defensive: some row models may attach __identifier directly.
  __identifier?: string;
}

export const CustomCheckbox = forwardRef<HTMLButtonElement, SelectCheckboxProps>(
  ({ testIdPrefix, rowId, __identifier, ...rest }, ref) => {
    const prefix = testIdPrefix ?? '';
    const isRowCell = !(rowId === undefined || rowId === null || rowId === '');
    let testId: string;
    if (isRowCell) {
      testId = `${prefix}-select-row-${rowId}`;
    } else if (__identifier) {
      testId = `${prefix}-select-row-${__identifier}`;
    } else {
      testId = `${prefix}-select-all`;
    }
    return <Checkbox {...rest} ref={ref} data-testid={testId} />;
  },
);
```

**`actor/src/components/table/LazyTable.tsx.hbs` (line ~675–680, existing `slotProps` block):**

```jsx
+ import { CustomCheckbox } from './SelectCheckbox';
  ...
- slotProps={{ /* existing keys */ }}
+ slots={{ /* merge: */ baseCheckbox: CustomCheckbox }}
+ slotProps={{ /* existing keys + */ baseCheckbox: { testIdPrefix: uniqueId } }}
  checkboxSelection={checkboxSelection !== false}
```

**`actor/src/components/table/EagerTable.tsx.hbs` (line ~520–525):** analogous.
