## Why

Every generator-emitted MUI `<Autocomplete>` that requests a **server-paginated** page of options gives the user **no signal that the list was truncated** when the page comes back full. The three in-scope widgets, all confirmed by `grep`/`Read` on 2026-06-30:

| Widget | Template | Limit source | Caller |
|---|---|---|---|
| Single-relation picker | `actor/src/components/widgets/SingleRelationInput.tsx.hbs` | `_seek.limit = {{ calculateLinkAutocompleteRows link }}` (Java helper, defaults to `10`) | `actor/src/containers/components/link/index.tsx.hbs:154` |
| Multi-relation tag picker | `actor/src/components/widgets/Tags.tsx.hbs` | `limitOptions` prop, defaults to `10` (line 82); widget builds `_seek.limit` (line 113) | `actor/src/containers/components/tag/index.tsx.hbs:88` |
| Type-ahead text input | `actor/src/components/widgets/TextWithTypeAhead.tsx.hbs` | Limit lives in the server-side `get{Attr}Options` action; widget receives an opaque `string[]` | `actor/src/containers/widget-fragments/textinput.hbs:23` |

`SingleValueFilterComponent.tsx.hbs` is **out of scope** — its only MUI `<Autocomplete>` (line 165) is bound to `filter.filterOption.enumValues`, a finite locally-supplied array with no server pagination and therefore no truncation possible. Verified by reading the file on 2026-06-30.

Reproduced live against `RelationTest/Actor` on 2026-06-30: with 12 `TransferObjectC` rows whose `field` contains `c`, the **SingleAggregationAssociation** autocomplete on `TransferObjectA → First A → Single` returns exactly 10 options (`C-extra-01 … C-extra-10`). `Giga C` and `C-extra-11` are silently dropped. The user has no way to discover that a more specific query would surface them.

This is a one-line UX defect with concrete user impact: in any deployed app with more rows than the autocomplete limit, the user **cannot find rows whose attribute value sorts after the page boundary** without already knowing the value. This frequently leads to false "the row doesn't exist" conclusions and / or to users abandoning the autocomplete and falling back to the selector dialog.

## What Changes

Single Class III (react-template-only) change. The fix is conservative: a non-selectable **header** item is rendered **above** the option list inside the MUI `<Autocomplete>` dropdown **whenever the returned page is full** — i.e. `options.length >= limit`. It does not depend on whether the user has typed anything; on initial open with a full first page the hint already advises the user to type to narrow the result set. The hint disappears once the user has narrowed the search enough that the server returned fewer than `limit` rows.

The hint text comes from a new system-i18n key (`judo.autocomplete.showing-first-results`), reports the caller-supplied limit via a `{{limit}}` interpolation, and is themable. It renders as an enclosed status footer below the listbox — a top divider plus a tinted background, rather than italics, carry the "this is not an option" signal.

### (a) Shared header component + slot hook

- New file `actor/src/components/widgets/AutocompleteMoreResultsHint.tsx.hbs` exports two symbols:
  - `AutocompleteMoreResultsHint` — a small React FC taking a `limit` prop that renders a non-interactive status footer with `pointerEvents: 'none'`, `role="status"`, `borderTop`, `bgcolor: 'action.hover'` and `minHeight: 32`. Receives its label via i18n (`t('judo.autocomplete.showing-first-results', { limit, defaultValue: 'Showing the first {{limit}} results — narrow your search' })`). It is deliberately NOT `aria-hidden`: truncation is real system status a screen-reader user needs (per the W3C `role="status"` search-results working example and GOV.UK accessible-autocomplete).
  - `useAutocompleteMoreResultsHintPaper(limit, optionsLength)` — a hook that returns a stable-identity `paper` slot component for MUI Autocomplete. It encapsulates the visibility calculation (`optionsLength >= limit`), the ref-based latest-value read (so MUI does not remount the paper on threshold crossings), and the `<Paper>` wrapper that prepends `<AutocompleteMoreResultsHint />` above the listbox. This is the single source of truth for the hint machinery; the three widget call sites use it verbatim.
- Re-exported from `actor/src/components/widgets/index.tsx.hbs`.
- Post-review note (2026-07-08, gaborflorian review nit): the hook was extracted from an initial inline-in-each-widget draft into `AutocompleteMoreResultsHint.tsx.hbs` to eliminate the ~9-line duplicated block across the three widgets. Behaviour is unchanged. See `tasks.md` §10.

### (b) Plumb the limit through every server-paginated autocomplete widget

For each of the three widget templates, add a new optional prop `autoCompleteLimit?: number` (or reuse `Tags.tsx.hbs`'s existing `limitOptions`), pass it from the caller container template, then call `useAutocompleteMoreResultsHintPaper` and hand its return value to `<Autocomplete slots={ { paper: paperSlot } } />`. The exact pattern is pinned in `design.md` §D1.

- `SingleRelationInput.tsx.hbs` — new `autoCompleteLimit` prop; `link/index.tsx.hbs` passes `{{ calculateLinkAutocompleteRows link }}`.
- `Tags.tsx.hbs` — uses its existing `limitOptions` (default `10`); no new prop, no container plumbing.
- `TextWithTypeAhead.tsx.hbs` — new `autoCompleteLimit` prop; `textinput.hbs` passes `{{ calculateTextAutocompleteRows child }}`. The `TextInput` EClass in `ui.ecore` exposes `isTypeAheadField` but **no** `autoCompleteRows` attribute (verified 2026-06-30 against `/home/balazs/judo-ng/models/judo-meta-ui/model/model/ui.ecore`), so the new helper `UiWidgetHelper.calculateTextAutocompleteRows(TextInput)` is introduced solely for symmetry with `calculateLinkAutocompleteRows` and returns the constant `10` unconditionally.

### (c) New i18n key

- `actor/public/i18n/system_en-US.json.hbs`: `"judo.autocomplete.showing-first-results": "Showing the first {{limit}} results — narrow your search"`
- `actor/public/i18n/system_hu-HU.json.hbs`: `"judo.autocomplete.showing-first-results": "Az első {{limit}} találat látható — szűkítse a keresést"`
- `actor/public/i18n/system_default.json.hbs`: same as `en-US`.

### What this change does NOT do

- Does **not** raise or lower the server-side page limit. `calculateLinkAutocompleteRows` continues to return `10` by default; any model-driven override (`Link.autoCompleteRows`) continues to take effect unchanged.
- Does **not** add a "load more" / paginate-on-scroll behaviour. The hint is informational only. (A future change MAY upgrade it into an interactive "load next page" button; out of scope here per AGENTS.md rule #4 "simplicity".)
- Does **not** modify the EMF UI model, the catalogue, or any generator that lives outside `judo-ui-react-template`.
- Does **not** change MUI's free-solo / clear-on-blur / read-only semantics on any of the three widgets.
- Does **not** depend on the search input string. The hint shows on initial open if the first batch fills the limit, and stays visible until the user narrows the search enough that the server returns fewer than `limit` options.
- Does **not** touch `SingleValueFilterComponent.tsx.hbs` (out of scope; finite-enum autocomplete with no pagination).

## Capabilities

### Modified Capabilities

- **`input-widgets`** — one new MODIFIED requirement: "Autocomplete dropdowns hint when the result list is truncated". Scenarios: header visible whenever returned options reach the limit (regardless of search input); header hidden when fewer options are returned; header rendered in italic and outside the listbox role.
- **`relation-management`** — one new MODIFIED requirement: "Link autocomplete dropdown surfaces truncation hint" (touches `SingleRelationInput` + `Tags` wiring through the link/tag container templates).

## Impact

- **`judo-ui-react/src/main/resources/actor/src/components/widgets/AutocompleteMoreResultsHint.tsx.hbs`** — new file (~50 lines): FC + `useAutocompleteMoreResultsHintPaper` hook.
- **`judo-ui-react/src/main/resources/actor/src/components/widgets/index.tsx.hbs`** — one new re-export.
- **`judo-ui-react/src/main/resources/actor/src/components/widgets/SingleRelationInput.tsx.hbs`** — new `autoCompleteLimit?` prop; one call to `useAutocompleteMoreResultsHintPaper(autoCompleteLimit, options.length)` + `slots={ { paper: paperSlot } }` on the `<Autocomplete>`. ~3 net-added lines (import + hook call + slots prop).
- **`judo-ui-react/src/main/resources/actor/src/components/widgets/Tags.tsx.hbs`** — same hook call using existing `limitOptions`. ~3 net-added lines.
- **`judo-ui-react/src/main/resources/actor/src/components/widgets/TextWithTypeAhead.tsx.hbs`** — new `autoCompleteLimit?` prop; same hook call. ~3 net-added lines.
- **`judo-ui-react/src/main/resources/actor/src/containers/components/link/index.tsx.hbs`** — pass `autoCompleteLimit={ {{ calculateLinkAutocompleteRows link }} }`. ~1 line.
- **`judo-ui-react/src/main/resources/actor/src/containers/widget-fragments/textinput.hbs`** — pass `autoCompleteLimit={ {{ calculateTextAutocompleteRows child }} }`. ~1 line.
- **`judo-ui-react/src/main/java/hu/blackbelt/judo/ui/generator/react/UiWidgetHelper.java`** — new static helper `calculateTextAutocompleteRows(TextInput)` returning constant `10`. ~6 lines.
- **`judo-ui-react/src/main/resources/actor/public/i18n/system_en-US.json.hbs`**, **`system_hu-HU.json.hbs`**, **`system_default.json.hbs`** — one new key each. 3 lines.
- **Integration tests** (`judo-ui-react-itest/**`): regeneration will introduce the new prop and the new i18n key into every regenerated frontend. The six committed snapshot files under `src/test/resources/snapshots/frontend-react/` that reference an autocomplete widget need a one-time mechanical refresh (an explicit `bash` one-liner is provided in `tasks.md` §7.2). The diff-checker plugin (`judo-diff-checker-maven-plugin`) will list every drift on the first CI run.
- **Downstream consumers**: zero contract change. The new prop is optional; the header is purely additive in the rendered DOM. Existing Playwright selectors targeting `option` elements continue to work — the header is rendered with `role="presentation"` (no `role="option"`).
