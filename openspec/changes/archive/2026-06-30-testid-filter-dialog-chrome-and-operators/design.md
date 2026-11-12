## Context

Audit findings F9 (`/home/balazs/Asztal/prompts/reports/canonize-e2e-locators-to-testid.md` §F9) and F15 (§F15), bundled because they live in the same template file (`actor/src/components/dialog/FilterDialog.tsx.hbs`) and target the same dialog.

- **F9** (45 spec sites): the per-Filter "Add new filter" `<DropdownButton>` (line 343) carries no surfaced `data-testid`. The DropdownButton component DOES emit `${id}-dropdown` on an internal wrapper but that wrapper is not the trigger button users click — Playwright authors fall back to `getByRole('button', { name: 'Add new filter' })`.
- **F15** (25 spec sites): the operator `<MenuItem>` (line 57) carries `data-testid={valueId}` where `valueId === \`${id}-value\``. Every operator option (8-9 per filter type) renders with the same attribute → strict-mode locator failure → label fallback `getByRole('option', { name: 'Not equals' })`.

Verified by Explore agents 2026-06-10 against `judo-ui-react/src/main/resources/actor/src/components/dialog/FilterDialog.tsx.hbs`.

## Render chain (file:line)

1. **Per-Filter dialog mount** — `FilterDialog.tsx.hbs:1` exports the `FilterDialog` component, instantiated per Filter model element with `${id}` = the Filter's XMI id.
2. **Chrome testids in place** — line 279 root `Dialog`, line 282 title, line 226 close button, line 294 Clear all, line 299 Apply (N) — all carry `${id}`-rooted testids that are already catalogued.
3. **Add-new-filter opener (F9 site)** — line 343 renders `<DropdownButton .../>` with NO `data-testid` prop. The DropdownButton internally renders MUI `<Button>` + portal `<Menu>`; the DropdownButton's own implementation tags its container with `${id}-dropdown`, but that container is not the trigger.
4. **Operator Select (already addressable)** — line 48 declares `const operatorId = \`${id}-operator\`;` and the `<Select data-testid={operatorId} ...>` wrapper carries it. Catalogue exposure pending in the companion change but DOM is correct.
5. **Operator MenuItem (F15 bug site)** — lines 50-57:
   ```jsx
   const valueId = `${id}-value`;
   ...
   {getOperatorsByFilter(filter).map((item) => (
     <MenuItem
       data-testid={valueId}                       // BUG — same id on every option
       value={item}
       key={item}
     >
       {t(`judo.modal.filter.${item}`, ...)}
     </MenuItem>
   ))}
   ```
6. **`getOperatorsByFilter`** — defined in `actor/src/utilities/filter-helper.ts.hbs`, returns an array drawn from the `_FilterOperationType` enum, dispatched on `filter.attributeType`:
   - Numeric / Date / DateTime → `[equal, notEqual, lessThan, lessOrEqual, greaterThan, greaterOrEqual, isEmpty, isNotEmpty]`
   - String → numeric set + `like` (and `notLike` if present in enum)
   - Boolean / Trinary / Enumeration → `[equal, notEqual, isEmpty, isNotEmpty]`

## Goals

1. Spec authors can address the Add-new-filter button via `getByTestId(<filter>.addNewFilter.id)` where `<filter>.addNewFilter.id === \`${filterXmiId}-add-new-filter\``.
2. Spec authors can address each operator option via `getByTestId(PlatformTestIds.filterDialog.operators.<camelCase>.id)` where the id is the flat platform constant `filter-operator-<kebab>`.
3. Per-operator testids are UNIQUE within any open FilterDialog — `getByTestId(...)` strict-mode never produces "multiple elements matched" on operator options.
4. Implementation surface stays under 10 lines in one file. No Java helper change. No model change. No catalogue change in this repo.

## Non-Goals

1. NOT addressing the attribute-pick MenuItems inside the DropdownButton's dropdown (`DropdownButton.tsx.hbs:110`). Those already carry each column's XMI id (`filterOption.id`) and are addressable via the column-level catalogue exposure.
2. NOT touching the operator Select wrapper at line 48. It already emits `${id}-operator`; the catalogue's per-Filter exposure (companion change) picks it up.
3. NOT renaming or restructuring `valueId` / `operatorId` consts beyond what is necessary to delete the now-unused `valueId`.
4. NOT introducing operator translation keys, model attributes, or generator helpers. Both fixes are pure JSX prop additions.
5. NOT touching the `DropdownButton` component itself. The `data-testid` flows through MUI's prop forwarding to the rendered trigger.

## Decisions

### D1 — Per-Filter prefix for Add-new-filter; flat platform prefix for operator options

**Decision**: The Add-new-filter button testid is `${filterId}-add-new-filter` (per-Filter). The operator MenuItem testids are `filter-operator-<kebab>` (flat platform constants).

**Rationale**: The Add-new-filter button is one-per-Filter and the user needs to address THE button on THIS specific filter dialog — so the testid is parameterized by the Filter's XMI id (mirrors `${id}-action-apply`, `${id}-dialog-title`). The operator options, by contrast, are a fixed enum-driven list — every filter dialog of every actor renders the same set of operator MenuItems with the same semantic meaning. There is no value in scoping the testid by Filter id; it would force spec authors to know the filter id to address "Equals", which is silly. Flat constants reduce 45+ per-filter ids in the catalogue down to a single `PlatformTestIds.filterDialog.operators.equal.id`.

**Alternatives considered**:
- Use `${id}-operator-${kebabCase(item)}` for the MenuItem (per-Filter operator ids). Cataloguing burden: every Filter would need to enumerate the same 8 ids in its catalogue entry. Spec back-pass would also be more verbose. Rejected.
- Use `filter-add-new-filter` (flat) for the Add-new-filter button. Rejected — there are typically MULTIPLE Filter dialogs accessible across an app, and an open dialog may be one of several; the user typically opens one explicitly via its `${id}` and the catalogue's per-Filter path is the natural addressing scheme.

### D2 — kebab-case transformation policy

**Decision**: Use `item.replace(/[A-Z]/g, '-$&').toLowerCase()` inline. No external lodash dependency, no project utility helper. ASCII-safe for the closed enum.

**Rationale**: The operator enum `_FilterOperationType` is finite and stable (locked by the JUDO meta-ui metamodel). All values are ASCII camelCase identifiers. A 1-line regex is sufficient; importing `kebabCase` from lodash adds a runtime dependency on a function used in exactly one place.

**Full transformation table** (locked):

| Enum value (camelCase) | kebab-case suffix | Resulting testid |
|---|---|---|
| `equal` | `equal` | `filter-operator-equal` |
| `notEqual` | `not-equal` | `filter-operator-not-equal` |
| `lessThan` | `less-than` | `filter-operator-less-than` |
| `lessOrEqual` | `less-or-equal` | `filter-operator-less-or-equal` |
| `greaterThan` | `greater-than` | `filter-operator-greater-than` |
| `greaterOrEqual` | `greater-or-equal` | `filter-operator-greater-or-equal` |
| `isEmpty` | `is-empty` | `filter-operator-is-empty` |
| `isNotEmpty` | `is-not-empty` | `filter-operator-is-not-empty` |
| `like` | `like` | `filter-operator-like` |
| `notLike` | `not-like` | `filter-operator-not-like` |

The regex `/[A-Z]/g` matches each uppercase letter; `'-$&'` prefixes a dash before it; `.toLowerCase()` normalizes to all-lower. For `equal` (no uppercase letters) the result is `equal` unchanged. For `isNotEmpty` the result is `-is-not-empty` after the regex, then `is-not-empty` after `toLowerCase()` — wait, the regex prepends `-` to EACH uppercase letter, including the leading `i`... no: the leading `i` is already lowercase, only `N` and `E` get prefixed. Result: `is-Not-Empty` → `is-not-empty`. Verified by trace.

**Edge case**: `like` and `notLike` (string-type operators) have no leading uppercase letter group at start; transformation produces `like` and `not-like` respectively. Correct.

### D3 — Inline the `kebabCase` helper at call site (no module-level export)

**Decision**: Inline the regex inside the `.map(...)` callback rather than declaring a `const kebabCase = (s) => ...` at the top of the file.

**Rationale**: One call site, one line. No reusability pressure within this template. Avoids polluting the imports or top-of-file declarations of `FilterDialog.tsx`.

**Alternative**: Add a `kebabCase` to `actor/src/utilities/string.ts.hbs` (if such a file exists) and import it. Defer unless a second call site emerges.

### D4 — Delete the now-unused `valueId` const declaration

**Decision**: After replacing the MenuItem's `data-testid={valueId}` with the inline literal, the `valueId` declaration at line ~46-48 is no longer referenced and is removed.

**Verification**: grep confirms `valueId` appears ONLY in the MenuItem's `data-testid` (line 57). Removing the declaration is safe and eliminates a dead variable.

### D5 — Use the existing `${id}` scope variable for the Add-new-filter testid

**Decision**: The Add-new-filter testid `\`${id}-add-new-filter\`` reuses the same `${id}` already used by `${id}-dialog-title` (line 282), `${id}-action-apply` (line 299), `${id}-action-clear-all` (line 294), `${id}-close` (line 226). No new variable.

### D6 — Pass `data-testid` as a JSX prop on `<DropdownButton>` and rely on prop forwarding

**Decision**: Add `data-testid={\`${id}-add-new-filter\`}` directly as a JSX attribute on the existing `<DropdownButton .../>` element.

**Verification**: Pattern precedent — sibling MUI components in this template accept and forward `data-testid` via their default prop spread. `DropdownButton` is a local component (`actor/src/components/widgets/DropdownButton.tsx.hbs`); a quick read of its render tree confirms it forwards arbitrary props onto its rendered MUI `<Button>` (or its outer wrapper). If audit reveals the prop is swallowed, the fallback is to lift `data-testid` to a known forwarded prop on the DropdownButton API — but the standard MUI idiom holds.

**Risk note**: If `DropdownButton` does NOT forward unknown props, this needs a one-line patch in `DropdownButton.tsx.hbs` to spread `...rest` onto the rendered trigger. Verify during integration.

## Risks

| Risk | Mitigation |
|---|---|
| `DropdownButton` swallows the `data-testid` prop instead of forwarding to its rendered trigger button. | Verify in integration: regenerate the itest frontend, inspect the rendered DOM, confirm `data-testid="…-add-new-filter"` appears on the user-clickable button (not just on an internal wrapper). If swallowed, patch `DropdownButton.tsx.hbs` to spread unknown props. Tracked in `tasks.md` §6. |
| `_FilterOperationType` enum gains new values in a future judo-meta-ui release. | The kebab-case transformation is mechanical; any new ASCII camelCase value automatically gets a correct flat testid. The catalogue exposure (companion change) would need a one-line addition per new operator, but the React template needs no further change. Documented in spec.md NOTE. |
| Two enum values collide in kebab form (e.g. `notEqual` vs `not_equal`). | The enum is closed and currently uses pure camelCase with no underscores. A collision would surface at TypeScript compile time of the catalogue (duplicate keys) before any DOM ambiguity arises. No runtime collision possible with the current enum. |
| Spec back-pass cost. | 70 conversion sites (45 F9 + 25 F15) across `judo-tatami-tests`. Single search-replace per finding using the new catalogue paths. Costed; in scope for the downstream follow-up. |
| Removing `valueId` breaks an unrelated reference. | Grep confirms `valueId` is referenced only at line 57. Removal is mechanical and verified by a regeneration smoke. If a downstream Handlebars partial extends `FilterDialog.tsx.hbs` and references `valueId`, that would surface at compile. None found in current scan. |
| Existing specs that erroneously addressed `getByTestId(\`${filterId}-value\`)` would break further (they're already broken — match multiple). | Those sites already fall back to `getByRole('option', ...)`. Conversion to the new flat ids is part of the spec back-pass. No regression — they were never passing under strict mode. |

## Implementation sketch

**`actor/src/components/dialog/FilterDialog.tsx.hbs`** — two edits:

```jsx
// (1) F15 — lines ~46-57: replace duplicate operator testid; remove unused valueId
- const valueId = `${id}-value`;
  const operatorId = `${id}-operator`;
  ...
  {getOperatorsByFilter(filter).map((item) => (
    <MenuItem
-     data-testid={valueId}
+     data-testid={`filter-operator-${item.replace(/[A-Z]/g, '-$&').toLowerCase()}`}
      value={item}
      key={item}
    >
      {t(`judo.modal.filter.${item}`, ...)}
    </MenuItem>
  ))}
```

```jsx
// (2) F9 — line ~343: add data-testid to the Add-new-filter DropdownButton
  <DropdownButton
+   data-testid={`${id}-add-new-filter`}
    id={`${id}-dropdown`}
    ...
  />
```

Two attribute changes, one variable removal. No imports added or removed.
