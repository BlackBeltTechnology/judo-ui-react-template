## Decisions

### D1. Reuse (and extend) JNG-6409's `slots.paper` hook, not MUI's `noOptionsText` prop

The obvious first approach — pass `noOptionsText={t('judo.autocomplete.no-results', …)}` to every `<Autocomplete>` — is a **no-op on three of the four widgets** because they all set `freeSolo={true}`. MUI's Autocomplete rendering path deliberately guards the built-in "no options" element with `!freeSolo`, so under freeSolo the text is never inserted into the DOM. Documented behaviour, refused-by-design across issues [#18985](https://github.com/mui-org/material-ui/issues/18985), [#37862](https://github.com/mui/material-ui/issues/37862), [#45576](https://github.com/mui/material-ui/issues/45576); verified live 2026-07-09 by running the ActionGroupTest generator with `noOptionsText` set and observing no message on the Astronomer `SingleRelationInput` under either locale.

The only reliable injection point for freeSolo widgets is the same one JNG-6409 already uses: **the `slots.paper` prop**. MUI still renders the Paper wrapper (even under freeSolo with empty options) — only the "no options" element inside it is suppressed. Injecting our own hint node above `paperProps.children` bypasses the guard cleanly.

Therefore JNG-6410 introduces a **new sibling file** `AutocompleteNoResultsHint.tsx.hbs`, mirroring JNG-6409's structural convention (one hint = one file):

1. `<AutocompleteNoResultsHint>` FC renders the localized "No matching results" text.
2. `useAutocompleteHintPaper({ limit, optionsLength, loading })` hook composes both hints in one memoized Paper. It imports `<AutocompleteMoreResultsHint>` from its sibling file and renders whichever hint applies: no-results when `!loading && optionsLength === 0`, more-results when `optionsLength >= limit > 0` (mutually exclusive).
3. The JNG-6409 file keeps its FC unchanged; its now-obsolete `useAutocompleteMoreResultsHintPaper` hook is removed (subsumed by the new composition hook; the old hook had no external callers).

For the one non-freeSolo widget — `SingleValueFilterComponent`'s enumeration branch — MUI's native `noOptionsText` prop works fine and is used directly. Introducing the Paper hook there would be dead ceremony (its option source is a finite local enum array; MUI's built-in filter drives the visible count).

Rejected alternatives:
- **`noOptionsText` prop on every widget.** Silent no-op on 3 of 4 widgets. Discovered only via live verification. This was the original design.md D1 and had to be revised.
- **Sentinel option (inject `{ label: 'No matching results', disabled: true }` when the fetch returns zero rows).** Works around the freeSolo guard but pollutes `options` semantics: the value flows through `filterOptions`, `getOptionLabel`, `onChange`, etc. Risks accidental selection and keyboard-navigation focus on the sentinel. Rejected as fragile.
- **`loading={optionsLength === 0} loadingText={t(...)}`.** Would leave a spinner glued next to the "No matching results" text and keeps `loading` state permanently on when the query is empty — semantically wrong.
- **Controlled `open` prop + custom empty state.** Forces the popper open even under freeSolo with empty options, letting the built-in `noOptionsText` render. Rejected because it would remove MUI's autoclose semantics on blur/escape from all three widgets — a much larger behaviour change than the Paper-slot approach.

Verified 2026-07-09 against ActionGroupTest: with the Paper-slot approach in place, "No matching results" renders on the Astronomer `SingleRelationInput` immediately after the empty search settles (user screenshot attached to JIRA JNG-6410).

### D2. Key naming: `judo.autocomplete.no-results`

Sits inside the `judo.autocomplete.*` namespace created by JNG-6409 (`judo.autocomplete.type-for-more-results`). Alternatives considered and rejected:

- `judo.autocomplete.no-options` — literal mirror of MUI's default. Rejected because "options" is MUI-internal jargon; end users think in terms of *results* / *matches*.
- `judo.autocomplete.empty` — too generic; would collide with any future "empty state" strings.
- `judo.form.no-results` or `judo.action.no-results` — cross-cuts multiple widgets; better to keep alongside the sibling `type-for-more-results` under one autocomplete area.

The English default `"No matching results"` was chosen (rather than parroting MUI's `"No options"`) because it is friendlier and mirrors typical enterprise-app copy. Hungarian value `"Nincs találat"` is the canonical short form used across JUDO's own admin frontends (verified visually in existing hu-HU translations for related empty-state messages).

### D3. All four Autocomplete-hosting widgets are in scope, unlike JNG-6409

JNG-6409 explicitly excluded `SingleValueFilterComponent` because its options are a **finite local enum array** — it cannot be truncated, so the "more results" hint is meaningless there. For the no-results hint the calculus is different: the user types free text into the filter's autocomplete and MUI filters the enum locally; a query like `"xyz"` against a real-world enum easily produces an empty match set, and MUI shows `"No options"` in English. Therefore `SingleValueFilterComponent` **IS** in scope for JNG-6410. Verified 2026-07-08 by reading the file (line 165 Autocomplete had no `noOptionsText`); implementation adds the prop directly (see D1 — no Paper slot needed there because the widget has no `freeSolo`).

### D4. `loading` gating is expressed by the hook, not left to MUI

For `SingleValueFilterComponent` (non-freeSolo, native `noOptionsText`) MUI handles the loading gate automatically: it renders `loadingText` while `loading === true` and only transitions to `noOptionsText` once the fetch settles. No extra work required there.

For the three freeSolo widgets, our custom hint component sits above `paperProps.children` and would render immediately when the user starts typing — including during the in-flight fetch — unless we gate it ourselves. The hook therefore accepts a `loading?: boolean` parameter and suppresses the no-results hint while `loading === true`. All three call sites already maintain a `loading` state variable and pass it through.

`loadingText` remains untranslated in this change; localizing it is a follow-up (see proposal Non-goals).

### D5. No new widget prop; the message is unconditional

Unlike JNG-6409's `autoCompleteLimit`, the empty message has no per-caller variance to express. Every Autocomplete in every generated app wants the same localized string. Adding an opt-in prop would be dead ceremony. Callers get the localized behaviour automatically on regeneration.

### D6. No snapshot churn

Every regenerated widget file gains one changed hook call (three files) or one `noOptionsText` prop (one file), and every regenerated `system_*.json` gains one key — but none of those artifacts has a committed snapshot. Verified via `find judo-ui-react-itest -path '*snapshots*'`: the snapshot set covers container-level and page-level `.tsx` files only, so neither the new `AutocompleteNoResultsHint.tsx` nor any `system_*.json` is snapshotted. Container-level snapshots do **not** change either, because no container template is modified. Net snapshot refresh for this change: zero files.

## Risks / Trade-offs

| Risk | Mitigation |
|---|---|
| Translation key clashes with a future MUI i18n contract. | The key lives in the `judo.*` namespace, which the app owns end-to-end. No collision surface with MUI internals. |
| A locale falls back to English silently if the translation file is missing. | The `defaultValue` passed to `t(...)` guarantees the English fallback renders instead of the raw key. |
| Consumers of the previous `useAutocompleteMoreResultsHintPaper(limit, optionsLength)` API break on regen. | The only three call sites live inside this generator and are updated in the same change. External callers to a JNG-6409-only API do not exist. |
| MUI updates the freeSolo guard in a future minor version, causing our Paper hint to render **alongside** MUI's built-in `noOptionsText`. | Both surfaces would show localized text; visual duplication is the worst case. Detected at snapshot-refresh time. Mitigation deferred until the MUI change actually lands. |
| The Paper hook renders both hints on freeSolo widgets even when the popper would otherwise not open. | Verified 2026-07-09: MUI still mounts the Paper on freeSolo with empty options; our hint therefore renders as expected. If a future MUI version stops mounting the Paper on empty freeSolo Autocompletes (as PR #41300 briefly did before revert), we would need to switch to a controlled-open approach. |

## Non-goals (locked)

1. No change to `loadingText`. (Separate follow-up.)
2. No new widget-level opt-in prop.
3. No change to when the message appears — `!loading && optionsLength === 0` for the freeSolo widgets; MUI's built-in gate for `SingleValueFilterComponent`.
4. No change to the JNG-6409 more-results hint behaviour. The two hints coexist in the same Paper slot but are mutually exclusive by construction.
