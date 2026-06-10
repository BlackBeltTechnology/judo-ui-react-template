## Definition of Done

- `./mvnw clean install` exits green; the React-template's itest regenerates fixture frontends and runs them under Vitest/Playwright without new failures.
- Regenerated `judo-ui-react-itest/ActionGroupTestPro/action_group_test_pro__god/target/frontend-react/src/components/dialog/FilterDialog.tsx` contains:
  - On the Add-new-filter `<DropdownButton>`: `data-testid={\`${id}-add-new-filter\`}` (transpiled to a template-string expression).
  - On each operator `<MenuItem>`: `data-testid={\`filter-operator-${item.replace(/[A-Z]/g, '-$&').toLowerCase()}\`}` (or its transpiled equivalent).
  - No remaining reference to a `valueId` local in the `FilterOperator` component.
- A Playwright smoke check against the itest frontend confirms that opening a filter dialog renders unique `data-testid="filter-operator-equal"`, `"filter-operator-not-equal"`, etc. on the operator MenuItems (no duplicates within one open Select).
- A Playwright smoke check confirms `data-testid="<filterXmiId>-add-new-filter"` is present on the user-clickable Add-new-filter button (not only on an internal wrapper).
- Commit message: `JNG-6391 emit data-testid on filter dialog Add-new-filter button and per-operator MenuItems (F9 + F15)`.

## 1. F15 — replace duplicate operator testid

- [ ] 1.1 Open `judo-ui-react/src/main/resources/actor/src/components/dialog/FilterDialog.tsx.hbs`.
- [ ] 1.2 Locate the `FilterOperator` component (around lines 40-65). Confirm the local declarations `const valueId = \`${id}-value\`;` (line ~46-48 area) and `const operatorId = \`${id}-operator\`;`.
- [ ] 1.3 Locate the `<MenuItem>` inside `getOperatorsByFilter(filter).map(...)` (line ~50-57). Replace `data-testid={valueId}` with:
  ```jsx
  data-testid={`filter-operator-${item.replace(/[A-Z]/g, '-$&').toLowerCase()}`}
  ```
- [ ] 1.4 Grep the file for any other reference to `valueId`. If (as expected) the only reference was the one just replaced, delete the `const valueId = ...;` declaration.
- [ ] 1.5 Keep `operatorId` untouched — the operator Select wrapper at line ~48 continues to carry `data-testid={operatorId}`.

## 2. F9 — add testid to Add-new-filter DropdownButton

- [ ] 2.1 In the same file, locate the `<DropdownButton>` (around line 343) used as the "Add new filter" opener.
- [ ] 2.2 Add a new JSX prop on its opening tag, alongside the existing `id={\`${id}-dropdown\`}` line:
  ```jsx
  data-testid={`${id}-add-new-filter`}
  ```
- [ ] 2.3 Verify (by reading `judo-ui-react/src/main/resources/actor/src/components/widgets/DropdownButton.tsx.hbs`) that the component forwards unknown props to its rendered MUI `<Button>` trigger. If it does NOT, file a sub-task: patch `DropdownButton.tsx.hbs` to accept `...rest` and spread them onto the trigger. (See design.md §D6 risk note.)

## 3. Spec lockdown

- [ ] 3.1 Author `openspec/changes/testid-filter-dialog-chrome-and-operators/specs/filter-dialog-chrome-and-operators/spec.md`. One new capability with two ADDED requirements:
  - **Requirement A**: every per-Filter FilterDialog SHALL emit `data-testid={\`${id}-add-new-filter\`}` on its Add-new-filter `<DropdownButton>`.
  - **Requirement B**: every operator `<MenuItem>` SHALL emit `data-testid={\`filter-operator-<kebab>\`}` where `<kebab>` is the kebab-case form of the camelCase operator enum value.
- [ ] 3.2 Four scenarios:
  - Add-new-filter testid present on a numeric column's filter dialog
  - Operator MenuItems for a numeric-type filter — 8 unique testids match the locked enum→kebab table
  - Operator MenuItems for a string-type filter — same 8 plus `filter-operator-like` (and `filter-operator-not-like` if exposed)
  - Per-operator testid uniqueness — no two MenuItems within a single open Select share the same testid

## 4. Integration build

- [ ] 4.1 Run `./mvnw clean install` from the repo root. Confirm all reactor modules SUCCESS and itest Vitest/Playwright runs without new errors.
- [ ] 4.2 Grep the regenerated `judo-ui-react-itest/ActionGroupTestPro/action_group_test_pro__god/target/frontend-react/src/components/dialog/FilterDialog.tsx`:
  - `data-testid={\`${id}-add-new-filter\`}` appears exactly once (on the DropdownButton).
  - `data-testid={\`filter-operator-` appears inside the operator MenuItem render path.
  - `valueId` no longer appears anywhere in the FilterOperator component body.
- [ ] 4.3 Launch the itest frontend (if a runnable dev server exists in this itest, otherwise rely on Playwright headless from the itest module) and confirm via DOM inspection that:
  - Opening a filter dialog for a numeric column shows MenuItems with unique testids matching the design.md transformation table.
  - Clicking the Add-new-filter button is addressable via `[data-testid="<filterXmiId>-add-new-filter"]` (not only via role+name).

## 5. Companion catalogue change (DIFFERENT REPO — do not include in this commit)

- [ ] 5.1 The catalogue exposure lives at `judo-ui-e2e-template/openspec/changes/expose-filter-dialog-chrome-and-operators-catalogue/`. It is shipped from the `judo-ui-e2e-template` repo, NOT from this one. This task list does NOT touch the catalogue.
- [ ] 5.2 Spec back-pass (in `judo-tatami-tests`, downstream): 45 (F9) + 25 (F15) = 70 conversions from `getByRole(...)` fallbacks to `getByTestId(...)`. Tracked separately.

## 6. Commit

- [ ] 6.1 `git add` only:
  - `judo-ui-react/src/main/resources/actor/src/components/dialog/FilterDialog.tsx.hbs`
  - (only if D6's risk materialised) `judo-ui-react/src/main/resources/actor/src/components/widgets/DropdownButton.tsx.hbs`
  - `openspec/changes/testid-filter-dialog-chrome-and-operators/**`
- [ ] 6.2 Commit on the current `feature/JNG-6391_Test_Data-TestId` branch with the Definition-of-Done message above:
  `JNG-6391 emit data-testid on filter dialog Add-new-filter button and per-operator MenuItems (F9 + F15)`
- [ ] 6.3 Do NOT modify `judo-tatami-tests` or `judo-ui-e2e-template` in this commit. Catalogue side ships from its own repo; spec back-pass ships after both leaves are released.
