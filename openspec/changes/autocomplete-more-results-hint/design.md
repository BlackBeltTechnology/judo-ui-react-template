## Decisions

### D1. Status footer rendered below the listbox via MUI 7's `slots.paper` (custom Paper component)

`renderOption` is rejected because it requires returning one DOM node **per option**; injecting a header there would either duplicate it on every render or require a sentinel option in the `options` array (which then participates in keyboard navigation and `isOptionEqualToValue`, both undesirable).

`slotProps.paper` is rejected as the prop-passing mechanism because of MUI issue [#43609](https://github.com/mui/material-ui/issues/43609) (verified 2026-06-30 via `code_search`) — it does not accept custom props that aren't on `PaperProps`, so we can't pass `showHint` / `limit` through it without type assertion hacks.

**Chosen approach**: use MUI 7's `slots.paper` to swap in a custom `Paper` component produced by the shared hook `useAutocompleteHintPaper` (which lives in `AutocompleteNoResultsHint.tsx.hbs` because MUI accepts only one `slots.paper` and both hints must share it). The Paper renders the more-results footer **after** `props.children` (which contains the listbox), so the footer sits visually below the option list.

```tsx
const paperSlot = useAutocompleteHintPaper({ limit: autoCompleteLimit, optionsLength: options.length, loading });

<Autocomplete
  // ...existing props...
  slots={ { paper: paperSlot } }
/>
```

Properties of the resulting footer:

- Is **not** an `<li role="option">`, so keyboard arrow navigation skips it, and it is a **sibling** of `ul[role=listbox]` rather than a child — ARIA APG requires a listbox to contain only `role="option"` elements (https://www.w3.org/WAI/ARIA/apg/patterns/combobox/).
- Inherits the paper's elevation so it visually belongs to the dropdown.
- Has `pointerEvents: 'none'` so it cannot be clicked or hovered like an option.
- Carries `role="status"` and is **not** `aria-hidden`. Truncation is real system status a screen-reader user needs; the documented pattern for result-count messaging is a live region (W3C `role="status"` search-results working example: https://www.w3.org/WAI/WCAG22/working-examples/aria-role-status-searchresults/ — and GOV.UK accessible-autocomplete, which does the same).
- Is **enclosed as its own region** (`borderTop: 1 divider` + a grey-ramp fill + `minHeight: 32`, `caption` / `text.secondary`, no italic). The container, not the font style, carries the "this is not an option" signal — Gestalt *common region*. See D7 for why the fill comes from the grey ramp and not from an `action.*` token.

**Revision 2026-08-14 (supersedes the 2026-06-30 above-the-list call).** The original decision put the hint above the list, in italic, on the reasoning that a footer under a scrollable list is easily missed. Shipped, that produced a row with the same left padding (`px: 2` = 16px, identical to MUI's option padding) and roughly the same height as an option, so it read as a **selectable first item** — violating Gestalt *similarity* and Nielsen H4/H8. The original objection turns out not to apply here: **the footer sits outside the scroll container.** `.MuiAutocomplete-listbox` owns the `max-height` + `overflow: auto`, and the footer is a sibling of it inside the Paper, so it stays pinned and visible no matter how far the option list is scrolled. Five candidates were compared in a served mockup; the footer status bar (the Slack / Linear / GitHub search convention) was selected.

**Why `useCallback` with an empty dependency list + `stateRef`**: MUI remounts the dropdown's paper subtree whenever the `slots.paper` component *identity* changes, which blurs the input mid-typing. An empty dep list pins the identity for the widget's lifetime; the render-time flags are then read through `stateRef.current`, which is reassigned on every render, so the pinned callback still sees current values. Depending on `[showHint]` instead would remount the paper the moment the hint appears or disappears.

**Fallback if `slots.paper` ever proves insufficient**: the legacy `PaperComponent` prop is still supported in MUI 7 and accepts the same custom Paper. Switching is a one-line edit. No further refactor required.

References (verified via `code_search` 2026-06-30):
- MUI Autocomplete API: `slots: { paper?: elementType }` and `slotProps: { paper?: func|object }` (https://mui.com/material-ui/api/autocomplete/)
- Overriding component structure: https://mui.com/material-ui/customization/overriding-component-structure/
- Issue #43609 (custom-props limitation on `slotProps.paper`): https://github.com/mui/material-ui/issues/43609

### D7. Footer fill is derived from `background.paper`, never from an `action.*` token or a fixed ramp step

The first implementation used `bgcolor: 'action.hover'`. Reported broken on 2026-08-14 ("the user barely sees the tip") and confirmed by measuring the generated theme's own palette (`background.paper: #ffffff`, `background.default: #fafafa`, `text.secondary: #434448`):

| Candidate fill | Resolves to | Separation from `background.paper` | Collides with hovered option? |
|---|---|---|---|
| `background.default` | `#fafafa` | 1.044:1 | no, but even fainter |
| `action.hover` (first impl) | `#f5f5f5` | **1.090:1** | **yes — byte-identical** |
| `action.selected` | `#ebebeb` | 1.192:1 | still an interaction state |
| `grey.300` (fixed ramp, also rejected — see below) | `#e0e0e0` | 1.320:1 | no |
| **`emphasize(background.paper, 0.12)` (chosen)** | `#e0e0e0` on the default paper | **1.320:1** | no |

Two independent defects in the original choice:

1. **Semantic collision.** `action.hover` *is* the fill MUI paints on an option under the pointer, so a static footer using it renders the exact same colour as a hovered row — the user cannot tell chrome from interaction feedback.
2. **Insufficient separation.** At 1.09:1 against the paper the band is effectively invisible, so the "enclosed region" signal that justifies dropping the italics never actually lands.

`background.default` is not the fix either: in this theme it is *lighter* than `action.hover` (1.044:1), so the recessed-surface token is useless here.

A fixed grey-ramp step (`grey.300` light / `grey.800` dark, selected on `theme.palette.mode`) was implemented first and also rejected, for two reasons discovered on review:

1. **`grey` is not part of this app's palette.** `palette.ts.hbs` defines only `primary`, `secondary`, `text.*`, `background.*` and `subtitleColor`, so `theme.palette.grey[…]` falls through to MUI's built-in default. The modeler's theme has no influence on it — it is a constant wearing a token's clothing.
2. **`mode` is not a reliable proxy for paper luminance.** `paletteThemeLight` hard-codes `mode: 'light'` but accepts an arbitrary modeler-supplied `application.theme.paperBackgroundColor`. Branching on `mode` therefore assumes something the model can contradict.

Measured failure modes of the fixed-ramp approach against a modeler-configured paper:

| Configured `background.paper` | Fixed ramp | Separation | `emphasize(paper, 0.12)` | Separation |
|---|---|---|---|---|
| `#ffffff` (default) | `#e0e0e0` | 1.32:1 ✓ | `#e0e0e0` | 1.32:1 ✓ |
| `#2a2a2a` (default dark) | `#424242` | 1.43:1 ✓ | `#434343` | 1.46:1 ✓ |
| `#e8e8e8` (light grey) | `#e0e0e0` | **1.08:1 — invisible again** | `#cccccc` | 1.31:1 ✓ |
| `#f5eedc` (beige) | `#e0e0e0` | **1.14:1 — barely visible** | `#d7d1c1` | 1.31:1 ✓ |
| `#1e1e1e` (dark paper, `mode` still `'light'`) | `#e0e0e0` | **12.63:1 — glaring white band** | `#393939` | 1.44:1 ✓ |

**Chosen**: derive the fill from the actual configured paper colour with MUI's `emphasize` helper (exported from `@mui/material/styles`, verified in 7.3.6):

```tsx
bgcolor: emphasize(theme.palette.background.paper, 0.12)
```

`emphasize` is `getLuminance(color) > 0.5 ? darken(color, k) : lighten(color, k)`, so it picks the correct direction from the colour itself and needs no `mode` branch. On the default light theme it evaluates to exactly `#e0e0e0` — pixel-identical to the reviewed-and-approved fixed-ramp rendering — while holding ~1.31–1.46:1 across every case above.

The derived-fill column was verified by executing the installed `@mui/system@7.3.6` `emphasize` directly, not by reimplementing its maths: `#ffffff → rgb(224,224,224)`, `#2a2a2a → rgb(67,67,67)`, `#e8e8e8 → rgb(204,204,204)`, `#f5eedc → rgb(215,209,193)`, `#1e1e1e → rgb(57,57,57)`.

**Known limitation**: `emphasize` is proportional, not target-seeking, so it cannot *guarantee* a contrast floor on a pathological paper colour (a mid-grey paper yields ~1.3:1, which is fine, but nothing enforces a minimum). A coefficient-stepping helper could guarantee a floor; that is more machinery than this one band warrants, and is not implemented.

**Known dark-mode caveat (pre-existing, out of scope).** The generated dark palette is marked `// WIP` and sets `text.secondary: #646464` against `background.paper: #2a2a2a`. That is 1.72:1 against the derived fill `#444444` — a WCAG-AA failure, and it fails on every candidate fill (1.70–2.72:1). This affects all secondary text in the app, not just this footer, so the fix belongs in `palette.ts.hbs`, not here. Light mode, which is what ships today, is 7.37:1 — comfortably AA.

### D2. Truncation heuristic: `options.length >= limit`

Chosen during planning (reporter clarification 2026-06-30, after an initial proposal that gated on a non-empty input string): show the footer whenever the returned page is full. Rationale:

- If `options.length < limit`, the server returned fewer rows than asked for → there cannot be more pages → no truncation possible → hide footer.
- If `options.length >= limit`, the user MAY benefit from narrowing the search. Showing the footer on initial open as well is intentional: the user gets the affordance from the first keystroke onward instead of having to discover it after typing.
- Strictly `>=` (not `===`) handles the rare race where the backend returns `limit + 1` to signal "has next page" — semantics-tolerant.

**Tracking the input string is therefore not required.** This simplifies every widget; no extra state, no extra `onInputChange` callback, and no risk of debounce mismatch between input-string updates and options updates.

### D3. New prop name: `autoCompleteLimit`, not `limitOptions`

Two reasons:
- `Tags.tsx.hbs` already exposes `limitOptions` with a fallback default and its own meaning ("how many to request"); reusing that name on the other three widgets risks suggesting the widget itself controls the page size, which is false for `SingleRelationInput` and `TextWithTypeAhead` (the limit is baked into the caller's query customizer / server action).
- `autoCompleteLimit` reads as "the limit applied to the autocomplete result you're going to receive, so you can use it for truncation detection". It is purely a **display-side** hint, not a fetch control.

Implementation note: in `Tags.tsx.hbs` the widget continues to drive the fetch from `limitOptions`; the truncation hint reads from the **same** value so the two cannot drift apart.

### D4. i18n key under the `judo.autocomplete.*` namespace

Existing keys in `system_*.json.hbs` cluster under `judo.<area>.<sub>` (e.g. `judo.action.*`, `judo.pages.*`, `judo.dialogs.*`). The autocomplete hint is a cross-cutting widget concern, not a per-page concern, so a new top-level area `judo.autocomplete.*` is justified rather than wedging the key into `judo.action.*`.

The default English string `"Showing the first {{limit}} results — narrow your search"` matches the JIRA ticket wording verbatim. The Hungarian translation uses three dots (ellipsis character `…`) to match the spacing used in other system messages such as `judo.security.loading-principal`.

### D5. Java helper symmetry: introduce `calculateTextAutocompleteRows(TextInput)`

The existing helper pair `calculateLinkAutocompleteRows(Link)` / `calculateTableAutocompleteRows(Table)` in `UiWidgetHelper.java` already establishes the "compute the limit for this autocomplete target" pattern. Adding a matching `calculateTextAutocompleteRows(TextInput)` keeps DRY (AGENTS.md rule #8) and avoids hard-coding `10` inside `textinput.hbs`.

If `TextInput` does not currently expose an `autoCompleteRows` attribute in the JUDO UI metamodel, the helper returns the same default constant as the link helper (`10`). Adding the attribute upstream is out of scope.

### D6. Snapshot churn is unavoidable

Every regenerated itest container that wires up a `SingleRelationInput`, `Tags`, `TextWithTypeAhead`, or `SingleValueFilterComponent` will emit a new `autoCompleteLimit={...}` prop. Every snapshot file under `judo-ui-react-itest/**/src/test/resources/snapshots/frontend-react/` that contains one of those components needs a one-time update. This is mechanical and expected; the diff-checker plugin (`judo-diff-checker-maven-plugin`) will list every drift on the first CI run, and the snapshot-refresh procedure documented in AGENTS.md "Important Notes" §5 covers it.

The widget files themselves are **not** snapshotted — verified via `find judo-ui-react-itest -path '*snapshots*' -name '*.tsx'`, which returns only container-level files. No widget-level snapshot refresh is required.

## Risks / Trade-offs

| Risk | Mitigation |
|---|---|
| The "list is full" heuristic produces a false positive when the data happens to contain exactly `limit` matches. | The hint text says "Type for more results" (advisory) rather than "Hidden results exist" (assertive). False positives cost the user a useless keystroke; a false negative (silently hidden rows, current state) costs them the row. |
| MUI 7's `slotProps.paper` rendering may differ across MUI minor versions. | Pin the implementation to the `slotProps.paper.children` slot — covered by both MUI 7.x and a forward-compat path that falls back to `PaperComponent` if needed. Smoke-tested in at least one itest before merge. |
| Tags widget's existing `limitOptions` default (`10`) and `calculateLinkAutocompleteRows` default (`10`) drift in the future. | A test in the spec scenarios fixes both at `10` for the integration baselines; if either default changes, the spec scenarios will need an explicit update — which is the desired behaviour. |
| Screen readers may announce the footer as part of the listbox. | The footer is rendered outside the `role="listbox"` element (a sibling of it inside the paper), so it never enters the active-descendant chain. It is intentionally announced — via `role="status"` — because truncation is status the user needs; it is not silenced with `aria-hidden`. |

## Non-goals (locked)

1. No "Load next page" interactive footer.
2. No paginated infinite-scroll behaviour.
3. No counter ("11 of 25 matches"). The server does not currently return the total count alongside the page; obtaining it would require a separate count query per keystroke, which is far heavier than the cost of the bug we're fixing.
4. No change to the seed itest data. The new behaviour is verified by a manual reproduction (documented in `tasks.md` §0) and by snapshot diffs of the four widget files.
