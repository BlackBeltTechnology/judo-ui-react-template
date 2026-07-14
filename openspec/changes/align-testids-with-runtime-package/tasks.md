## Definition of Done

- `mvn clean install -DskipPrepareNodeJS` exits green from a clean workspace; `judo-diff-checker-maven-plugin:checkDiffs` reports zero unexpected diffs after snapshot refresh.
- `grep -rn "'button::selector'" judo-ui-react/src/main/resources/actor/` returns zero lines.
- The six composite widget templates (`SingleRelationInput`, `Tags`, `TextWithTypeAhead`, `BinaryInput`, `AssociationButton`, `ModeledTabs`) emit bare `data-testid="field::<xmiId>"` on their outer wrapper (no `::container` suffix).
- `grep -rn "buildFieldTestId([^)]*, 'container')" judo-ui-react/src/main/resources/actor/src/components/widgets/ judo-ui-react/src/main/resources/actor/src/components/ModeledTabs.tsx.hbs` returns zero lines. Remaining `'container'` emissions (dialogs, DropdownButton, flex.hbs) are permitted and out of scope.
- Regenerated widget snapshots for `SingleRelationInput`, `Tags` carry `data-testid="field::<xmiId>::button::set"` on the primary open-selector button.
- `UiGeneralHelper.getElementId(EObject)` exists with a unit test covering: (i) element with non-empty `sourceId` → returns `sourceId`; (ii) element with `sourceId` feature but empty value → returns `getXMIID`; (iii) element without `sourceId` feature → returns `getXMIID`.
- Every template site that previously read `{{ getXMIID X }}` inside a `buildFieldTestId(...)` call now reads `{{ getElementId X }}`. Enforced by grep in task 5.2.
- Live cross-engine verification (optional gate, per JNG-6391 task 8.1): the same Playwright selector `page.getByTestId('field::<xmiId>::button::set').click()` succeeds on both runtime and template builds of the same actor.
- Branch: `feature/JNG-6391_align_testids_with_runtime_package`. Commit trailer: every commit references `JNG-6391`.

## 0. Baseline capture

- [ ] 0.1 On the parent branch, dump current relation-widget `data-testid` lines: `grep -n 'data-testid' judo-ui-react/src/main/resources/actor/src/components/widgets/{SingleRelationInput,Tags,TextWithTypeAhead}.tsx.hbs > /tmp/relation-widget-testids.before.txt`.
- [ ] 0.2 Dump union of `buildFieldTestId` role literals: `grep -rhoE "buildFieldTestId\([^)]+\)" judo-ui-react/src/main/resources/actor/ | sort -u > /tmp/testid-roles.before.txt`.
- [ ] 0.3 Create fresh branch `feature/JNG-6391_align_testids_with_runtime_package` from `origin/develop` (assuming JNG-6391 is already merged; otherwise from `feature/JNG-6391_unified_data_id_and_testid`).

## 1. `getElementId` Java helper

- [ ] 1.1 In `judo-ui-react/src/main/java/hu/blackbelt/judo/ui/generator/react/UiGeneralHelper.java`, add:
  ```java
  @TemplateHelper
  public static String getElementId(EObject element) {
      if (element == null) return "unknown";
      EStructuralFeature f = element.eClass().getEStructuralFeature("sourceId");
      if (f != null) {
          Object v = element.eGet(f);
          if (v instanceof String s && !s.isEmpty()) return s;
      }
      return getXMIID(element).replaceAll("@", "");
  }
  ```
- [ ] 1.2 Add unit test `UiGeneralHelperTest.getElementId_prefersSourceId` under `judo-ui-react/src/test/java/…` covering the three branches from the DoD. Use a small in-memory EMF model or mock `EObject`.
- [ ] 1.3 Verify by running `mvn -pl judo-ui-react test` — the new test passes.

## 2. G1 — `button::selector` → `button::set`

- [ ] 2.1 `judo-ui-react/src/main/resources/actor/src/components/widgets/SingleRelationInput.tsx.hbs:242` — replace `buildFieldTestId(id, 'button::selector')` with `buildFieldTestId(id, 'button::set')`.
- [ ] 2.2 `judo-ui-react/src/main/resources/actor/src/components/widgets/Tags.tsx.hbs:321` — same replacement.
- [ ] 2.3 Verify: `grep -rn "'button::selector'" judo-ui-react/src/main/resources/actor/` returns zero lines.

## 3. G2 — drop `container` role on composite widget outer wrappers

- [ ] 3.1 `SingleRelationInput.tsx.hbs:169` — `buildFieldTestId(id, 'container')` → `buildFieldTestId(id)`.
- [ ] 3.2 `Tags.tsx.hbs:196` — same replacement.
- [ ] 3.3 `TextWithTypeAhead.tsx.hbs:94` — same replacement.
- [ ] 3.4 `BinaryInput.tsx.hbs:50` — `buildFieldTestId(props.id, 'container')` → `buildFieldTestId(props.id)`.
- [ ] 3.5 `AssociationButton.tsx.hbs:63` — `buildFieldTestId(id, 'container')` → `buildFieldTestId(id)`.
- [ ] 3.6 `ModeledTabs.tsx.hbs:93` — same replacement.
- [ ] 3.7 Verify: `grep -rn "buildFieldTestId([^)]*, 'container')" judo-ui-react/src/main/resources/actor/src/components/widgets/ judo-ui-react/src/main/resources/actor/src/components/ModeledTabs.tsx.hbs` returns zero lines.
- [ ] 3.8 Verify: the remaining `'container'` emissions (dialogs, DropdownButton, flex.hbs) are untouched.

**Deferred, tracked as open questions in `design.md` D5:**

- `SingleRelationInput.tsx.hbs:278` — `buildFieldTestId(id, 'dropdown')` on `<MenuList>`. Confirm whether the DOM level maps to runtime's `dropdown` or `menu` slot via live comparison.
- Add `field::<id>::actions` slot on the icon-button strip once its runtime binding target is confirmed.

## 4. G3 — `getXMIID` → `getElementId` sweep for testids

- [ ] 4.1 Enumerate the sweep set: `grep -rn "buildFieldTestId('{{ getXMIID" judo-ui-react/src/main/resources/actor/ > /tmp/getxmiid-testid-sites.txt`.
- [ ] 4.2 For each hit, replace `{{ getXMIID X }}` with `{{ getElementId X }}` inside the `buildFieldTestId(...)` argument. Preserve every non-testid usage of `getXMIID` unchanged.
- [ ] 4.3 Also audit non-`buildFieldTestId` `data-testid` emissions that reference `getXMIID` — e.g. inline template literals `data-testid={\`field::{{ getXMIID X }}::…\`}`. Same replacement rule.
- [ ] 4.4 Verify: `grep -rn "'buildFieldTestId('{{ getXMIID" judo-ui-react/src/main/resources/actor/` returns zero lines.
- [ ] 4.5 Verify: `grep -rn "getXMIID" judo-ui-react/src/main/resources/actor/` still returns hits for non-testid uses (Pandino keys, i18n keys, container-name detection). Manually confirm each remaining hit is not inside a `data-testid` context.

## 5. Spec updates

- [ ] 5.1 Update `openspec/specs/transfer-identity/spec.md` role table:
  - Remove: `selector`, `menu::popper`, `menu::boundary`.
  - Add: `menu` (dropdown popper surface), `actions` (icon-button strip).
  - Amend `set` description: "primary open-selector-dialog button; corresponds to the runtime's `opensetselector` action-definition mapping in `packages/test-ids/src/element.ts::getFieldButtonRole`."
  - Add a normative sentence pointing to `@judo/test-ids` `getFieldButtonRole` as the source of truth for button-role labels.
- [ ] 5.2 Add a new Scenario under the "Hierarchical `data-testid` scheme" Requirement: "Element ID resolution prefers `sourceId`". Given an EObject with `sourceId = 'psm-id-alpha'` and `xmi:id = 'ui-id-beta'`, the emitted testid uses `psm-id-alpha`. Given an EObject with only `xmi:id`, the emitted testid uses `xmi:id`.

## 6. Verification gates

- [ ] 6.1 `grep -rn "'button::selector'" judo-ui-react/src/main/resources/actor/` returns zero lines.
- [ ] 6.2 `grep -rn "buildFieldTestId([^)]*, 'container')" judo-ui-react/src/main/resources/actor/src/components/widgets/ judo-ui-react/src/main/resources/actor/src/components/ModeledTabs.tsx.hbs` returns zero lines.
- [ ] 6.3 `grep -rn "buildFieldTestId('{{ getXMIID" judo-ui-react/src/main/resources/actor/` returns zero lines.
- [ ] 6.4 `openspec validate align-testids-with-runtime-package --strict` reports the change as valid.

## 7. Integration build + snapshot refresh

- [ ] 7.1 `mvn clean install -DskipPrepareNodeJS` from repo root. Capture the drift list from `judo-diff-checker-maven-plugin:checkDiffs`.
- [ ] 7.2 For each drift file, copy from `judo-ui-react-itest/<module>/target/frontend-react/<path>` to `judo-ui-react-itest/<module>/src/test/resources/snapshots/frontend-react/<path>`. One snapshot-refresh commit per itest.
- [ ] 7.3 Re-run `mvn clean install -DskipPrepareNodeJS`; `checkDiffs` SHALL report zero drifts.

## 8. Cross-engine parity spot-check

- [ ] 8.1 Rebuild `ActionGroupTest__god` under both the runtime (`judo-frontend-runtime@feature/unify-data-testid-contract`) and this template. Open a page containing a `SingleRelationInput`. In devtools, verify byte-identical `data-testid` values on: outer wrapper, input, autocomplete, dropdown, `button::set`, `menu`, `actions`.
- [ ] 8.2 Run one Playwright test authored against the runtime output against the template build. Confirm zero selector edits needed. If the runtime build is not locally available, annotate PR body and defer to follow-up.

## 9. Commit + PR

- [ ] 9.1 Commit series: `1-getElementId-helper`, `2-button-set-rename`, `3-drop-container-role`, `4-getElementId-sweep`, `5-spec-update`, `6-snapshot-refresh-<itest>` (×6). Each commit prefixed `JNG-6391`.
- [ ] 9.2 Open PR against `develop`. PR body references `70fd3317` on `judo-frontend-runtime@feature/unify-data-testid-contract` as the authoritative source, cites `dataid-template-runtime.pdf` §5 for the asymmetric-alignment rule, and enumerates the three deferred follow-ups (dialog/nav/tabs slot naming, `build*` → `get*` rename, util helpers).
