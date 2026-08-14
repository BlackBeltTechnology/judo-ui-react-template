## Definition of Done

- `./mvnw clean install` exits green; itests regenerate frontends and pass under Vitest/Playwright without new failures.
- Regenerated `AutocompleteNoResultsHint.tsx` exports `AutocompleteNoResultsHint` and `useAutocompleteHintPaper`, while `AutocompleteMoreResultsHint.tsx` still exports `AutocompleteMoreResultsHint`. The old `useAutocompleteMoreResultsHintPaper` export is gone.
- Regenerated `SingleRelationInput.tsx`, `Tags.tsx`, `TextWithTypeAhead.tsx` each import `useAutocompleteHintPaper` and call it with `{ limit, optionsLength: options.length, loading }`. They pass its return value to `slots={ { paper: paperSlot } }`.
- Regenerated `SingleValueFilterComponent.tsx` carries the prop `noOptionsText={t('judo.autocomplete.no-results', { defaultValue: 'No matching results' })}` on its enum-branch `<Autocomplete>`.
- Regenerated `system_en-US.json`, `system_hu-HU.json`, and `system_default.json` each contain the new `judo.autocomplete.no-results` key alongside the pre-existing `judo.autocomplete.showing-first-results` key.
- Live verification against `ActionGroupTest`: typing a nonsense query into the Astronomer `SingleRelationInput` under `en-US` shows `"No matching results"` in the dropdown; the served `system_hu-HU.json` contains `"Nincs találat"` (visual `hu-HU` verification skipped because the app has no runtime language switcher — the rendering code path is locale-agnostic and only reads whichever key i18next has active).
- `judo-diff-checker-maven-plugin` reports zero drifts: none of the templates touched by this change has a committed snapshot (see §6.2), so no file needs copying into `src/test/resources/snapshots/frontend-react/`.
- Commit message: `JNG-6410 localize autocomplete empty-state message`.

## 0. Manual baseline reproduction (completed 2026-07-09)

- [x] 0.1 Baseline: on `ActionGroupTest`, open a `SingleRelationInput` (e.g. Create Galaxy → Astronomer). Type `zzzznope`. Under the original (`noOptionsText`-prop-only) attempt no message appeared at all, because MUI suppresses `noOptionsText` under `freeSolo`. This is the actual defect surface.
- [x] 0.2 Post-implementation: same query on the same field renders "No matching results" in the dropdown under `en-US`. (Screenshot in JIRA JNG-6410.)
- [x] 0.3 Hungarian value confirmed served via `curl http://localhost:8181/ActionGroupTest/God/i18n/system_hu-HU.json | grep no-results` → `"judo.autocomplete.no-results": "Nincs találat"`. The rendering code path is `t('judo.autocomplete.no-results', ...)` — identical for both locales; only the JSON lookup changes.

## 1. New i18n keys

- [x] 1.1 In `judo-ui-react/src/main/resources/actor/public/i18n/system_en-US.json.hbs`, add `"judo.autocomplete.no-results": "No matching results",` inside the existing `judo.autocomplete.*` group, immediately after `judo.autocomplete.showing-first-results`.
- [x] 1.2 In `system_hu-HU.json.hbs`, add `"judo.autocomplete.no-results": "Nincs találat",` in the same position.
- [x] 1.3 In `system_default.json.hbs`, mirror the `en-US` value.

## 2. New sibling file for the no-results hint + composition hook

- [x] 2.1 Created new file `judo-ui-react/src/main/resources/actor/src/components/widgets/AutocompleteNoResultsHint.tsx.hbs` containing:
  - `<AutocompleteNoResultsHint>` FC — renders a `<Box role="presentation" sx={ { px: 2, py: 1, pointerEvents: 'none' } }>` containing a `<Typography variant="body2" color="text.secondary">` whose text is `t('judo.autocomplete.no-results', { defaultValue: 'No matching results' })`.
  - `useAutocompleteHintPaper({ limit, optionsLength, loading })` — composition hook that imports `<AutocompleteMoreResultsHint>` from its sibling file and renders whichever hint applies inside one memoized Paper. Computes `showMoreHint = typeof limit === 'number' && limit > 0 && optionsLength >= limit` and `showNoResults = !loading && optionsLength === 0`, stores both in a ref, returns a `useCallback([], ...)` memoized Paper wrapper.
- [x] 2.2 Registered the new template in `judo-ui-react/src/main/resources/ui-react.yaml` immediately after the `AutocompleteMoreResultsHint.tsx` entry, so the generator emits the file.
- [x] 2.3 Deleted the now-obsolete `useAutocompleteMoreResultsHintPaper` hook from `judo-ui-react/src/main/resources/actor/src/components/widgets/AutocompleteMoreResultsHint.tsx.hbs`. The `<AutocompleteMoreResultsHint>` FC in that file is unchanged.

## 3. Wire the new hook into the three freeSolo widgets

Each of `SingleRelationInput.tsx.hbs`, `Tags.tsx.hbs`, `TextWithTypeAhead.tsx.hbs`:

- [x] 3.1 Update the import line `import { useAutocompleteMoreResultsHintPaper } from './AutocompleteMoreResultsHint';` to `import { useAutocompleteHintPaper } from './AutocompleteNoResultsHint';` (note the file change).
- [x] 3.2 Update the call site `const paperSlot = useAutocompleteMoreResultsHintPaper(<limitVar>, options.length);` to `const paperSlot = useAutocompleteHintPaper({ limit: <limitVar>, optionsLength: options.length, loading });`. The `loading` variable is already in scope on all three widgets. The `<limitVar>` is `autoCompleteLimit` in `SingleRelationInput` and `TextWithTypeAhead`, and `limitOptions` in `Tags`.
- [x] 3.3 The JSX line `slots={ { paper: paperSlot } }` is unchanged (already present from JNG-6409).

## 4. Set `noOptionsText` on the enum filter (non-freeSolo)

- [x] 4.1 In `judo-ui-react/src/main/resources/actor/src/components/table/SingleValueFilterComponent.tsx.hbs`, at the enum-branch `<Autocomplete>` (line ~167), add:
  ```tsx
  noOptionsText={t('judo.autocomplete.no-results', { defaultValue: 'No matching results' })}
  ```
  `useTranslation` / `t` are already in scope (used elsewhere in the file). No hook or slot wiring is needed because the widget does not set `freeSolo` — MUI's native rendering path takes over.

## 5. Spec + validation

- [x] 5.1 Update `openspec/changes/autocomplete-no-results-message/proposal.md` and `design.md` to reflect the Paper-slot approach (D1 flipped). Removed implementation-leaking claim about MUI's `role="listbox"` placement from `specs/input-widgets/spec.md`.
- [x] 5.2 Ran `openspec validate autocomplete-no-results-message --strict` → `Change 'autocomplete-no-results-message' is valid`.

## 6. Integration build + snapshot refresh

- [x] 6.1 Ran `mvn clean install -DskipPrepareNodeJS` from the repo root → **BUILD SUCCESS** on the first run. All itests (`ActionGroupTest`, `ActionGroupTestPro`, `CRUDActionsTest`, `OperationParametersTest`, `RelationTest`, `SimpleOrderManagement`) built and their frontends regenerated. The `judo-diff-checker-maven-plugin:checkDiffs` goal ran on all four snapshotted modules with zero diffs.
- [x] 6.2 No snapshot refresh required. Committed snapshots under `judo-ui-react-itest/**/src/test/resources/snapshots/frontend-react/**` cover container-level and page-level `.tsx` files only — verified via `find` that none of the modified templates (`SingleRelationInput`, `Tags`, `TextWithTypeAhead`, `SingleValueFilterComponent`, `AutocompleteMoreResultsHint`, or `system_*.json`) have a corresponding snapshot file. Container/page snapshots are unaffected because container templates were not modified.

## 7. Commit

- [x] 7.1 Staged and committed only:
  - `judo-ui-react/src/main/resources/actor/src/components/widgets/AutocompleteMoreResultsHint.tsx.hbs`
  - `judo-ui-react/src/main/resources/actor/src/components/widgets/AutocompleteNoResultsHint.tsx.hbs`
  - `judo-ui-react/src/main/resources/ui-react.yaml`
  - `judo-ui-react/src/main/resources/actor/src/components/widgets/SingleRelationInput.tsx.hbs`
  - `judo-ui-react/src/main/resources/actor/src/components/widgets/Tags.tsx.hbs`
  - `judo-ui-react/src/main/resources/actor/src/components/widgets/TextWithTypeAhead.tsx.hbs`
  - `judo-ui-react/src/main/resources/actor/src/components/table/SingleValueFilterComponent.tsx.hbs`
  - `judo-ui-react/src/main/resources/actor/public/i18n/system_en-US.json.hbs`
  - `judo-ui-react/src/main/resources/actor/public/i18n/system_hu-HU.json.hbs`
  - `judo-ui-react/src/main/resources/actor/public/i18n/system_default.json.hbs`
  - `openspec/changes/autocomplete-no-results-message/**`
  - Any updated `judo-ui-react-itest/**/src/test/resources/snapshots/frontend-react/**` files.
- [x] 7.2 Branch: `feature/JNG-6410_autocomplete_no_results_message`. Commit message: `JNG-6410 localize autocomplete empty-state message`.
- [x] 7.3 Pushed. PR against `develop` still to be opened via UI.
