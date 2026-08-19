## Definition of Done

- `./mvnw clean install` exits green; the React-template's itests regenerate fixture frontends and run them under Vitest/Playwright without new failures.
- Regenerated `SingleRelationInput.tsx`, `Tags.tsx`, and `TextWithTypeAhead.tsx` under at least one itest contain the new footer JSX gated on `options.length >= limit`, rendered **below** the listbox via a custom `slots.paper` Paper component (per `design.md` §D1).
- Regenerated `link/index.tsx` for a representative link container passes `autoCompleteLimit={ … }` to `<SingleRelationInput>`.
- Regenerated `widget-fragments/textinput.hbs` consumer (any container using a TypeAhead text field) passes `autoCompleteLimit={ … }` to `<TextWithTypeAhead>`.
- Regenerated `system_en-US.json` contains the new key `"judo.autocomplete.showing-first-results": "Showing the first {{limit}} results — narrow your search"`.
- Manual reproduction in `RelationTest/Actor` (documented in §0 below) shows the footer appearing **below** the truncated `SingleAggregationAssociation` dropdown both on initial open (full first page) and after typing `c` (12 matches → 10 returned), and disappearing once the search is narrowed enough to return fewer than 10 matches.
- All snapshot drifts caught by `judo-diff-checker-maven-plugin` are resolved by copying regenerated files into `src/test/resources/snapshots/frontend-react/` (AGENTS.md "Important Notes" §5; explicit `bash` one-liner in §7.2 below).
- Commit message: `JNG-6409 surface truncation hint in autocomplete dropdowns`.

## Scope confirmation (read first)

- **In scope** (server-paginated autocompletes, three widgets): `SingleRelationInput`, `Tags`, `TextWithTypeAhead`.
- **Explicitly OUT of scope**: `SingleValueFilterComponent.tsx.hbs`. Verified 2026-06-30 by reading the file (the only `<Autocomplete>` at line 165 is bound to `filter.filterOption.enumValues`, a finite local enum array supplied by `FixedTableFilters.tsx.hbs:33`). No server pagination → no truncation possible → no hint needed. Do not add `autoCompleteLimit` to this widget.
- **Metamodel constraint** (verified 2026-06-30 against `/home/balazs/judo-ng/models/judo-meta-ui/model/model/ui.ecore`): the `TextInput` EClass exposes `isTypeAheadField` but **no** `autoCompleteRows` attribute. Only the `Link` EClass has `autoCompleteRows`. Therefore `calculateTextAutocompleteRows` returns a constant `10`; do **not** attempt `input.getAutoCompleteRows()`.

## 0. Manual baseline reproduction (already done; just confirm before merge)

- [x] 0.1 In `RelationTest/Actor`, ensure `TransferObjectC` table has at least 11 rows whose `field` contains `c`. (Already populated 2026-06-30: `Giga C`, `C-extra-01..11`.)
- [x] 0.2 Navigate to `TransferObjectA → First A → Single` tab. Click into the `SingleAggregationAssociation` combobox, clear it. Confirm the initial dropdown shows 10 options with no footer (baseline before fix). Type `c`; confirm still 10 options with no footer.
- [x] 0.3 After the implementation tasks (§1–§5) are complete, redo step 0.2 and confirm the footer **does** appear with the text "Showing the first 10 results — narrow your search" **below** the listbox both before typing and after typing `c`. Narrow the search to `extra-11` and confirm the footer **disappears** (only 1 match, below the limit).

## 1. New shared footer component + slot hook

- [x] 1.1 Create `judo-ui-react/src/main/resources/actor/src/components/widgets/AutocompleteMoreResultsHint.tsx.hbs`. Export a functional component `AutocompleteMoreResultsHint` taking a `limit: number` prop that reads `t('judo.autocomplete.showing-first-results', { limit, defaultValue: 'Showing the first {{limit}} results — narrow your search' })` via `useTranslation()`. Wrap in a `<Box>` styled as an enclosed status footer sitting below the option list: `pointerEvents: 'none'`, `role="status"` (NOT `aria-hidden` — truncation is status the user needs), `borderTop: 1, borderColor: 'divider'`, a fill derived from the active paper colour via an `sx` theme callback — `emphasize(theme.palette.background.paper, 0.06)`, importing `emphasize` from `@mui/material/styles`. NOT `action.hover` (resolves to the same colour a hovered option gets) and NOT a fixed `grey[…]` step selected on `theme.palette.mode` (`grey` is absent from the generated palette, and `mode` can contradict a modeler-supplied `paperBackgroundColor`); see design §D7. Then `minHeight: 32`, and `caption` / `text.secondary` typography. No italic: the enclosing region carries the "not an option" signal.
- [x] 1.1a **(added 2026-07-08 in response to review)** Also export from the same file a hook `useAutocompleteMoreResultsHintPaper(limit: number | undefined, optionsLength: number): ComponentType<PaperProps>` that (i) computes `show = typeof limit === 'number' && limit > 0 && optionsLength >= limit`, (ii) mirrors the latest value through a `useRef` (so the returned slot component identity stays stable across threshold crossings and MUI does not remount the paper/listbox), and (iii) returns a `useCallback`-memoized Paper wrapper that prepends `<AutocompleteMoreResultsHint />` above `paperProps.children` when the ref is true. This hook is the **single source of truth** for the hint machinery — all three widget templates call it verbatim (see §4a/§4b/§4c). Do **not** add JSDoc/prose comments inside the `.hbs` — comments there leak into every generated `.tsx` (the standard G-E-N-E-R-A-T-E-D banner is enough).
- [x] 1.2 Re-export the component from `judo-ui-react/src/main/resources/actor/src/components/widgets/index.tsx.hbs` (add a `export * from './AutocompleteMoreResultsHint';` line next to the existing exports).

## 2. New i18n keys

- [x] 2.1 In `judo-ui-react/src/main/resources/actor/public/i18n/system_en-US.json.hbs`, add `"judo.autocomplete.showing-first-results": "Showing the first {{limit}} results — narrow your search",` keeping alphabetical-ish grouping (a new `judo.autocomplete.*` block is the cleanest location; place it before `judo.breadcrumb.*`). Preserve JSON validity under all `{{# if }}` guards (the existing file pattern shows where commas may be optional at trailing positions — copy that style).
- [x] 2.2 In `system_hu-HU.json.hbs`, add `"judo.autocomplete.showing-first-results": "Az első {{limit}} találat látható — szűkítse a keresést",`.
- [x] 2.3 In `system_default.json.hbs`, mirror the English value.

## 3. New Java helper

- [x] 3.1 In `judo-ui-react/src/main/java/hu/blackbelt/judo/ui/generator/react/UiWidgetHelper.java`, add a static helper directly under `calculateTableAutocompleteRows` (around line 509):

  ```java
  // JNG-6409: TextInput has no per-element autoCompleteRows override in ui.ecore today.
  // Return a constant matching calculateLinkAutocompleteRows' default. See JNG-6409.
  public static Integer calculateTextAutocompleteRows(TextInput input) {
      return 10;
  }
  ```

  Do **not** call `input.getAutoCompleteRows()` — the attribute does not exist on `TextInput` (confirmed via `ui.ecore` 2026-06-30).
- [x] 3.2 Verify the helper is auto-registered via the `@TemplateHelper` annotation on the class (existing pattern). No further wiring is required.
- [x] 3.3 Ensure the `import` for `hu.blackbelt.judo.ui.model.ui.TextInput` is present at the top of the file (it likely is already, since other `TextInput`-related helpers exist; if not, add it).

## 4. Widget template changes

> **Post-review update (2026-07-08):** The "Shared snippet" below was the **initial** implementation. In response to gaborflorian's review nit "This calculation is a duplicate so u should put this into the new component", the visibility calculation + Paper wrapper were extracted into `useAutocompleteMoreResultsHintPaper` inside `AutocompleteMoreResultsHint.tsx.hbs` (see §1.1 update below and §10). Each widget call site now consists of **one import + one hook call + one `slots` prop**; the block below is retained as historical context and describes the pre-refactor state.

### Shared snippet — pre-refactor (historical; superseded by hook, see §10)

Each widget template originally gained:

1. New imports at the top of the file:
   ```typescript
   import { useCallback } from 'react';
   import Paper, { PaperProps } from '@mui/material/Paper';
   import { AutocompleteMoreResultsHint } from './AutocompleteMoreResultsHint';
   ```
2. Just before the JSX `return (...)`:
   ```typescript
   const showHint = typeof <LIMIT_EXPR> === 'number' && <LIMIT_EXPR> > 0 && options.length >= <LIMIT_EXPR>;
   const PaperWithHint = useCallback((paperProps: PaperProps) => (
     <Paper {...paperProps}>
       {showHint && <AutocompleteMoreResultsHint />}
       {paperProps.children}
     </Paper>
   ), [showHint]);
   ```
   …substituting `<LIMIT_EXPR>` with the widget-specific name (see each subsection).
3. On the `<Autocomplete>` element, add `slots={ { paper: PaperWithHint } }` as a sibling prop.

### Shared snippet — post-refactor (✅ shipped)

Each widget template gains **one import + one hook call + one `slots` prop**:

1. New import at the top of the file:
   ```typescript
   import { useAutocompleteMoreResultsHintPaper } from './AutocompleteMoreResultsHint';
   ```
2. Just before the JSX `return (...)`:
   ```typescript
   const paperSlot = useAutocompleteMoreResultsHintPaper(<LIMIT_EXPR>, options.length);
   ```
   …substituting `<LIMIT_EXPR>` with the widget-specific name (see each subsection).
3. On the `<Autocomplete>` element, add `slots={ { paper: paperSlot } }` as a sibling prop.

No local `showHint`, no local `PaperWithHint`, no `useCallback` / `useRef` / `Paper` / `PaperProps` imports at the call site — the hook owns all of that.

### 4a. SingleRelationInput

- [x] 4a.1 In `judo-ui-react/src/main/resources/actor/src/components/widgets/SingleRelationInput.tsx.hbs`, add `autoCompleteLimit?: number;` to the props interface (next to `autoCompleteAttribute` at line ~40).
- [x] 4a.2 Destructure the new prop in the function signature.
- [x] 4a.3 Apply the **post-refactor** shared snippet with `<LIMIT_EXPR>` = `autoCompleteLimit`. `options` is the existing state variable already used by the component (verify line ~70 area).
- [x] 4a.4 Add `slots={ { paper: paperSlot } }` to the `<Autocomplete>` element at line ~170. Keep every existing prop unchanged (`renderInput`, `onChange`, `onInputChange`, `onOpen`, `freeSolo`, `forcePopupIcon`, …).
- [x] 4a.5 No input-string tracking is needed (per `design.md` §D2; the heuristic is just `options.length >= limit`).

### 4b. Tags

- [x] 4b.1 In `judo-ui-react/src/main/resources/actor/src/components/widgets/Tags.tsx.hbs`, no prop addition is needed — the widget already has `limitOptions` (default `10` at line 82).
- [x] 4b.2 Apply the **post-refactor** shared snippet with `<LIMIT_EXPR>` = `limitOptions`.
- [x] 4b.3 Add `slots={ { paper: paperSlot } }` to the `<Autocomplete>` element at line ~196.

### 4c. TextWithTypeAhead

- [x] 4c.1 In `judo-ui-react/src/main/resources/actor/src/components/widgets/TextWithTypeAhead.tsx.hbs`, add `autoCompleteLimit?: number;` to the props interface (line ~12).
- [x] 4c.2 Destructure the prop in the function signature (line ~43 area).
- [x] 4c.3 Apply the **post-refactor** shared snippet with `<LIMIT_EXPR>` = `autoCompleteLimit`. The local options state in this widget may be named differently from `options` (the widget receives `string[]` from the search action); verify the exact state variable by reading the file first, then substitute its name into the hook argument.
- [x] 4c.4 Add `slots={ { paper: paperSlot } }` to the `<Autocomplete>` element at line ~92.

## 5. Container template changes (wire the limit through)

- [x] 5.1 `judo-ui-react/src/main/resources/actor/src/containers/components/link/index.tsx.hbs`: where the `<SingleRelationInput>` is emitted (look for the existing `onAutoCompleteSearch={ … _seek: { limit: {{ calculateLinkAutocompleteRows link }} }, … }` block around line 154), add a sibling prop `autoCompleteLimit={ {{ calculateLinkAutocompleteRows link }} }`.
- [x] 5.2 `judo-ui-react/src/main/resources/actor/src/containers/widget-fragments/textinput.hbs`: where `<TextWithTypeAhead>` is emitted (around line 23), add `autoCompleteLimit={ {{ calculateTextAutocompleteRows child }} }`.
- [x] 5.3 `Tags.tsx.hbs` is the widget; its caller (`actor/src/containers/components/tag/index.tsx.hbs:88`) does NOT pass `limitOptions` today (Tags uses its built-in default `10`). No additional plumbing is required for this change — the widget reads its own `limitOptions` and uses it consistently for both the fetch limit and the hint gate.
- [x] 5.4 **Not in scope**: `SingleValueFilterComponent.tsx.hbs` and its caller `actor/src/components/table/FixedTableFilters.tsx.hbs:33`. The autocomplete inside `SingleValueFilterComponent` (line 165) is bound to `filter.filterOption.enumValues` (a finite local array of enum values), not to a server-paginated relation. Verified 2026-06-30 by reading both files. Therefore no truncation is possible and no hint is needed. Do **not** add `autoCompleteLimit` to this widget.

## 6. Spec lockdown

- [x] 6.1 The spec file `openspec/changes/autocomplete-more-results-hint/specs/input-widgets/spec.md` already declares one MODIFIED requirement ("Autocomplete dropdowns hint when the result list is truncated") with five scenarios. **Verify the file matches the contract you're about to implement.** Do not rewrite it.
- [x] 6.2 The spec file `openspec/changes/autocomplete-more-results-hint/specs/relation-management/spec.md` already declares one MODIFIED requirement ("Link autocomplete dropdown surfaces truncation hint") with two scenarios. Same verification.
- [x] 6.3 Run `openspec validate autocomplete-more-results-hint --strict`. It should exit with `Change 'autocomplete-more-results-hint' is valid`. Address any new issues introduced by the implementation.

## 7. Integration build + snapshot refresh

- [x] 7.1 Run `./mvnw clean install` from the repo root. The first run is expected to fail in `judo-diff-checker-maven-plugin` for every committed snapshot file that references the widget changes.
- [x] 7.2 Refresh the affected snapshots in one go with the following script. It derives each target path from the snapshot path and copies the freshly generated file on top of the committed snapshot (the `.snapshot` suffix is stripped to find the target generated file under `target/frontend-react/`).

  Pre-flight: the six **container-level** snapshots that reference an autocomplete widget (captured by `grep` on 2026-06-30):

  ```
  judo-ui-react-itest/ActionGroupTest/action_group_test__god/src/test/resources/snapshots/frontend-react/src/dialogs/View/Galaxy/Stars/RelationViewPage/index.tsx.snapshot
  judo-ui-react-itest/ActionGroupTest/action_group_test__god/src/test/resources/snapshots/frontend-react/src/pages/God/God/Galaxies/AccessViewPage/index.tsx.snapshot
  judo-ui-react-itest/ActionGroupTest/action_group_test__god/src/test/resources/snapshots/frontend-react/src/containers/View/Matter/Form/ViewMatterForm.tsx.snapshot
  judo-ui-react-itest/ActionGroupTestPro/action_group_test_pro__god/src/test/resources/snapshots/frontend-react/src/pages/God/God/Galaxies/AccessViewPage/index.tsx.snapshot
  judo-ui-react-itest/RelationTest/relation_test__actor/src/test/resources/snapshots/frontend-react/src/pages/Actor/TagContainerTransfer/AccessViewPage/index.tsx.snapshot
  judo-ui-react-itest/RelationTest/relation_test__actor/src/test/resources/snapshots/frontend-react/src/containers/TagContainerTransfer/TagContainerTransfer_View_Edit/components/TagContainerTransferTagContainerTransfer_View_EditManyAggregationCompostionComponent/index.tsx.snapshot
  ```

  Refresh them (and any other committed snapshots that turn out to drift) with:

  ```bash
  for snap in $(grep -rl -e 'SingleRelationInput' -e 'TextWithTypeAhead' -e '<Tags' \
                  judo-ui-react-itest/*/*/src/test/resources/snapshots/frontend-react 2>/dev/null); do
      itest_root=$(echo "$snap" | sed -E 's|(judo-ui-react-itest/[^/]+/[^/]+)/.*|\1|')
      rel=$(echo "$snap" | sed -E 's|.*/snapshots/frontend-react/||; s|\.snapshot$||')
      src="$itest_root/target/frontend-react/$rel"
      if [ -f "$src" ]; then
          cp "$src" "$snap"
          echo "refreshed $snap"
      else
          echo "MISSING source: $src (for $snap)" >&2
      fi
  done
  ```

  If any widget files themselves have committed snapshots (run the same pattern with `SingleRelationInput.tsx.snapshot`, `Tags.tsx.snapshot`, `TextWithTypeAhead.tsx.snapshot` filenames), refresh those too:

  ```bash
  for widget in SingleRelationInput Tags TextWithTypeAhead; do
      find judo-ui-react-itest -path "*/src/test/resources/snapshots/frontend-react/*/${widget}.tsx.snapshot" 2>/dev/null | while read snap; do
          itest_root=$(echo "$snap" | sed -E 's|(judo-ui-react-itest/[^/]+/[^/]+)/.*|\1|')
          rel=$(echo "$snap" | sed -E 's|.*/snapshots/frontend-react/||; s|\.snapshot$||')
          cp "$itest_root/target/frontend-react/$rel" "$snap" && echo "refreshed $snap"
      done
  done
  ```

- [x] 7.3 Sanity-check each diff:
  ```bash
  git diff --stat -- 'judo-ui-react-itest/**/*.snapshot'
  ```
  The diff for each refreshed snapshot should contain ONLY one of:
  - the new `autoCompleteLimit={...}` prop on a `<SingleRelationInput>` or `<TextWithTypeAhead>` element (container snapshots);
  - the new `slots.paper` block + `showHint` + `PaperWithHint` consts + the three new imports (widget snapshots, if any);
  - the new `judo.autocomplete.showing-first-results` line (i18n snapshots, if any).

  Anything else (whitespace drift, unrelated regenerations) is a red flag — investigate before continuing.
- [x] 7.4 Re-run `./mvnw clean install` and confirm green.

## 8. Manual verification on RelationTest

- [x] 8.1 In `judo-ng/runtime/judo-tatami-tests/models/RelationTest`, run `./judo.sh start` against the regenerated bundle. *(Additionally verified 2026-07-08 on `judo-ng/runtime/judo-tatami-tests/models/ActionGroupTest` — the Astronomer single-relation autocomplete on Galaxy view renders the hint identically.)*
- [x] 8.2 Repeat the §0.3 reproduction. Capture a screenshot of the dropdown with the footer visible **below** the option list (reading "Showing the first 10 results — narrow your search") and attach it to the JIRA ticket (JNG-6409).

## 9. Commit

- [x] 9.1 Stage and commit only the following paths:
  - `judo-ui-react/src/main/java/hu/blackbelt/judo/ui/generator/react/UiWidgetHelper.java`
  - `judo-ui-react/src/main/resources/actor/src/components/widgets/AutocompleteMoreResultsHint.tsx.hbs` (new)
  - `judo-ui-react/src/main/resources/actor/src/components/widgets/index.tsx.hbs`
  - `judo-ui-react/src/main/resources/actor/src/components/widgets/SingleRelationInput.tsx.hbs`
  - `judo-ui-react/src/main/resources/actor/src/components/widgets/Tags.tsx.hbs`
  - `judo-ui-react/src/main/resources/actor/src/components/widgets/TextWithTypeAhead.tsx.hbs`
  - `judo-ui-react/src/main/resources/actor/src/containers/components/link/index.tsx.hbs`
  - `judo-ui-react/src/main/resources/actor/src/containers/widget-fragments/textinput.hbs`
  - `judo-ui-react/src/main/resources/actor/public/i18n/system_en-US.json.hbs`
  - `judo-ui-react/src/main/resources/actor/public/i18n/system_hu-HU.json.hbs`
  - `judo-ui-react/src/main/resources/actor/public/i18n/system_default.json.hbs`
  - `openspec/changes/autocomplete-more-results-hint/**`
  - Any updated `judo-ui-react-itest/**/src/test/resources/snapshots/frontend-react/**` files.

  Do **not** stage `SingleValueFilterComponent.tsx.hbs` — it is out of scope per §5.4.
- [x] 9.2 Commit on the `feature/JNG-6409_autocomplete_more_results_hint` branch with the Definition-of-Done message above.
- [x] 9.3 Push the branch and open a PR against `develop` referencing JNG-6409.

## 10. Post-review refactor (2026-07-08, commit `8bc5ecfe`)

Addresses gaborflorian's review comment on `SingleRelationInput.tsx.hbs` line 170: *“This calculation is a duplicate so u should put this into the new component”*. The same nine-line block (visibility calc + `useRef` mirror + `useCallback` Paper wrapper) had been copy-pasted into all three widgets by the initial §4 snippet.

- [x] 10.1 Move the block into `AutocompleteMoreResultsHint.tsx.hbs` as the exported hook `useAutocompleteMoreResultsHintPaper(limit, optionsLength)` (see §1.1a). Signature returns `ComponentType<PaperProps>`; body encapsulates the ref-based stable-identity slot component. Behaviour is byte-identical to the pre-refactor per-widget block.
- [x] 10.2 Update `SingleRelationInput.tsx.hbs`:
  - Change `import Paper, { type PaperProps } from '@mui/material/Paper';` back to `import Paper from '@mui/material/Paper';` (Paper is still used elsewhere in the file for the Popper; `PaperProps` is no longer needed at the call site).
  - Drop `useCallback` from the react import (`useRef` stays — used elsewhere in the widget).
  - Change `import { AutocompleteMoreResultsHint } from './AutocompleteMoreResultsHint';` to `import { useAutocompleteMoreResultsHintPaper } from './AutocompleteMoreResultsHint';`.
  - Replace the nine-line `showMoreResultsHint` / `showMoreResultsHintRef` / `PaperWithMoreResultsHint` block with `const paperSlot = useAutocompleteMoreResultsHintPaper(autoCompleteLimit, options.length);`.
  - Rename the `slots` prop value from `PaperWithMoreResultsHint` to `paperSlot`.
- [x] 10.3 Update `Tags.tsx.hbs`:
  - Remove the newly-added `import Paper, { type PaperProps } from '@mui/material/Paper';` line entirely (Paper is not otherwise used by this widget).
  - Change the import to `useAutocompleteMoreResultsHintPaper`.
  - Replace the nine-line block with `const paperSlot = useAutocompleteMoreResultsHintPaper(limitOptions, options.length);`.
  - Rename `slots.paper` value to `paperSlot`.
- [x] 10.4 Update `TextWithTypeAhead.tsx.hbs`:
  - Remove the `import Paper, { type PaperProps } from '@mui/material/Paper';` line entirely.
  - Drop `useCallback` and `useRef` from the react import (neither was used elsewhere in this widget).
  - Change the import to `useAutocompleteMoreResultsHintPaper`.
  - Replace the nine-line block with `const paperSlot = useAutocompleteMoreResultsHintPaper(autoCompleteLimit, options.length);`.
  - Rename `slots.paper` value to `paperSlot`.
- [x] 10.5 Drop the two JSDoc blocks (component and hook) that had been added to `AutocompleteMoreResultsHint.tsx.hbs` during the initial §1.1 draft. `.hbs` comments leak into every generated `.tsx` (only the standard G-E-N-E-R-A-T-E-D banner is desired). Rule for future changes: **no free-form comments inside `.hbs` templates**.
- [x] 10.6 Full `mvn clean install` from repo root, verify BUILD SUCCESS and no snapshot drift (the widget files are not covered by committed snapshots; the container-level snapshots refreshed in §7.2 still match because none of the container templates changed in this refactor).
- [x] 10.7 Manual verification against the locally-running `judo-tatami-tests/models/ActionGroupTest` Karaf: wipe `.karaf`, regenerate karaf-offline (`mvn -pl karaf-offline -am install -Pbuild-karaf -DskipPrepareNodeJS`), restart via `judo.sh`, hard-refresh browser — hint still renders identically to the pre-refactor screenshot.
- [x] 10.8 Single commit on the same branch: `JNG-6409 extract useAutocompleteMoreResultsHintPaper hook, drop hbs comments`. Pushed as `8bc5ecfe`. 4 files changed, +30 / −47 (net cleanup of ~17 duplicated lines).
