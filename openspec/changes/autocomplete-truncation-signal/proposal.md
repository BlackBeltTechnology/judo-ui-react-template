## Why

The just-shipped `autocomplete-more-results-hint` change (JNG-6409) makes the "Type for more results…" hint appear when `options.length >= autoCompleteLimit`. For the two **Link-driven** widgets (`SingleRelationInput`, `Tags`) this is structurally sound: both the backend page size (`_seek.limit`) and the frontend threshold (`autoCompleteLimit`) are emitted from the **same** call to `UiWidgetHelper.calculateLinkAutocompleteRows(link)` — desync is impossible by construction.

For the **TextInput type-ahead** widget the situation is asymmetric. Confirmed by reading the templates on 2026-07-02:

- `actor/src/containers/widget-fragments/textinput.hbs:51-57` emits:
  ```jsx
  autoCompleteLimit={ {{ calculateTextAutocompleteRows child }} }
  onAutoCompleteSearch={ async (searchText: string) => {
      if (actions?.get{{ firstToUpper child.attributeType.name }}Options) {
          return await actions.get{{ firstToUpper child.attributeType.name }}Options(searchText, data, editMode, validation);
      }
      return Promise.resolve([]);
  } }
  ```
- `actor/src/containers/types.ts.hbs:64` declares:
  ```ts
  get<Attr>Options?: (searchText: string, data: <Container>, editMode: boolean, validation: Map<...>) => Promise<string[]>;
  ```

The template controls **only** the frontend hint threshold. The actual row count comes from a modeler-supplied Pandino DI hook (`actions.get<Attr>Options`) which has:
1. No `limit` parameter to know what threshold the frontend uses.
2. No way to signal to the frontend whether it truncated its return.

Under the interim design shipped in JNG-6409, the `judo-ui-react::autoCompleteRows=<N>` annotation on `TextInput` sets **only** the frontend threshold. The correctness of the hint depends on the modeler manually keeping the annotation value in sync with whatever limit their `get<Attr>Options` implementation happens to enforce. Concrete failure modes:

| Modeler intent | Annotation value | Backend `get<Attr>Options` returns at most | Rows in DB matching search | User sees | Hint should show? | Hint actually shows? |
|---|---|---|---|---|---|---|
| Cap suggestions at 8 | `8` | 20 (forgot to cap) | 25 | 20 options | Yes (5 hidden) | Yes (20 ≥ 8) — but for wrong reason (hint means "reduce query", user has 20 already; misleading) |
| Cap suggestions at 8 | *(none — defaults 10)* | 8 (backend truncates) | 9 | 8 options | Yes (1 hidden) | **No** (8 < 10 default) — **silent truncation, same defect JNG-6409 was meant to fix** |
| Cap suggestions at 8 | `8` | 8 | 7 | 7 options | No | No (7 < 8) — correct by coincidence |

The middle row is the important one: the JNG-6409 change **does not** cover TextInput correctness in a common case, because the modeler is under no obligation (and has no discoverability) to add the annotation, so the frontend default 10 silently masks any true backend cap below 10.

**Root cause**: the backend has the ground truth about truncation and the frontend has to guess. The current contract passes no signal in either direction.

**Design principle for this change**: *the backend's returned data is the source of truth. The frontend hint should react to what the backend actually returned, not to a separately-configured number.*

## What Changes

Single Class III (react-template-only) change. `get<Attr>Options` returns richer data so the frontend can render the hint without knowing (or caring about) any limit.

### (a) Widen the `get<Attr>Options` contract

`actor/src/containers/types.ts.hbs` — the generated `Actions` type gains a new return shape for `get<Attr>Options`:

```ts
export type AutocompleteOptionsResult = {
  items: string[];
  /** True iff the backend truncated the result set. Set by the modeler-supplied implementation. */
  truncated: boolean;
};

get<Attr>Options?: (
  searchText: string,
  data: <Container>,
  editMode: boolean,
  validation: Map<keyof <Container>, string>,
) => Promise<AutocompleteOptionsResult>;
```

`AutocompleteOptionsResult` is exported from a stable location (e.g. `actor/src/components/widgets/TextWithTypeAhead.tsx.hbs` or a new `actor/src/components/widgets/autocomplete-types.ts.hbs`) so consumer implementations can import it.

### (b) Consume `truncated` inside the widget

`actor/src/components/widgets/TextWithTypeAhead.tsx.hbs`:

- `onAutoCompleteSearch` return type widens from `Promise<string[]>` to `Promise<AutocompleteOptionsResult>`.
- Internally the widget destructures `{ items, truncated }`, feeds `items` into the MUI `<Autocomplete>` options, and uses `truncated` (via a ref, same shape as today for stable Paper identity — see `autocomplete-more-results-hint/design.md` §D1) to decide whether to render `<AutocompleteMoreResultsHint />` inside the custom `slots.paper` component.
- The `autoCompleteLimit` prop is **removed**. `options.length >= limit` heuristic is deleted from this widget only.

### (c) Update the call site fragment

`actor/src/containers/widget-fragments/textinput.hbs` — the `<TextWithTypeAhead>` invocation:

- Drops the `autoCompleteLimit={ {{ calculateTextAutocompleteRows child }} }` line.
- Wraps the `get<Attr>Options` invocation to hand back the result as-is (already correct shape).

### (d) Remove the annotation-based helper

`judo-ui-react/src/main/java/hu/blackbelt/judo/ui/generator/react/UiWidgetHelper.java`:

- Delete `calculateTextAutocompleteRows(TextInput)` (introduced in JNG-6409).
- Delete the corresponding annotation constant / documentation.

`judo-ui-react/src/test/java/hu/blackbelt/judo/ui/generator/react/UiWidgetHelperTextAutocompleteRowsTest.java`:

- Delete (the helper no longer exists).

### (e) Snapshot updates

Every itest that regenerates a `TextWithTypeAhead` will emit the new call shape and the widened `Actions` interface. Currently **zero** itest models in this repo (and zero in `/home/balazs/judo-ng/runtime/judo-tatami-tests/models/`) have `isTypeAheadField="true"`, so **no snapshot files change today**. The widget file (`TextWithTypeAhead.tsx.hbs`) itself is snapshotted; its snapshot loses the `autoCompleteLimit` prop and gains `truncated` handling.

### What this change does NOT do

- Does **not** remove `calculateLinkAutocompleteRows`, `calculateTableAutocompleteRows`, or the `SingleRelationInput`/`Tags` widgets' `autoCompleteLimit`. Those two remain structurally correct under the "template controls both ends" model — no design flaw there, no reason to touch them.
- Does **not** introduce or require a `judo-meta-ui` change. `TextInput` still has no `autoCompleteRows` attribute in `ui.ecore`, and it does not need one under this design.
- Does **not** add a "load more" affordance, pagination, or a hidden-count indicator. Truncation-signal only; the hint remains informational.
- Does **not** change the widget's rendering, styling, or accessibility (still uses the same `AutocompleteMoreResultsHint` component and `slots.paper` mechanism from JNG-6409).
- Does **not** touch the two Link-driven widgets, the `TextWithTypeAhead` visual output, or the `AutocompleteMoreResultsHint` component itself.

## Consumer migration

Any downstream code implementing `get<Attr>Options` must update its return type from `Promise<string[]>` to `Promise<AutocompleteOptionsResult>`. Verified 2026-07-02 across:

- `judo-ui-react-template/judo-ui-react-itest/**/model/*.model` — zero occurrences of `isTypeAheadField="true"`.
- `/home/balazs/judo-ng/runtime/judo-tatami-tests/models/**/model/*.model` — zero occurrences.
- `/home/balazs/judo-ng/runtime/judo-tatami-tests/models/**/application/frontend-react/model/*.model` — zero occurrences.

Therefore there are **zero known existing implementations** of `get<Attr>Options` in any codebase reachable from this workspace. The signature change is technically breaking but has no observed consumers. External customer projects that may have hand-written `getXOptions` handlers will get a TypeScript error on their next regeneration; the error message directly points at the required migration (add `truncated` to the return object). A short migration note in the change log is sufficient.

## Alternatives considered (and rejected)

**Alt-1: Pass `limit` into `get<Attr>Options` as a parameter, keep `string[]` return.**
Consumers can honor the limit, but nothing forces them to; the frontend still has to compare `items.length >= limit` and hope. Removes the "no way to know" excuse but not the desync possibility. Rejected as half-measure.

**Alt-2: Have the template generate a default `get<Attr>Options` implementation** that calls a backend range operation with a fixed `_seek.limit`, mirroring the Link pipeline.
This is the "truly structural" fix but requires:
- A `judo-meta-ui` change to attach a range-operation reference to `TextInput` (equivalent of `Link.autocompleteRangeActionDefinition`).
- Coordinated changes in the ESM → UI transform and the ui.ecore metamodel.
- New generator code emitting the default handler.
Estimated multi-week effort spread across three repos. Rejected as disproportionate; may be revisited under a separate initiative if TextInput typeahead becomes a first-class model concept.

**Alt-3: Keep the annotation from JNG-6409 as a hint threshold and document the coupling contract.**
The design that shipped. The middle row of the failure-mode table above is the reason this proposal exists.

**Alt-4: Return the effective limit alongside items (`{items, limit}`) instead of `{items, truncated}`.**
Equivalent expressiveness; slightly leakier because it exposes the backend's internal number to the frontend. Truncated boolean is what the frontend actually needs. Rejected on principle-of-least-privilege grounds.
