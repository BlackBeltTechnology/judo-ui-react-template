## Why

Every generator-emitted MUI `<Autocomplete>` widget currently relies on MUI's built-in default `noOptionsText` (the string `"No options"`), which is **hard-coded English** and does not participate in the application's i18n pipeline. In a Hungarian-locale deployment the user searching a `SingleRelationInput`, `Tags`, `TextWithTypeAhead`, or the enumeration filter inside `SingleValueFilterComponent` sees English text jammed between otherwise localized labels the moment their query returns zero rows.

Verified 2026-07-08 by `grep noOptionsText` across `judo-ui-react/src/main/resources/actor/`: **no widget passes the prop**. Four `<Autocomplete>` call sites exist in the generator; all fall back to MUI's default:

| Widget template | Autocomplete line | Source of options | `freeSolo`? |
|---|---|---|---|
| `actor/src/components/widgets/SingleRelationInput.tsx.hbs` | 175 | Server-paginated (via `onAutoCompleteSearch`) | yes |
| `actor/src/components/widgets/Tags.tsx.hbs` | 199 | Server-paginated (via query customizer) | yes |
| `actor/src/components/widgets/TextWithTypeAhead.tsx.hbs` | 97 | Server action `get{Attr}Options` returning `string[]` | yes |
| `actor/src/components/table/SingleValueFilterComponent.tsx.hbs` | 165 | Finite local enum array (`filter.filterOption.enumValues`) | no |

Reproducing on any regenerated frontend: open any `SingleRelationInput`, type a query with no matches → dropdown reads `"No options"` regardless of the active locale.

This is the symmetric bug to JNG-6409 (`autocomplete-more-results-hint`). Where that change localized the *"there may be more"* signal, this change localizes the *"there is nothing"* signal. Together they close the empty-input / partial-input / no-match cycle with locale-correct messaging.

### Why MUI's `noOptionsText` prop is not sufficient on three of the four widgets

MUI `<Autocomplete>` deliberately suppresses `noOptionsText` when `freeSolo={true}` is set. The rendering guard inside `Autocomplete.js` is:

```
if (groupedOptions.length === 0 && !freeSolo && !loading) { … renders noOptionsText … }
```

Documented in MUI issues [#18985](https://github.com/mui-org/material-ui/issues/18985), [#37862](https://github.com/mui/material-ui/issues/37862), [#45576](https://github.com/mui/material-ui/issues/45576). The stated rationale from the maintainer: *"The `freeSolo` prop is meant to primarily support the search box with suggestion pattern. Displaying a message when empty goes against this primary objective."* MUI classifies this as by-design and has repeatedly rejected the request to change it.

Consequently, on `SingleRelationInput`, `Tags`, and `TextWithTypeAhead` (all `freeSolo`) the `noOptionsText` prop is a no-op — it never reaches the DOM. Verified live 2026-07-09 against ActionGroupTest: typing a non-matching query into the Astronomer relation input produced no empty-state message at all, in either locale, with the `noOptionsText` prop set.

The only reliable injection point on freeSolo widgets is the same one JNG-6409 uses: **the `slots.paper` component**. MUI still renders the Paper wrapper (even under freeSolo when options is empty) — only the internal "no options" element inside it is suppressed. Extending JNG-6409's `useAutocompleteMoreResultsHintPaper` hook to also render the no-results hint reuses machinery already present in three of the four widgets.

For `SingleValueFilterComponent` (no `freeSolo`), the `noOptionsText` prop works natively and is kept as-is — it is the smallest possible change for that widget and needs no Paper-slot ceremony.

## What Changes

Single Class III (react-template-only) change. Follows JNG-6409's structural convention (one hint = one file): one new i18n key plus a new sibling shared-component file `AutocompleteNoResultsHint.tsx.hbs`. The JNG-6409 file `AutocompleteMoreResultsHint.tsx.hbs` keeps its exported `<AutocompleteMoreResultsHint>` FC unchanged; only its now-obsolete `useAutocompleteMoreResultsHintPaper` hook is removed (subsumed by the new composition hook). The new file owns both the JNG-6410 `<AutocompleteNoResultsHint>` FC and the composition hook `useAutocompleteHintPaper({ limit, optionsLength, loading })`, which renders whichever hint applies (or neither) inside one Paper slot. This is necessary because MUI Autocomplete accepts only one `slots.paper` component and both hints must share it.

### (a) New i18n key

- `actor/public/i18n/system_en-US.json.hbs`: `"judo.autocomplete.no-results": "No matching results"`
- `actor/public/i18n/system_hu-HU.json.hbs`: `"judo.autocomplete.no-results": "Nincs találat"`
- `actor/public/i18n/system_default.json.hbs`: same as `en-US`.

Placed inside the existing `judo.autocomplete.*` block introduced by JNG-6409, immediately after `judo.autocomplete.type-for-more-results`, so all autocomplete-scoped strings live together.

### (b) New sibling file for the no-results hint + composition hook

New file `actor/src/components/widgets/AutocompleteNoResultsHint.tsx.hbs` exports:

- `<AutocompleteNoResultsHint>` FC — renders the localized "No matching results" text inside a `<Box>`.
- `useAutocompleteHintPaper({ limit, optionsLength, loading })` hook — returns a memoized `Paper` component. Inside the Paper, above `paperProps.children`, it renders:
  - the "no results" hint when `!loading && optionsLength === 0`, or
  - the "more results" hint (imported from `AutocompleteMoreResultsHint.tsx`) when `typeof limit === 'number' && limit > 0 && optionsLength >= limit`.
  - The two states are mutually exclusive by construction (`0` vs `≥ limit > 0`).

The JNG-6409 file keeps its `<AutocompleteMoreResultsHint>` FC unchanged. Its now-obsolete `useAutocompleteMoreResultsHintPaper` hook is removed — subsumed by the new composition hook. The old hook's only three callers live inside this generator and are updated to import `useAutocompleteHintPaper` from the new file.

### (c) Wire the new hook into the three freeSolo widgets

For `SingleRelationInput`, `Tags`, and `TextWithTypeAhead`:

1. Update the import to name the new hook (`useAutocompleteHintPaper`).
2. Update the call site to pass the new `{ limit, optionsLength, loading }` object; the `loading` variable is already present as a state on all three widgets.
3. No change to the JSX using `slots={ { paper: paperSlot } }` — that line is already present (JNG-6409).

No new widget prop is exposed to callers. No caller-side (container-template) change is required.

### (d) Set `noOptionsText` on the one non-freeSolo widget

For `SingleValueFilterComponent` (enum-branch Autocomplete only): add

```tsx
noOptionsText={t('judo.autocomplete.no-results', { defaultValue: 'No matching results' })}
```

Because the widget has no `freeSolo`, MUI's native rendering path takes over — `noOptionsText` displays whenever the user's typed filter matches zero enum labels. No Paper slot is needed. `useTranslation` and `t` are already in scope (used elsewhere in the file).

### What this change does NOT do

- Does **not** change **when** the empty state is shown on the enum filter. That remains MUI's own logic: `noOptionsText` renders whenever `options.length === 0` **and** the dropdown is open **and** `loading` is `false`.
- Does **not** modify the `loadingText` prop or add a localized "Loading…" — that is a separate concern and out of scope. (A follow-up ticket MAY address it symmetrically.)
- Does **not** overlap with the "more results" hint introduced by JNG-6409. The two features render at different times: the more-results hint requires `optionsLength >= limit`; the no-results hint requires `optionsLength === 0`. They are mutually exclusive by construction and rendered by the same Paper slot.
- Does **not** introduce any new widget prop, container-template plumbing, or Java helper.
- Does **not** modify the EMF UI model, catalogue, or any generator outside `judo-ui-react-template`.

## Capabilities

### Modified Capabilities

- **`input-widgets`** — one new MODIFIED requirement: "Autocomplete dropdowns display a localized empty-state message". Scenarios cover the three server-paginated widgets: message rendered when search returns zero options; message follows the active locale; message does not appear while `loading` is true.
- **`table-filters`** — one new MODIFIED requirement: "Enumeration filter autocomplete displays a localized empty-state message". Covers `SingleValueFilterComponent`, which was explicitly out of scope for JNG-6409 (its options are a finite enum array, not server-paginated) but IS in scope here because the user can still type a query that matches no enum label.
- **`relation-management`** — one new MODIFIED requirement: "Link and tag autocomplete dropdowns show a localized empty-state message" — covers the two relation-driven widgets from the caller-container perspective (no container change; only asserts the wired-through widget behaviour).

## Impact

- **`judo-ui-react/src/main/resources/actor/public/i18n/system_en-US.json.hbs`** — one new key. 1 line.
- **`judo-ui-react/src/main/resources/actor/public/i18n/system_hu-HU.json.hbs`** — one new key. 1 line.
- **`judo-ui-react/src/main/resources/actor/public/i18n/system_default.json.hbs`** — one new key. 1 line.
- **`judo-ui-react/src/main/resources/actor/src/components/widgets/AutocompleteMoreResultsHint.tsx.hbs`** — obsolete hook removed; `<AutocompleteMoreResultsHint>` FC unchanged. ~15 lines net.
- **`judo-ui-react/src/main/resources/actor/src/components/widgets/AutocompleteNoResultsHint.tsx.hbs`** — NEW file. `<AutocompleteNoResultsHint>` FC + composition hook `useAutocompleteHintPaper`. ~55 lines.
- **`judo-ui-react/src/main/resources/ui-react.yaml`** — one new template registry entry pointing at the new `.hbs` file so the generator emits it. 3 lines.
- **`judo-ui-react/src/main/resources/actor/src/components/widgets/SingleRelationInput.tsx.hbs`** — updated import (target file changed) + updated hook call. 2 lines changed.
- **`judo-ui-react/src/main/resources/actor/src/components/widgets/Tags.tsx.hbs`** — updated import (target file changed) + updated hook call. 2 lines changed.
- **`judo-ui-react/src/main/resources/actor/src/components/widgets/TextWithTypeAhead.tsx.hbs`** — updated import (target file changed) + updated hook call. 2 lines changed.
- **`judo-ui-react/src/main/resources/actor/src/components/table/SingleValueFilterComponent.tsx.hbs`** — one new `noOptionsText` prop on the enum-branch `<Autocomplete>`. 1 line.
- **Integration tests** (`judo-ui-react-itest/**`): no snapshot refresh. The committed snapshot set covers container-level and page-level `.tsx` files only — none of the four widgets, nor any `system_*.json`, has a snapshot counterpart, and no container template is modified. `judo-diff-checker-maven-plugin` reports zero drifts.
- **Downstream consumers**: minor contract change on the shared component — external consumers of `useAutocompleteMoreResultsHintPaper` (there are none outside the generator) would need to migrate to `useAutocompleteHintPaper`. Everything else is unchanged.
