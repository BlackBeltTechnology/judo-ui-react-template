## Definition of Done

- `./mvnw clean install` exits green; the React-template's itest regenerates fixture frontends and runs them under Vitest/Playwright without new failures.
- Regenerated `judo-ui-react-itest/ActionGroupTestPro/action_group_test_pro__god/target/frontend-react/src/containers/Star/Table/components/StarTableTableComponent/index.tsx` contains `hiddenInSelectorMode: true,` inside the action object whose `name: 'openCreateFormAction'`, and does NOT contain it on any other action.
- Regenerated `LazyTable.tsx` contains the `.filter((toolBarAction) => !(containerIsSelector && toolBarAction.hiddenInSelectorMode))` chain before the toolbar `.map(...)`.
- Regenerated `EagerTable.tsx` contains the analogous filter using `isSelectorTable` and destructures the prop.
- Commit message: `JNG-6391 hide form-opening Create button in selector context (F10 dedupe)`.

## 1. Type extension

- [x] 1.1 In `judo-ui-react/src/main/resources/actor/src/utilities/table.ts.hbs`, locate the `ToolBarActionProps<T>` interface (declared around line 43). Add `hiddenInSelectorMode?: boolean;` as a new optional property just before the closing brace, with a brief comment cross-referencing F10.

## 2. Emit the static flag in TableComponent

- [x] 2.1 In `judo-ui-react/src/main/resources/actor/src/containers/components/table/index.tsx.hbs`, locate the `toolBarActions` array emission (line ~215, inside the `{{# each table.tableActionButtonGroup.buttons as |button| }}` block).
- [x] 2.2 Inside each emitted action object literal, after the `isBulk:` line and before the `{{# if button.confirmation }}` block, add a conditional Handlebars block:
  ```handlebars
  {{# if button.actionDefinition.isOpenCreateFormAction }}
  hiddenInSelectorMode: true,
  {{/ if }}
  ```
- [x] 2.3 Add a single-line `//` comment in the .hbs immediately above the conditional block explaining the F10 origin and pointing at the LazyTable/EagerTable filter sites.

## 3. Filter in LazyTable

- [x] 3.1 In `judo-ui-react/src/main/resources/actor/src/components/table/LazyTable.tsx.hbs`, locate the toolbar render loop (line ~737, the `toolBarActions.map((toolBarAction: ToolBarActionProps<T>) => ... )` inside `<GridToolbarContainer>`).
- [x] 3.2 Change the call into a `.filter(...).map(...)` chain. The filter predicate: `(toolBarAction: ToolBarActionProps<T>) => !(containerIsSelector && toolBarAction.hiddenInSelectorMode)`. `containerIsSelector` is already destructured at line 143 — no further wiring needed.

## 4. Filter in EagerTable

- [x] 4.1 In `judo-ui-react/src/main/resources/actor/src/components/table/EagerTable.tsx.hbs`, locate the props destructuring block (line ~135 area). Add `isSelectorTable,` to the destructured list. The prop is already declared in the interface at line 89.
- [x] 4.2 Locate the toolbar render loop (line ~575). Apply the analogous `.filter(...).map(...)` change using `isSelectorTable` (not `containerIsSelector` — EagerTable's prop name).

## 5. Spec lockdown

- [x] 5.1 Author `openspec/changes/dedupe-create-button-in-selector/specs/table-toolbar-rendering/spec.md` declaring one new requirement ("Selector-mode tables suppress form-opening Create buttons"). Two scenarios:
  - Form-opening Create button hidden when table is rendered inside a selector dialog
  - Form-opening Create button visible when table is rendered as a regular page

## 6. Integration build

- [x] 6.1 Run `./mvnw clean install` from the repo root. Confirm:
  - All reactor modules report SUCCESS.
  - The itest module's `pnpm install` step and Vitest/Playwright run do not fail with new errors related to this change.
- [x] 6.2 Grep the regenerated `judo-ui-react-itest/ActionGroupTestPro/action_group_test_pro__god/target/frontend-react/src/containers/Star/Table/components/StarTableTableComponent/index.tsx` for `hiddenInSelectorMode: true,`. Confirm it appears exactly under `name: 'openCreateFormAction'` and nowhere else.
- [x] 6.3 Grep the regenerated `LazyTable.tsx` and `EagerTable.tsx` (under the same itest path) for `hiddenInSelectorMode` in the filter call.

## 7. Commit

- [x] 7.1 `git add` only:
  - `judo-ui-react/src/main/resources/actor/src/utilities/table.ts.hbs`
  - `judo-ui-react/src/main/resources/actor/src/containers/components/table/index.tsx.hbs`
  - `judo-ui-react/src/main/resources/actor/src/components/table/LazyTable.tsx.hbs`
  - `judo-ui-react/src/main/resources/actor/src/components/table/EagerTable.tsx.hbs`
  - `openspec/changes/dedupe-create-button-in-selector/**`
- [x] 7.2 Commit on the current `feature/JNG-6391_dedupe_create_button_in_selector` branch with the Definition-of-Done message above.
- [x] 7.3 Do NOT modify downstream `judo-tatami-tests` or `judo-ui-e2e-template` in this commit. F10's catalogue side needs no change (per design.md Non-Goal #1 of the proposal — both leaves continue to exist in the catalogue).
