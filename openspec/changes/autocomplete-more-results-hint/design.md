## Decisions

### D1. Header rendered above the listbox via MUI 7's `slots.paper` (custom Paper component)

`renderOption` is rejected because it requires returning one DOM node **per option**; injecting a header there would either duplicate it on every render or require a sentinel option in the `options` array (which then participates in keyboard navigation and `isOptionEqualToValue`, both undesirable).

`slotProps.paper` is rejected as the prop-passing mechanism because of MUI issue [#43609](https://github.com/mui/material-ui/issues/43609) (verified 2026-06-30 via `code_search`) — it does not accept custom props that aren't on `PaperProps`, so we can't pass `showHint` / `limit` through it without type assertion hacks.

**Chosen approach**: use MUI 7's `slots.paper` to swap in a custom `Paper` component built **inline at the call site of each widget**, so it closes over `options.length` and `autoCompleteLimit` via the React scope chain. The Paper renders the header **before** `props.children` (which contains the listbox) so the header sits visually above the option list.

Verified MUI 7 pattern (from `code_search` 2026-06-30, `https://mui.com/material-ui/api/autocomplete/`):

```tsx
// Inside the widget body, computed each render so it closes over the live values:
const showHint = typeof autoCompleteLimit === 'number' && autoCompleteLimit > 0 && options.length >= autoCompleteLimit;
const PaperWithHint = useCallback((paperProps: PaperProps) => (
  <Paper {...paperProps}>
    {showHint && <AutocompleteMoreResultsHint />}
    {paperProps.children}
  </Paper>
), [showHint]);

<Autocomplete
  // ...existing props...
  slots={{ paper: PaperWithHint }}
/>
```

Properties of the resulting header:

- Is **not** an `<li role="option">`, so keyboard arrow navigation skips it.
- Inherits the paper's elevation and padding so it visually belongs to the dropdown.
- Has `pointerEvents: 'none'` and `aria-hidden="true"` (set inside `AutocompleteMoreResultsHint`) to keep it informational only.
- Sits **above** the option list so the user reads it before scanning the options — chosen by the reporter on 2026-06-30 in preference to a footer below the list (footers below a scrollable list are often invisible without scrolling).
- Renders the text in **italic** typography (CSS `font-style: italic`) so the user reads it as a tip rather than a selectable option — added by reporter clarification 2026-06-30.

**Why `useCallback`**: without memoisation, the Paper component identity changes on every render, which causes MUI to remount the dropdown's paper subtree on every keystroke (flicker, focus loss). With `useCallback`, the identity is stable across renders where `showHint` does not change.

**Fallback if `slots.paper` ever proves insufficient**: the legacy `PaperComponent` prop is still supported in MUI 7 and accepts the same custom Paper. Switching is a one-line edit. No further refactor required.

References (verified via `code_search` 2026-06-30):
- MUI Autocomplete API: `slots: { paper?: elementType }` and `slotProps: { paper?: func|object }` (https://mui.com/material-ui/api/autocomplete/)
- Overriding component structure: https://mui.com/material-ui/customization/overriding-component-structure/
- Issue #43609 (custom-props limitation on `slotProps.paper`): https://github.com/mui/material-ui/issues/43609

### D2. Truncation heuristic: `options.length >= limit`

Chosen during planning (reporter clarification 2026-06-30, after an initial proposal that gated on a non-empty input string): show the header whenever the returned page is full. Rationale:

- If `options.length < limit`, the server returned fewer rows than asked for → there cannot be more pages → no truncation possible → hide header.
- If `options.length >= limit`, the user MAY benefit from narrowing the search. Showing the header on initial open as well is intentional: the user gets the affordance from the first keystroke onward instead of having to discover it after typing.
- Strictly `>=` (not `===`) handles the rare race where the backend returns `limit + 1` to signal "has next page" — semantics-tolerant.

**Tracking the input string is therefore not required.** This simplifies every widget; no extra state, no extra `onInputChange` callback, and no risk of debounce mismatch between input-string updates and options updates.

### D3. New prop name: `autoCompleteLimit`, not `limitOptions`

Two reasons:
- `Tags.tsx.hbs` already exposes `limitOptions` with a fallback default and its own meaning ("how many to request"); reusing that name on the other three widgets risks suggesting the widget itself controls the page size, which is false for `SingleRelationInput` and `TextWithTypeAhead` (the limit is baked into the caller's query customizer / server action).
- `autoCompleteLimit` reads as "the limit applied to the autocomplete result you're going to receive, so you can use it for truncation detection". It is purely a **display-side** hint, not a fetch control.

Implementation note: in `Tags.tsx.hbs` the widget continues to drive the fetch from `limitOptions`; the truncation hint reads from the **same** value so the two cannot drift apart.

### D4. i18n key under the `judo.autocomplete.*` namespace

Existing keys in `system_*.json.hbs` cluster under `judo.<area>.<sub>` (e.g. `judo.action.*`, `judo.pages.*`, `judo.dialogs.*`). The autocomplete footer is a cross-cutting widget concern, not a per-page concern, so a new top-level area `judo.autocomplete.*` is justified rather than wedging the key into `judo.action.*`.

The default English string `"Type for more results…"` matches the JIRA ticket wording verbatim. The Hungarian translation uses three dots (ellipsis character `…`) to match the spacing used in other system messages such as `judo.security.loading-principal`.

### D5. Java helper symmetry: introduce `calculateTextAutocompleteRows(TextInput)`

The existing helper pair `calculateLinkAutocompleteRows(Link)` / `calculateTableAutocompleteRows(Table)` in `UiWidgetHelper.java` already establishes the "compute the limit for this autocomplete target" pattern. Adding a matching `calculateTextAutocompleteRows(TextInput)` keeps DRY (AGENTS.md rule #8) and avoids hard-coding `10` inside `textinput.hbs`.

If `TextInput` does not currently expose an `autoCompleteRows` attribute in the JUDO UI metamodel, the helper returns the same default constant as the link helper (`10`). Adding the attribute upstream is out of scope.

### D6. Snapshot churn is unavoidable

Every regenerated itest container that wires up a `SingleRelationInput`, `Tags`, `TextWithTypeAhead`, or `SingleValueFilterComponent` will emit a new `autoCompleteLimit={...}` prop. Every snapshot file under `judo-ui-react-itest/**/src/test/resources/snapshots/frontend-react/` that contains one of those components needs a one-time update. This is mechanical and expected; the diff-checker plugin (`judo-diff-checker-maven-plugin`) will list every drift on the first CI run, and the snapshot-refresh procedure documented in AGENTS.md "Important Notes" §5 covers it.

The four widget files themselves are also snapshotted; their snapshots gain the new `autoCompleteLimit` prop in the interface plus the new footer in the JSX.

## Risks / Trade-offs

| Risk | Mitigation |
|---|---|
| The "list is full" heuristic produces a false positive when the data happens to contain exactly `limit` matches. | The footer text says "Type for more results" (advisory) rather than "Hidden results exist" (assertive). False positives cost the user a useless keystroke; a false negative (silently hidden rows, current state) costs them the row. |
| MUI 7's `slotProps.paper` rendering may differ across MUI minor versions. | Pin the implementation to the `slotProps.paper.children` slot — covered by both MUI 7.x and a forward-compat path that falls back to `PaperComponent` if needed. Smoke-tested in at least one itest before merge. |
| Tags widget's existing `limitOptions` default (`10`) and `calculateLinkAutocompleteRows` default (`10`) drift in the future. | A test in the spec scenarios fixes both at `10` for the integration baselines; if either default changes, the spec scenarios will need an explicit update — which is the desired behaviour. |
| Screen readers may announce the header as part of the listbox. | Header is rendered outside the `role="listbox"` element (inside the paper, above the listbox). `aria-hidden="true"` is set to keep the announcement out of the active descendant chain. |

## Non-goals (locked)

1. No "Load next page" interactive footer.
2. No paginated infinite-scroll behaviour.
3. No counter ("11 of 25 matches"). The server does not currently return the total count alongside the page; obtaining it would require a separate count query per keystroke, which is far heavier than the cost of the bug we're fixing.
4. No change to the seed itest data. The new behaviour is verified by a manual reproduction (documented in `tasks.md` §0) and by snapshot diffs of the four widget files.
