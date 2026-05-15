## 1. Failing-baseline snapshot capture (TDD)

- [x] 1.1 Run `mvn -pl judo-ui-react-itest/ActionGroupTest/action_group_test__god -am clean install` against the current code (no fix yet) and confirm the existing `ViewGalaxyForm.tsx.snapshot` still matches. This is the green pre-baseline. **Result:** confirmed by inspection — no diff-checker entry covered the affected form-action file (`src/dialogs/God/God/Galaxies/AccessFormPage/index.tsx`), so the bug was invisible to snapshot regression. This is itself a verification gap, addressed in §5.
- [x] 1.2 Locate the page-action snapshot file for the Galaxy view (where the `Update` / `Create` action body lives). Inspect it and confirm it does NOT currently import `passesLocalValidation` and does NOT build a `requiredByRecord` for `discoveryConfirmed`. Record the pre-fix line numbers / shape in a scratch note for diff comparison. **Result:** the form-action body for required-attribute Galaxy lives in `src/dialogs/God/God/Galaxies/AccessFormPage/index.tsx`, which had **no snapshot file at all** — confirming both (a) pre-fix absence of the guard and (b) the snapshot-coverage gap. The view-page action (`src/pages/.../AccessViewPage/index.tsx.snapshot`) was already snapshotted with no `passesLocalValidation` import; the fix correctly leaves that file untouched (no required-attribute trinary on the view page).
- [x] 1.3 Manually `pnpm dev` the generated app and reproduce the bug: open the Galaxy form, leave `discoveryConfirmed` empty, click `Save`, observe the request hits the backend and the failure round-trips. (Manual baseline; user-run.)

## 2. Java helper change

- [x] 2.1 In `judo-ui-react/src/main/java/hu/blackbelt/judo/ui/generator/react/UiPageHelper.java`, extend `getRequiredByWidgetsForPage(PageDefinition)` so the predicate matches both:
  - elements with non-null `getRequiredBy()` (current behavior), and
  - `TrinaryLogicCombo` elements whose bound `attributeType.isIsRequired()` is `true`.
  Keep deduplication via `LinkedHashSet` and the final `FQName`-sorted ordering.
- [x] 2.2 Add a private helper method `isStaticallyRequiredTrinaryCombo(VisualElement)` that encapsulates the new clause, with a Javadoc one-liner referencing the spec scenario. **Result:** added with Javadoc cross-referencing `input-widgets#Trinary Logic Combo` scenario `Submit blocked when required boolean is empty`.
- [x] 2.3 Confirm `hasPageRequiredBy(...)` automatically picks up the new case (it already delegates to `getRequiredByWidgetsForPage(...).isEmpty()`). No edit needed; verify by inspection. **Verified.**
- [x] 2.4 If a unit-test class exists for `UiPageHelper`, add a JUnit 5 case that loads a tiny in-memory `PageDefinition` containing one statically-required `TrinaryLogicCombo` and asserts `getRequiredByWidgetsForPage` returns it. If no such test class exists, skip; coverage is provided by the integration snapshot in §4. **Skipped:** only `MaskEntryTest` exists under `judo-ui-react/src/test/java/`; no `UiPageHelper` unit-test harness. Coverage provided by the integration snapshot in §5.

## 3. Handlebars fragment change

- [x] 3.1 Edit `judo-ui-react/src/main/resources/actor/src/fragments/page/local-validate.fragment.hbs` so that the existing `each (getRequiredByWidgetsForPage page)` loop emits, for the `attributeType` branch, a literal `true` value when `child.requiredBy` is null (i.e. statically-required case). Keep the `!!data.<requiredBy>` form for the existing `requiredBy` case. Mirror the same conditional in the `relationType` branch defensively, even though no relation widget participates today.
- [x] 3.2 Verify by inspection that no other `*.fragment.hbs` references the same loop in a way that would conflict (`grep -rn getRequiredByWidgetsForPage judo-ui-react/src/main/resources`). **Verified:** the helper has exactly one consumer — `local-validate.fragment.hbs` itself.

## 4. Component testid preservation

- [x] 4.1 Edit `judo-ui-react/src/main/resources/actor/src/components/widgets/TrinaryLogicCombobox.tsx.hbs`. Replace the current `{!required && (...)}` block with a `required ? <hidden anchor MenuItem> : <visible Unknown MenuItem>` ternary as described in design Decision 3. The hidden anchor MUST keep `data-testid={`${id}-undefined`}` and MUST set `sx={ { display: 'none' } }`, `aria-hidden="true"`, `disabled`, and a sentinel `value="__hidden__"`.
- [x] 4.2 Confirm the existing `value` resolution (`(required && (value === null || value === undefined)) ? '' : (TRINARY_LOGIC.get(value) ?? '')`) is left intact. **Verified** in regenerated `TrinaryLogicCombobox.tsx`.
- [x] 4.3 Confirm `data-testid={`${id}-true`}` and `data-testid={`${id}-false`}` are unchanged. **Verified.**

## 5. Snapshot refresh & verification

- [x] 5.1 Run `mvn -pl judo-ui-react-itest/ActionGroupTest/action_group_test__god -am clean install` and let the diff-checker fail on changed files. **Result:** the build passed without diff-checker complaints because the form-action file (`src/dialogs/God/God/Galaxies/AccessFormPage/index.tsx`) was not in the diff-checker `<sources>` list at all — i.e., the regression-protection gap that originally enabled this bug to ship. Resolved in 5.2/5.3.
- [x] 5.2 Inspect the diff. **Result of inspection:**
  - `src/dialogs/God/God/Galaxies/AccessFormPage/index.tsx` correctly imports `passesLocalValidation`, builds `const requiredByRecord = { discoveryConfirmed: true }`, and calls the guard at the top of the create/update action — verified by `grep -n 'passesLocalValidation\|requiredByRecord\|discoveryConfirmed: true' target/frontend-react/.../AccessFormPage/index.tsx`.
  - `target/frontend-react/src/components/widgets/TrinaryLogicCombobox.tsx` now contains the hidden anchor MenuItem (`value={'__hidden__'}`, `data-testid={`${id}-undefined`}`, `sx={ { display: 'none' } }`, `aria-hidden`, `disabled`).
  - `ViewGalaxyForm.tsx`, `AccessViewPage/index.tsx`, and other tracked snapshots are byte-identical to their committed copies (verified via `diff -q`) — no view-page action churn, as expected.
- [x] 5.3 Copy the updated files from `target/frontend-react/` to `src/test/resources/snapshots/frontend-react/` per `AGENTS.md` step 5. **Result:** added `src/dialogs/God/God/Galaxies/AccessFormPage/index.tsx` to the `<sources>` list in `judo-ui-react-itest/ActionGroupTest/action_group_test__god/pom.xml` (it was missing — that's why the bug was invisible to regression) and committed the matching `.snapshot` file. The new snapshot locks in the `passesLocalValidation` import (line 42), the `requiredByRecord = { discoveryConfirmed: true, ... }` literal (lines 184-185), and the `validationResult` early-return (lines 190-194).
- [x] 5.4 Re-run the same Maven goal; confirm a clean diff-checker pass. **Result:** user-confirmed build success after task-list update; new snapshot is byte-identical to the regenerated file by construction, so diff-checker passes cleanly.
- [ ] 5.5 Run `mvn -q clean install` from the repository root to confirm no other itest module is affected. If unexpected diffs appear, identify them: any other itest with a statically-required `TrinaryLogicCombo` should be acknowledged here, otherwise investigate the helper. **Pending:** user-run; no other itest has a statically-required `TrinaryLogicCombo` today, so no diffs expected.

## 6. Manual verification on the running app

- [x] 6.1 `pnpm dev` the `ActionGroupTest/action_group_test__god/target/frontend-react` app.
- [x] 6.2 Open the Galaxy form. Confirm the required combo (`discoveryConfirmed`) renders empty by default and shows two selectable options.
- [x] 6.3 Leave `discoveryConfirmed` empty and click `Save`. Confirm:
  - the network tab shows NO outgoing request to the backend service for this submit;
  - the trinary combo field renders the standard `error` border and `helperText` set to the i18n value of `judo.error.validation-failed.MISSING_REQUIRED_ATTRIBUTE`.
- [x] 6.4 Pick `Yes`, click `Save`. Confirm the backend call goes out as before.
- [x] 6.5 Open a filter dialog or column filter on a boolean attribute that has `isRequired = true`. Confirm the filter combo still shows three selectable options (`Yes`, `No`, `Unknown`) and submission does not trigger `MISSING_REQUIRED_ATTRIBUTE` because the filter form action did not include the attribute in `requiredByRecord`.
- [x] 6.6 Inspect the DOM and confirm `data-testid="<id>-undefined"` exists on the required combo (as a hidden node) and on the optional combo (as a visible `Unknown` MenuItem).

## 7. Spec & docs

- [x] 7.1 Run `openspec validate trinary-combo-required-submit-guard --strict`. Resolve any reported issues. **Result:** valid.
- [x] 7.2 Run `openspec show trinary-combo-required-submit-guard` to verify the change renders correctly.
- [x] 7.3 Defer applying the modified `Trinary Logic Combo` requirement to `openspec/specs/input-widgets/spec.md` until archive — `openspec archive` performs the merge automatically (same convention as the previous archive `2026-05-04-trinary-combo-required`).

## 8. Follow-ups (out of scope here)

- [x] 8.1 Open a separate proposal to broaden the submit guard to all statically-required scalar inputs (text, numeric, date, enum, …). The fragment shape introduced here generalizes; only the helper predicate would need to widen.
- [ ] 8.2 Open a separate proposal to mirror the `discoveryConfirmed` / `archived` demo into `ActionGroupTestPro` for community/Pro snapshot parity.
- [ ] 8.3 Consider whether `actions.isXxxRequired(...)` should participate in submit-guard. Requires building part of `requiredByRecord` from runtime action calls; not addressed here.
