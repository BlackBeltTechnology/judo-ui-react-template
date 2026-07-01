## Definition of Done

- `./mvnw clean install` exits green; the React-template's itests regenerate fixture frontends and run them under Vitest/Playwright without new failures.
- Regenerated `SingleRelationInput.tsx`, `Tags.tsx`, and `TextWithTypeAhead.tsx` under at least one itest contain the new header JSX gated on `options.length >= limit`, rendered **above** the listbox via a custom `slots.paper` Paper component (per `design.md` §D1).
- Regenerated `link/index.tsx` for a representative link container passes `autoCompleteLimit={ … }` to `<SingleRelationInput>`.
- Regenerated `widget-fragments/textinput.hbs` consumer (any container using a TypeAhead text field) passes `autoCompleteLimit={ … }` to `<TextWithTypeAhead>`.
- Regenerated `system_en-US.json` contains the new key `"judo.autocomplete.type-for-more-results": "Type for more results…"`.
- Manual reproduction in `RelationTest/Actor` (documented in §0 below) shows the header appearing **above** the truncated `SingleAggregationAssociation` dropdown both on initial open (full first page) and after typing `c` (12 matches → 10 returned), and disappearing once the search is narrowed enough to return fewer than 10 matches.
- All snapshot drifts caught by `judo-diff-checker-maven-plugin` are resolved by copying regenerated files into `src/test/resources/snapshots/frontend-react/` (AGENTS.md "Important Notes" §5; explicit `bash` one-liner in §7.2 below).
- Commit message: `JNG-6409 surface truncation hint in autocomplete dropdowns`.

## Scope confirmation (read first)

- **In scope** (server-paginated autocompletes, three widgets): `SingleRelationInput`, `Tags`, `TextWithTypeAhead`.
- **Explicitly OUT of scope**: `SingleValueFilterComponent.tsx.hbs`. Verified 2026-06-30 by reading the file (the only `<Autocomplete>` at line 165 is bound to `filter.filterOption.enumValues`, a finite local enum array supplied by `FixedTableFilters.tsx.hbs:33`). No server pagination → no truncation possible → no hint needed. Do not add `autoCompleteLimit` to this widget.
- **Metamodel constraint** (verified 2026-06-30 against `/home/balazs/judo-ng/models/judo-meta-ui/model/model/ui.ecore`): the `TextInput` EClass exposes `isTypeAheadField` but **no** `autoCompleteRows` attribute. Only the `Link` EClass has `autoCompleteRows`. Therefore `calculateTextAutocompleteRows` returns a constant `10`; do **not** attempt `input.getAutoCompleteRows()`.

## 0. Manual baseline reproduction (already done; just confirm before merge)

- [ ] 0.1 In `RelationTest/Actor`, ensure `TransferObjectC` table has at least 11 rows whose `field` contains `c`. (Already populated 2026-06-30: `Giga C`, `C-extra-01..11`.)
- [ ] 0.2 Navigate to `TransferObjectA → First A → Single` tab. Click into the `SingleAggregationAssociation` combobox, clear it. Confirm the initial dropdown shows 10 options with no header (baseline before fix). Type `c`; confirm still 10 options with no header.
- [ ] 0.3 After the implementation tasks (§1–§5) are complete, redo step 0.2 and confirm the header **does** appear with the text "Type for more results…" **above** the listbox both before typing and after typing `c`. Narrow the search to `extra-11` and confirm the header **disappears** (only 1 match, below the limit).

## 1. New shared header component

- [ ] 1.1 Create `judo-ui-react/src/main/resources/actor/src/components/widgets/AutocompleteMoreResultsHint.tsx.hbs`. Export a single functional component `AutocompleteMoreResultsHint` with no props that reads `t('judo.autocomplete.type-for-more-results', { defaultValue: 'Type for more results…' })` via `useTranslation()`. Wrap in a styled `<Box>` (or MUI `<Typography>`) with list-subheader-like padding so it visually sits above the option list. Set `pointerEvents: 'none'`, `aria-hidden="true"`, `role="presentation"`, and **`fontStyle: 'italic'`** so the text reads as a tip rather than a selectable option. Add an optional bottom divider (`borderBottom: 1, borderColor: 'divider'`) so it visually separates from the first option.
- [ ] 1.2 Re-export the component from `judo-ui-react/src/main/resources/actor/src/components/widgets/index.tsx.hbs` (add a `export * from './AutocompleteMoreResultsHint';` line next to the existing exports).

## 2. New i18n keys

- [ ] 2.1 In `judo-ui-react/src/main/resources/actor/public/i18n/system_en-US.json.hbs`, add `"judo.autocomplete.type-for-more-results": "Type for more results…",` keeping alphabetical-ish grouping (a new `judo.autocomplete.*` block is the cleanest location; place it before `judo.breadcrumb.*`). Preserve JSON validity under all `{{# if }}` guards (the existing file pattern shows where commas may be optional at trailing positions — copy that style).
- [ ] 2.2 In `system_hu-HU.json.hbs`, add `"judo.autocomplete.type-for-more-results": "Gépeljen tovább a további találatokhoz…",`.
- [ ] 2.3 In `system_default.json.hbs`, mirror the English value.

## 3. New Java helper

- [ ] 3.1 In `judo-ui-react/src/main/java/hu/blackbelt/judo/ui/generator/react/UiWidgetHelper.java`, add a static helper directly under `calculateTableAutocompleteRows` (around line 509):

  ```java
  // JNG-6409: TextInput has no per-element autoCompleteRows override in ui.ecore today.
  // Return a constant matching calculateLinkAutocompleteRows' default. See JNG-6409.
  public static Integer calculateTextAutocompleteRows(TextInput input) {
      return 10;
  }
  ```

  Do **not** call `input.getAutoCompleteRows()` — the attribute does not exist on `TextInput` (confirmed via `ui.ecore` 2026-06-30).
- [ ] 3.2 Verify the helper is auto-registered via the `@TemplateHelper` annotation on the class (existing pattern). No further wiring is required.
- [ ] 3.3 Ensure the `import` for `hu.blackbelt.judo.ui.model.ui.TextInput` is present at the top of the file (it likely is already, since other `TextInput`-related helpers exist; if not, add it).

## 4. Widget template changes

### Shared snippet (referenced by §4a/§4b/§4c)

Each widget template gains:

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

### 4a. SingleRelationInput

- [ ] 4a.1 In `judo-ui-react/src/main/resources/actor/src/components/widgets/SingleRelationInput.tsx.hbs`, add `autoCompleteLimit?: number;` to the props interface (next to `autoCompleteAttribute` at line ~40).
- [ ] 4a.2 Destructure the new prop in the function signature.
- [ ] 4a.3 Apply the shared snippet above with `<LIMIT_EXPR>` = `autoCompleteLimit`. `options` is the existing state variable already used by the component (verify line ~70 area).
- [ ] 4a.4 Add `slots={ { paper: PaperWithHint } }` to the `<Autocomplete>` element at line ~170. Keep every existing prop unchanged (`renderInput`, `onChange`, `onInputChange`, `onOpen`, `freeSolo`, `forcePopupIcon`, …).
- [ ] 4a.5 No input-string tracking is needed (per `design.md` §D2; the heuristic is just `options.length >= limit`).

### 4b. Tags

- [ ] 4b.1 In `judo-ui-react/src/main/resources/actor/src/components/widgets/Tags.tsx.hbs`, no prop addition is needed — the widget already has `limitOptions` (default `10` at line 82).
- [ ] 4b.2 Apply the shared snippet with `<LIMIT_EXPR>` = `limitOptions`.
- [ ] 4b.3 Add `slots={ { paper: PaperWithHint } }` to the `<Autocomplete>` element at line ~196.

### 4c. TextWithTypeAhead

- [ ] 4c.1 In `judo-ui-react/src/main/resources/actor/src/components/widgets/TextWithTypeAhead.tsx.hbs`, add `autoCompleteLimit?: number;` to the props interface (line ~12).
- [ ] 4c.2 Destructure the prop in the function signature (line ~43 area).
- [ ] 4c.3 Apply the shared snippet with `<LIMIT_EXPR>` = `autoCompleteLimit`. The local options state in this widget may be named differently from `options` (the widget receives `string[]` from the search action); verify the exact state variable by reading the file first, then substitute its name into the `showHint` expression.
- [ ] 4c.4 Add `slots={ { paper: PaperWithHint } }` to the `<Autocomplete>` element at line ~92.

## 5. Container template changes (wire the limit through)

- [ ] 5.1 `judo-ui-react/src/main/resources/actor/src/containers/components/link/index.tsx.hbs`: where the `<SingleRelationInput>` is emitted (look for the existing `onAutoCompleteSearch={ … _seek: { limit: {{ calculateLinkAutocompleteRows link }} }, … }` block around line 154), add a sibling prop `autoCompleteLimit={ {{ calculateLinkAutocompleteRows link }} }`.
- [ ] 5.2 `judo-ui-react/src/main/resources/actor/src/containers/widget-fragments/textinput.hbs`: where `<TextWithTypeAhead>` is emitted (around line 23), add `autoCompleteLimit={ {{ calculateTextAutocompleteRows child }} }`.
- [ ] 5.3 `Tags.tsx.hbs` is the widget; its caller (`actor/src/containers/components/tag/index.tsx.hbs:88`) does NOT pass `limitOptions` today (Tags uses its built-in default `10`). No additional plumbing is required for this change — the widget reads its own `limitOptions` and uses it consistently for both the fetch limit and the hint gate.
- [ ] 5.4 **Not in scope**: `SingleValueFilterComponent.tsx.hbs` and its caller `actor/src/components/table/FixedTableFilters.tsx.hbs:33`. The autocomplete inside `SingleValueFilterComponent` (line 165) is bound to `filter.filterOption.enumValues` (a finite local array of enum values), not to a server-paginated relation. Verified 2026-06-30 by reading both files. Therefore no truncation is possible and no hint is needed. Do **not** add `autoCompleteLimit` to this widget.

## 6. Spec lockdown

- [ ] 6.1 The spec file `openspec/changes/autocomplete-more-results-hint/specs/input-widgets/spec.md` already declares one MODIFIED requirement ("Autocomplete dropdowns hint when the result list is truncated") with five scenarios. **Verify the file matches the contract you're about to implement.** Do not rewrite it.
- [ ] 6.2 The spec file `openspec/changes/autocomplete-more-results-hint/specs/relation-management/spec.md` already declares one MODIFIED requirement ("Link autocomplete dropdown surfaces truncation hint") with two scenarios. Same verification.
- [ ] 6.3 Run `openspec validate autocomplete-more-results-hint --strict`. It should exit with `Change 'autocomplete-more-results-hint' is valid`. Address any new issues introduced by the implementation.

## 7. Integration build + snapshot refresh

- [ ] 7.1 Run `./mvnw clean install` from the repo root. The first run is expected to fail in `judo-diff-checker-maven-plugin` for every committed snapshot file that references the widget changes.
- [ ] 7.2 Refresh the affected snapshots in one go with the following script. It derives each target path from the snapshot path and copies the freshly generated file on top of the committed snapshot (the `.snapshot` suffix is stripped to find the target generated file under `target/frontend-react/`).

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

- [ ] 7.3 Sanity-check each diff:
  ```bash
  git diff --stat -- 'judo-ui-react-itest/**/*.snapshot'
  ```
  The diff for each refreshed snapshot should contain ONLY one of:
  - the new `autoCompleteLimit={...}` prop on a `<SingleRelationInput>` or `<TextWithTypeAhead>` element (container snapshots);
  - the new `slots.paper` block + `showHint` + `PaperWithHint` consts + the three new imports (widget snapshots, if any);
  - the new `judo.autocomplete.type-for-more-results` line (i18n snapshots, if any).

  Anything else (whitespace drift, unrelated regenerations) is a red flag — investigate before continuing.
- [ ] 7.4 Re-run `./mvnw clean install` and confirm green.

## 8. Manual verification on RelationTest

- [ ] 8.1 In `judo-ng/runtime/judo-tatami-tests/models/RelationTest`, run `./judo.sh start` against the regenerated bundle.
- [ ] 8.2 Repeat the §0.3 reproduction. Capture a screenshot of the dropdown with the header visible **above** the option list (italic text reading "Type for more results…") and attach it to the JIRA ticket (JNG-6409).

## 9. Commit

- [ ] 9.1 Stage and commit only the following paths:
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
- [ ] 9.2 Commit on the `feature/JNG-6409_autocomplete_more_results_hint` branch with the Definition-of-Done message above.
- [ ] 9.3 Push the branch and open a PR against `develop` referencing JNG-6409.
