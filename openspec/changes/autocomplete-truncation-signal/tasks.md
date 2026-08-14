## Definition of Done

- `mvn clean install` exits green; the React-template's itests regenerate fixture frontends and run them under Vitest/Playwright without new failures.
- The regenerated `TextWithTypeAhead.tsx` snapshot no longer contains an `autoCompleteLimit` prop and shows `truncated`-gated hint rendering.
- The regenerated `Actions` type for at least one container with a typeahead TextInput exposes `get<Attr>Options?: (...) => Promise<AutocompleteOptionsResult>`.
- `UiWidgetHelper.calculateTextAutocompleteRows` and `UiWidgetHelperTextAutocompleteRowsTest` no longer exist.
- A one-line migration note is added to `README.md` under "Breaking changes" (or the closest equivalent) linking to this change directory.
- Commit message: `JNG-XXXX switch TextInput typeahead hint to backend-signalled truncation`.

## Scope confirmation (read first)

- **In scope**: `TextWithTypeAhead` widget, its call-site fragment, and the `get<Attr>Options` action-type declaration.
- **Explicitly OUT of scope**: `SingleRelationInput`, `Tags`, `SingleValueFilterComponent`, any Java helper other than `calculateTextAutocompleteRows`, any snapshot for containers whose model does not use `isTypeAheadField="true"`.
- **Precondition**: `autocomplete-more-results-hint` (JNG-6409) is merged and the annotation-based helper is present in `develop`.

## 1. Widen the action type

- [ ] 1.1 Introduce an `AutocompleteOptionsResult` type in a dedicated **non-component** module — new template `judo-ui-react/src/main/resources/actor/src/components-api/components/AutocompleteOptions.ts.hbs`, re-exported from the existing `components-api/components/index.ts.hbs` barrel. Shape: `{ items: string[]; truncated: boolean }` with a JSDoc line on `truncated` explaining "set true if the backend capped the returned list". Register the new template in `ui-react.yaml`. Rationale: the contract is shared between the widget and the container `Actions` interface, so it must not live inside a component file — that would make `containers/types.ts.hbs` depend on a UI module for a pure type.
- [ ] 1.2 Update `judo-ui-react/src/main/resources/actor/src/containers/types.ts.hbs:64` to import `AutocompleteOptionsResult` from `~/components-api` (the file already imports types from there) and change the return type of `get<Attr>Options` to `Promise<AutocompleteOptionsResult>`.
- [ ] 1.3 Confirm no other template references `Promise<string[]>` as a `get*Options` return.

## 2. Consume `truncated` in the widget

- [ ] 2.1 In `TextWithTypeAhead.tsx.hbs`:
  - Remove the `autoCompleteLimit?: number` prop from the component's props type.
  - Remove the `const showMoreResultsHint = ...` computation.
  - Add a `truncatedRef` (mirroring the existing `showMoreResultsHintRef` pattern).
  - After the `onAutoCompleteSearch` returns, destructure `{ items, truncated }`, set `options` to `items`, and assign `truncatedRef.current = truncated`.
  - Rebind the memoised custom Paper component to render `<AutocompleteMoreResultsHint />` iff `truncatedRef.current` is true.
- [ ] 2.2 Import `AutocompleteOptionsResult` from `~/components-api` and update the JSDoc block above the component to describe the new contract in one paragraph, with a pointer to that type.

## 3. Update the call site

- [ ] 3.1 In `judo-ui-react/src/main/resources/actor/src/containers/widget-fragments/textinput.hbs`:
  - Remove the `autoCompleteLimit={ {{ calculateTextAutocompleteRows child }} }` line.
  - Leave the `onAutoCompleteSearch` wrapper as-is; the modeler's `get<Attr>Options` now returns the correct shape directly.

## 4. Remove the deprecated helper and its test

- [ ] 4.1 Delete `UiWidgetHelper.calculateTextAutocompleteRows(TextInput)`.
- [ ] 4.2 No test file to delete — verified `judo-ui-react/src/test/java/hu/blackbelt/judo/ui/generator/react/` contains only `MaskEntryTest.java`; `calculateTextAutocompleteRows` was never unit-tested.
- [ ] 4.3 Grep the repo for `calculateTextAutocompleteRows` and confirm zero remaining references (templates, YAML, or docs).
- [ ] 4.4 Grep for `judo-ui-react::autoCompleteRows` and confirm zero remaining references.

## 5. Regen and verify

- [ ] 5.1 Wipe `judo-ui-react-itest/SimpleOrderManagement/simple_order_management__customer/target/frontend-react/` and re-run `mvn generate-sources` (per the recipe already proven in the JNG-6409 workflow).
- [ ] 5.2 Confirm the generated `TextWithTypeAhead.tsx` matches the new shape (grep for `truncated` and for the **absence** of `autoCompleteLimit`).
- [ ] 5.3 Live-app verification: repeat the RelationTest rebuild-and-Karaf-restart procedure from the JNG-6409 workflow after first enabling `isTypeAheadField="true"` on one TextInput in `RelationTest-ui.model` and providing a temporary Pandino DI hook that returns `{ items: seed(20), truncated: true }` for that field. Confirm the hint appears in the browser without any annotation being set.

## 6. Documentation

- [ ] 6.1 Add a one-paragraph section to the top-level `README.md` or `docs/` describing the `AutocompleteOptionsResult` contract and pointing at this change directory.
- [ ] 6.2 If the JNG-6409 changelog entry has been drafted but not yet released, update it to note that the `judo-ui-react::autoCompleteRows` annotation is superseded by this change and never became a stable public interface.

## 7. Snapshot updates

- [ ] 7.1 Run `mvn install` on affected itests and confirm `judo-diff-checker-maven-plugin` reports **zero** drifts. Expected precondition: `TextWithTypeAhead.tsx` has no committed snapshot (the snapshot set covers container/page-level files only). If any drift does appear, refresh it by copying regenerated files into the appropriate `src/test/resources/snapshots/frontend-react/` directories per AGENTS.md "Important Notes" §5.
- [ ] 7.2 Confirm no container-level snapshot changes (verified precondition: zero itest models with `isTypeAheadField="true"`).
