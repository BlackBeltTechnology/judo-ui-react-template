## ADDED Requirements

### Requirement: DataGrid row-select and header-select checkboxes carry per-row data-testid attributes

The generator SHALL configure every `<DataGrid>` rendered by `LazyTable.tsx` and `EagerTable.tsx` (sources: `actor/src/components/table/LazyTable.tsx.hbs` line ~713, `actor/src/components/table/EagerTable.tsx.hbs` line ~557) with a `slots.baseCheckbox` override pointing at a wrapper component (exported as `CustomCheckbox` from `actor/src/components/table/SelectCheckbox.tsx.hbs`) and a `slotProps.baseCheckbox.testIdPrefix` value equal to the enclosing Table's XMI ID (the existing `uniqueId` prop).

The wrapper component SHALL emit a `data-testid` attribute on the rendered `<Checkbox>` according to the following grammar:

- If MUI passes a defined, non-null, non-empty `rowId` prop, the testid SHALL be `${testIdPrefix}-select-row-${rowId}`.
- Else, if the wrapper receives a defined `__identifier` field (defensive fallback for custom row models that bypass MUI's `rowId` contract), the testid SHALL be `${testIdPrefix}-select-row-${__identifier}`.
- Else (header cell, or unresolvable row context), the testid SHALL be `${testIdPrefix}-select-all`.

The wrapper SHALL forward all other props (including `ref`, `checked`, `indeterminate`, `onChange`, `aria-label`) to MUI's `<Checkbox>` unchanged. ARIA attributes, keyboard handlers, focus management, and indeterminate state are preserved. The `data-testid` is purely additive.

The wrapper SHALL be invoked only when MUI's built-in `__check__` column is rendered — i.e. when `checkboxSelection` resolves truthy. Tables with `checkboxSelection={false}` SHALL render no selection column and SHALL NOT cause any wrapper invocations.

The DataGrid render sites SHALL merge the new `slots.baseCheckbox` and `slotProps.baseCheckbox.testIdPrefix` keys into any pre-existing `slots={{...}}` / `slotProps={{...}}` blocks rather than introducing a duplicate JSX prop (React's last-wins semantics would silently drop the earlier block).

#### Scenario: Row's selection checkbox carries the per-row data-testid

- **GIVEN** a table model element whose generated DataGrid has `checkboxSelection={true}`
- **AND** the table renders a row whose MUI-supplied `rowId` is the string `'r-42'`
- **AND** the enclosing Table's XMI ID (the `uniqueId` prop) is `'EditMyEntityTable'`
- **WHEN** the DataGrid renders the row's selection checkbox cell
- **THEN** the rendered `<Checkbox>` SHALL carry `data-testid="EditMyEntityTable-select-row-r-42"`
- **AND** the rendered `<Checkbox>` SHALL continue to carry its original `aria-label="Select row"` attribute
- **AND** the checkbox's `onChange`, `checked`, and keyboard-interaction behavior SHALL be identical to MUI's default

#### Scenario: Header's select-all checkbox carries the select-all data-testid

- **GIVEN** the same table as in the previous scenario (`checkboxSelection={true}`, `uniqueId === 'EditMyEntityTable'`)
- **WHEN** the DataGrid renders the header row's select-all checkbox cell (MUI does NOT pass a `rowId` prop to this cell)
- **THEN** the rendered `<Checkbox>` SHALL carry `data-testid="EditMyEntityTable-select-all"`
- **AND** the rendered `<Checkbox>` SHALL continue to carry its original `aria-label="Select all rows"` attribute
- **AND** the checkbox's indeterminate state, `onChange`, and keyboard-interaction behavior SHALL be identical to MUI's default

#### Scenario: Tables without checkbox selection emit no selection testids

- **GIVEN** a table model element whose generated DataGrid has `checkboxSelection={false}` (or where `checkboxSelection !== false` resolves falsy)
- **WHEN** the DataGrid renders
- **THEN** no `__check__` column SHALL appear in the DOM
- **AND** no `<Checkbox>` carrying a `data-testid` ending in `-select-row-...` or `-select-all` SHALL appear in the DOM scoped to that table's XMI ID
- **AND** the wrapper component (`CustomCheckbox`) SHALL NOT be invoked by MUI for that grid
- **AND** the change SHALL have zero observable DOM impact relative to the pre-change rendering

**NOTE** — The `data-testid` grammar (`-select-row-<rowId>` / `-select-all`) is single-purpose and scoped to F3's row-selection problem. Per-data-cell testids on non-selection columns are NOT covered by this requirement. Models that swap MUI's built-in `__check__` column for a hand-rolled selection column (via `customImplementation`) bypass `slots.baseCheckbox` and are responsible for their own testid grammar; see `design.md` § Non-Goals.

**NOTE** — This requirement is the DOM half of a Class II paired change. The catalogue companion (exposing the new testid prefixes under `<table>.rowSelect.row(rowId).id` and `<table>.rowSelect.all.id`) lives at `judo-ui-e2e-template/openspec/changes/expose-datagrid-select-actions-catalogue/` and MUST ship in lockstep.
