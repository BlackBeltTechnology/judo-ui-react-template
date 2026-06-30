## Definition of Done

- `./mvnw clean install` exits green; the React-template's itest regenerates fixture frontends and runs them under Vitest/Playwright without new failures.
- The regenerated fixture frontend (e.g. `judo-ui-react-itest/ActionGroupTestPro/action_group_test_pro__god/target/frontend-react/src/components/table/SelectCheckbox.tsx`) exists and exports `CustomCheckbox`.
- The regenerated `LazyTable.tsx` and `EagerTable.tsx` both import `CustomCheckbox` from `./SelectCheckbox` and pass it under `slots.baseCheckbox`, with `slotProps.baseCheckbox.testIdPrefix` set to `uniqueId`.
- A spot-check render of any fixture grid with `checkboxSelection={true}` shows `data-testid="<tableXmiId>-select-all"` on the header checkbox and `data-testid="<tableXmiId>-select-row-<rowId>"` on each row's selection checkbox.
- A spot-check render of any fixture grid with `checkboxSelection={false}` shows NO `__check__` column and zero invocations of the wrapper.
- Commit message: `JNG-6391 emit data-testid on DataGrid row-select / header-select checkboxes (F3)`.
- Companion catalogue change at `judo-ui-e2e-template/openspec/changes/expose-datagrid-select-actions-catalogue/` is cross-referenced and scheduled to ship in lockstep (Class II paired).

## 1. Inspect the existing `slotProps` usage

- [x] 1.1 Grep `judo-ui-react/src/main/resources/actor/src/components/table/LazyTable.tsx.hbs` for `slotProps={{` and `slots={{`. Note the exact line range and indentation. Confirm whether `slots` is already present (if not, the change adds it; if yes, the change merges into it).
- [x] 1.2 Repeat 1.1 for `EagerTable.tsx.hbs`.
- [x] 1.3 Confirm `uniqueId` is destructured in both files' props block. If not destructured but declared, add it to the destructure list (analogous to F10's `isSelectorTable` handling).

## 2. Create the wrapper component

- [x] 2.1 Author `judo-ui-react/src/main/resources/actor/src/components/table/SelectCheckbox.tsx.hbs` per the implementation sketch in `design.md`. Export `CustomCheckbox` as a `forwardRef`-wrapped component that:
  - Accepts `testIdPrefix`, `rowId`, `__identifier`, and the rest of MUI's `CheckboxProps`.
  - Computes the resolved `data-testid` per D2 / D3 in `design.md`.
  - Delegates rendering to MUI's `<Checkbox>` with `{...rest}` and `ref` forwarded.
- [x] 2.2 Add an inline comment block at the top of the file citing F3 and the MUI v8 slots documentation URL.

## 3. Wire the slot in LazyTable

- [x] 3.1 In `judo-ui-react/src/main/resources/actor/src/components/table/LazyTable.tsx.hbs`, add `import { CustomCheckbox } from './SelectCheckbox';` to the import block at the top of the file (alongside the other relative `./` imports).
- [x] 3.2 At the `<DataGrid>` JSX (around line ~675–680, adjacent to `checkboxSelection` at line 713), merge the new keys:
  - `slots={{ ...(any existing keys), baseCheckbox: CustomCheckbox }}`
  - `slotProps={{ ...(any existing keys), baseCheckbox: { testIdPrefix: uniqueId } }}`
- [x] 3.3 Do NOT touch `checkboxSelection={checkboxSelection !== false}` at line 713.

## 4. Wire the slot in EagerTable

- [x] 4.1 Repeat step 3.1 in `EagerTable.tsx.hbs` (import).
- [x] 4.2 Repeat step 3.2 in `EagerTable.tsx.hbs` (around line ~520–525, adjacent to `checkboxSelection` at line 557).
- [x] 4.3 Do NOT touch `checkboxSelection={checkboxSelection !== false}` at line 557.

## 5. Spec lockdown

- [x] 5.1 Author `openspec/changes/testid-datagrid-row-select-checkboxes/specs/datagrid-row-select-testids/spec.md` declaring one new requirement and three scenarios per `proposal.md` § Capabilities.

## 6. Integration build

- [x] 6.1 Run `./mvnw clean install` from the repo root. Confirm all reactor modules report SUCCESS and the itest's `pnpm install` + Vitest / Playwright run does not surface new failures attributable to this change.
- [x] 6.2 Grep the regenerated fixture frontend (e.g. `judo-ui-react-itest/ActionGroupTestPro/action_group_test_pro__god/target/frontend-react/src/components/table/SelectCheckbox.tsx`) for the file's existence and for `data-testid={testId}`.
- [x] 6.3 Grep the regenerated `LazyTable.tsx` and `EagerTable.tsx` (under the same itest path) for `baseCheckbox: CustomCheckbox` and `testIdPrefix: uniqueId`.
- [x] 6.4 (Optional, if a Playwright fixture exercises selection) Run the fixture's Playwright suite and confirm the new testids appear in the rendered DOM via `page.locator('[data-testid$="-select-all"]')`.

## 7. Commit

- [x] 7.1 `git add` only:
  - `judo-ui-react/src/main/resources/actor/src/components/table/SelectCheckbox.tsx.hbs`
  - `judo-ui-react/src/main/resources/actor/src/components/table/LazyTable.tsx.hbs`
  - `judo-ui-react/src/main/resources/actor/src/components/table/EagerTable.tsx.hbs`
  - `openspec/changes/testid-datagrid-row-select-checkboxes/**`
- [x] 7.2 Commit on the current `feature/JNG-6391_Test_Data-TestId` branch with the Definition-of-Done message: `JNG-6391 emit data-testid on DataGrid row-select / header-select checkboxes (F3)`.
- [x] 7.3 Do NOT modify the downstream `judo-tatami-tests` repo or the `judo-ui-e2e-template` repo in this commit. The catalogue companion ships from `judo-ui-e2e-template/openspec/changes/expose-datagrid-select-actions-catalogue/` as a separate Class II-paired commit.
