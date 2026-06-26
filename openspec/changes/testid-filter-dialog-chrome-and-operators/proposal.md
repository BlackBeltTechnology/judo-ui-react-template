## Why

The generated FilterDialog (`actor/src/components/dialog/FilterDialog.tsx.hbs`) exposes `data-testid` on its outer chrome (dialog root, title, close, Apply, Clear all) but two interactive surfaces remain unaddressable from Playwright `getByTestId(...)`:

| Element | File:line | Current testid | Problem |
|---|---|---|---|
| "Add new filter" `<DropdownButton>` (per-Filter) | `FilterDialog.tsx.hbs:343` | `${id}-dropdown` lives on the DropdownButton's internal wrapper, NOT on the user-facing trigger button | F9 — the opener button is reachable only by label (`getByRole('button', { name: 'Add new filter' })`); 45 spec sites blocked |
| Operator `<MenuItem>` inside `FilterOperator` | `FilterDialog.tsx.hbs:50-57` | `data-testid={valueId}` = `${id}-value` — **the same testid is emitted on EVERY operator option** (8-9 MenuItems per filter type) | F15 — Playwright strict-mode fails ("multiple elements matched"); specs fall back to `getByRole('option', { name: 'Not equals' })`; 25 spec sites blocked |

These are audit findings F9 (45 conv) + F15 (25 conv) from `/home/balazs/Asztal/prompts/reports/canonize-e2e-locators-to-testid.md`. They are bundled here because they live in the same template file, target the same dialog, and require the same release cadence (Class II paired — the React template emits new testids, then the e2e-template catalogue exposes them).

### F15 is a render-correctness bug

Lines 50-57 of `FilterDialog.tsx.hbs` render every operator option with `data-testid={valueId}` where `valueId === \`${id}-value\``. All MenuItems in the operator Select share that identical attribute. This is not just a missing testid — it is an actively wrong one. Any `getByTestId(\`${filterId}-value\`)` call matches every option simultaneously.

### Operators are platform-universal, not per-Filter

The set of operators is governed by the static enum `_FilterOperationType` defined in `actor/src/utilities/filter-helper.ts.hbs` and dispatched by `getOperatorsByFilter(filter)`:

- Numeric / Date / DateTime: `equal`, `notEqual`, `lessThan`, `lessOrEqual`, `greaterThan`, `greaterOrEqual`, `isEmpty`, `isNotEmpty`
- String: numeric set plus `like` (and `notLike` if exposed)
- Boolean / Trinary / Enumeration: `equal`, `notEqual`, `isEmpty`, `isNotEmpty`

Because every operator is a platform constant (not a model-derived id), per-operator testids should NOT carry the per-Filter `${id}` prefix. They are flat platform tokens of the shape `filter-operator-<kebab>`, addressable from the catalogue once and reused across every filter dialog of every actor.

## What Changes

Single Class III (react-template-only) DOM-side change. Touches one file (`FilterDialog.tsx.hbs`). No model change, no Java helper change, no catalogue change here — the companion catalogue exposure lives in `judo-ui-e2e-template/openspec/changes/expose-filter-dialog-chrome-and-operators-catalogue/`.

### (a) Emit `data-testid` on the Add-new-filter DropdownButton (F9)

- In `actor/src/components/dialog/FilterDialog.tsx.hbs` (line ~343, the `<DropdownButton>` that opens the attribute-pick menu), add `data-testid={\`${id}-add-new-filter\`}` as a JSX prop. `${id}` is the per-Filter XMI id already in scope (it is the same identifier used to construct `${id}-dialog-title`, `${id}-action-apply`, etc.).
- The DropdownButton component forwards unknown props to its trigger button (MUI `Button`), so the attribute reaches the DOM as documented for sibling test-id sites in this template.

### (b) Replace the duplicate operator testid with a per-operator literal (F15)

- In `actor/src/components/dialog/FilterDialog.tsx.hbs` (lines ~50-57, the `FilterOperator` component's `{getOperatorsByFilter(filter).map(...)}` block), replace `data-testid={valueId}` on the `<MenuItem>` with `data-testid={\`filter-operator-${kebabCase(item)}\`}`.
- `kebabCase(item)` converts the camelCase operator enum value to kebab-case (`notEqual` → `not-equal`, `lessOrEqual` → `less-or-equal`, `isNotEmpty` → `is-not-empty`). The full transformation table is locked in `design.md`.
- `valueId` (`${id}-value`) is unused on the MenuItem after this change and is removed from the MenuItem prop list. The variable declaration on line ~46-48 remains untouched — it is still used by the operator Select wrapper (`<Select data-testid={valueId}>` on line ~48 stays as-is and is already addressable via `${id}-operator` — wait, let me re-read: line 48 actually uses `${operatorId}` = `${id}-operator` for the Select wrapper. Confirmed: `valueId` is used ONLY on the MenuItem at line 57). After this change `valueId` is no longer referenced and the declaration is removed.

### Pattern precedent

`actor/src/components/widgets/TrinaryLogicCombobox.tsx.hbs:63-65` already emits per-value testids on a sibling MUI Select:

```jsx
<MenuItem data-testid={`${id}-true`}  value={'Yes'}>...</MenuItem>
<MenuItem data-testid={`${id}-false`} value={'No'}>...</MenuItem>
<MenuItem data-testid={`${id}-undefined`} value={'Unknown'}>...</MenuItem>
```

This change applies the same idiom to the filter operator MenuItem, with the difference that the suffix is the operator enum value (kebab-cased) and the prefix is the platform constant `filter-operator-` (not `${id}-`) because operators are universal, not per-Filter.

### What this change does NOT do

- Does **not** modify the EMF UI model or any generator Java helper. Both fixes live entirely in the runtime JSX of one template file.
- Does **not** alter the catalogue exposed by `judo-ui-e2e-template`. The companion change `expose-filter-dialog-chrome-and-operators-catalogue` adds:
  - `<filterDialog>.addNewFilter.id` exposing `${filterId}-add-new-filter`
  - `PlatformTestIds.filterDialog.operators.<camelCase>.id` exposing the flat `filter-operator-<kebab>` constants
- Does **not** touch the attribute-pick MenuItems inside the DropdownButton's dropdown (`DropdownButton.tsx.hbs:110`). Those already carry each column's XMI id (`filterOption.id`) and are partially catalogued via the column id. F9's scope is the OPENER button only.
- Does **not** modify the operator-pick `<Select>` wrapper on line 48. It already carries `data-testid={\`${id}-operator\`}` and is addressable via the existing catalogue once the e2e-template's Filter exposure includes `operatorSelect.id`.
- Does **not** modify operator translation keys (`judo.modal.filter.${item}` on line 58). The visible label is decoupled from the testid.
- Does **not** address other audit findings (F1 / F3 / F5 / F10 / F11 / F14 — separate clusters with their own change folders).

## Capabilities

### Added Capabilities

- **`filter-dialog-chrome-and-operators`** — new capability with two ADDED requirements:
  1. Every per-Filter `FilterDialog` SHALL emit `data-testid={\`${id}-add-new-filter\`}` on its Add-new-filter `<DropdownButton>`.
  2. Every operator `<MenuItem>` SHALL emit a platform-fixed `data-testid={\`filter-operator-<kebab>\`}` derived from the operator enum value, where `<kebab>` is the kebab-case form of the camelCase enum name.

## Impact

- **`actor/src/components/dialog/FilterDialog.tsx.hbs`** (~2 edit sites):
  - Line ~343: add `data-testid={\`${id}-add-new-filter\`}` to the `<DropdownButton>` opening tag. ~1 line.
  - Lines ~50-57: replace `data-testid={valueId}` on the operator `<MenuItem>` with `data-testid={\`filter-operator-${kebabCase(item)}\`}`. Add a small `kebabCase` helper either by importing an existing utility or by inlining `item.replace(/[A-Z]/g, '-$&').toLowerCase()`. Remove the now-unused `valueId` local. ~3-4 lines.
- **Generated TypeScript**: every regenerated `FilterDialog.tsx` carries a per-Filter `${id}-add-new-filter` testid on the Add-new-filter button and one unique `filter-operator-<kebab>` testid per operator MenuItem. Existing tests that addressed via labels continue to pass; tests that addressed via the old (broken) `${id}-value` on MenuItems were already failing under strict-mode and used label fallbacks — they remain green.
- **Integration test (`judo-ui-react-itest`)**: `./mvnw clean install` regenerates and runs Vitest/Playwright on the fixture frontends. The change is purely additive at the DOM level (one new attribute on one button; one replaced — not removed — attribute on the MenuItem). No existing assertion should break.
- **Downstream consumer (`BlackBeltTechnology/judo-tatami-tests`)**: after release + bump and after the companion catalogue change ships, the 45 (F9) + 25 (F15) = 70 spec sites that fall back to `getByRole(...)` can be converted to `getByTestId(...)`:
  - `getByRole('button', { name: 'Add new filter' })` → `getByTestId(<filter>.addNewFilter.id)`
  - `getByRole('option', { name: 'Not equals' })` → `getByTestId(PlatformTestIds.filterDialog.operators.notEqual.id)`
