## Decisions

### D1. Truncation signalled by a boolean, not by a count

Chosen: `{ items: string[]; truncated: boolean }`.

Rejected: `{ items: string[]; limit: number }`, `{ items: string[]; total: number }`, `{ items: string[]; hasMore: boolean }`.

- `truncated` boolean carries exactly the information the frontend needs to decide whether to render `<AutocompleteMoreResultsHint />`. Nothing more, nothing less. Principle of least privilege.
- `limit` would leak an internal backend detail (the actual cap) into the frontend, and requires the frontend to redo `items.length >= limit` — reintroducing the exact desync surface this proposal removes.
- `total` (returning the full match count) is expensive: the backend would have to run a separate `COUNT(*)` query per keystroke.
- `hasMore` (Relay-style) reads as pagination-oriented and invites future creep toward "load next page" — explicitly out of scope per `autocomplete-more-results-hint/design.md` non-goal #1.

`truncated` is the smallest possible signal that fully captures the semantic. Any modeler-side implementation can compute it in whichever way is natural (typically `LIMIT N+1` and check for overflow, or check the DB driver's `hasNext()` cursor, etc.).

### D2. Backwards-compatibility strategy: hard break with clear error

Chosen: change the return type outright. Do not accept `string[]` as a fallback via a union.

Rationale:

- A union type (`Promise<string[] | AutocompleteOptionsResult>`) forces the widget to branch at runtime and treat legacy returns as `truncated: false` — reintroducing silent truncation for old consumers, which is the exact defect this proposal fixes.
- Verified 2026-07-02 that no reachable consumer implements the old signature (see proposal `Consumer migration` section). The break costs nothing today.
- A TypeScript compile error at the exact call site is far better guidance for external consumers than a silent behavioural regression.

### D3. Where the type lives

Chosen: export `AutocompleteOptionsResult` from `actor/src/components/widgets/TextWithTypeAhead.tsx.hbs` alongside the widget's other public types.

Rejected: putting it in `actor/src/containers/types.ts.hbs` next to `get<Attr>Options`, or in a new `actor/src/components/widgets/autocomplete-types.ts.hbs`.

- `types.ts.hbs` is generated per-container and per-actor, so the same type would be re-declared many times — modelers can't `import { AutocompleteOptionsResult } from '...'` in a stable location.
- A separate `autocomplete-types.ts.hbs` file adds a whole template for one type. `TextWithTypeAhead.tsx.hbs` already exports component-related types and is the primary "widget for typeahead" module; the type belongs with its consumer.

Consumer migration writes: `import { AutocompleteOptionsResult } from '@/components/widgets/TextWithTypeAhead';`

### D4. Removal, not deprecation, of `calculateTextAutocompleteRows` and its annotation

The helper and its `judo-ui-react::autoCompleteRows=<N>` annotation were introduced as a JNG-6409 stopgap. Under this proposal, they:

- Have no consumer (the widget stops reading `autoCompleteLimit`).
- Would encode a value that the frontend intentionally ignores.
- Would tempt future modelers to configure a number that has no effect, silently.

Keeping the helper "just in case" would create documentation debt. Removal is safer. If a future need arises to pass a limit to `get<Attr>Options`, a new helper with a different name (e.g. `getTextTypeaheadBackendPageSize`) can be introduced with clear semantics; nothing about this design blocks that.

### D5. No changes to Link or Table autocompletes

Explicitly locked. The two Link-driven widgets (`SingleRelationInput`, `Tags`) already have structurally-guaranteed sync because the same helper drives both the backend `_seek.limit` and the frontend threshold. There is no defect to fix on that side. Widening their contracts in parallel would double the surface area of this change for no user-visible improvement.

The `calculateLinkAutocompleteRows` / `calculateTableAutocompleteRows` helpers and the `SingleRelationInput.autoCompleteLimit` / `Tags.limitOptions` props are unchanged.

### D6. Ref-based `truncated` capture inside the widget

Same shape as the current `showMoreResultsHintRef` trick documented in `autocomplete-more-results-hint/design.md` §D1: read from a ref so the memoised custom `Paper` component keeps a stable identity across renders and MUI does not remount the paper subtree on every result change.

```tsx
const truncatedRef = useRef(false);
truncatedRef.current = result?.truncated ?? false;

const PaperWithHint = useCallback((paperProps: PaperProps) => (
  <Paper {...paperProps}>
    {truncatedRef.current && <AutocompleteMoreResultsHint />}
    {paperProps.children}
  </Paper>
), []);
```

Verified pattern; no MUI-version surprises expected.

## Risks / Trade-offs

| Risk | Mitigation |
|---|---|
| External customer projects have hand-written `get<Attr>Options` returning `Promise<string[]>` (invisible to this workspace). | TypeScript compile error at their next generation directly points at the required change. Short migration note in changelog. `git grep` and release notes call out the signature widening. |
| A backend implementation forgets to set `truncated: true` when it caps its result. | Same class of bug as any hand-written implementation forgetting a correctness detail — but now the failure is **local** (backend author sees the field they didn't set), not **coupled** (frontend and backend both configured separately). Documentation and a template-emitted JSDoc reminder on the `get<Attr>Options` type reduce this to routine review. |
| Some consumer wants the raw `string[]` shape for their own reasons. | The proposal exports `AutocompleteOptionsResult` — trivially destructured. If a consumer wants a pre-truncation shape, they can wrap: `async () => ({ items: await legacy(), truncated: false })`. |
| Future need to know the actual limit on the frontend (analytics, telemetry, adaptive UI). | Adding a second field to `AutocompleteOptionsResult` later is a purely-additive change. Not blocked by this design. |

## Non-goals (locked)

1. Do not change the two Link-driven widgets.
2. Do not add "load more" / infinite scroll.
3. Do not add a counter like "showing 8 of ??".
4. Do not introduce a `judo-meta-ui` metamodel change.
5. Do not preserve the JNG-6409 `judo-ui-react::autoCompleteRows` annotation for backwards compatibility — its semantics under this design are actively misleading.
