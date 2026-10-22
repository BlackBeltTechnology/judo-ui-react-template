## Definition of Done

- `mvn clean install -DskipPrepareNodeJS` exits green from a clean workspace; `judo-diff-checker-maven-plugin:checkDiffs` reports zero unexpected diffs after snapshot refresh.
- `grep -rn "data-testid={buildFieldTestId(toolBarAction.id)}" judo-ui-react/src/main/resources/actor/` returns zero lines.
- `grep -rn "data-testid={buildFieldTestId('{{ getElementId button }}')}" judo-ui-react/src/main/resources/actor/` returns zero lines.
- Every generated `ToolBarActionProps` literal in `containers/components/table/index.tsx.hbs` carries an `actionType: '<normalized>'` field.
- `buildButtonTestId` is exported from `src/utilities/transfer-id.ts` in every regenerated actor.
- `UiActionsHelper.getButtonActionType(EObject)` exists with a unit test covering the eight action-definition subclasses listed in `proposal.md` §(d).
- Live cross-engine parity: `page.getByTestId('button::<xmiId>::opencreateform')` resolves exactly one element on both a runtime build and a template build of the same actor for a table's Create toolbar action. Verified against `RelationTest__actor` if a runtime build is locally available; otherwise annotated in PR body and deferred.
- Branch: `feature/JNG-6391_align_buttons_with_runtime`. Commit trailer: every commit references `JNG-6391`.
- No new comment lines introduced into any `.hbs` template. Enforced per project rule `AGENTS.md` §Code Instructions #9.

## 0. Baseline capture

- [x] 0.1 On the parent branch (`feature/JNG-6391_unified_data_id_and_testid`), dump the current `buildFieldTestId` sites that target standalone buttons: run
  ```bash
  grep -rn "data-testid={buildFieldTestId([^)]*)}" judo-ui-react/src/main/resources/actor/ \
    | grep -iE "button|toolBarAction" > /tmp/button-testid-sites.before.txt
  ```
  Expected: eight sites listed in `proposal.md` §(c).

  Actual result (2026-07-21): grep returned **ten** sites, not eight. Two additional standalone-button sites were discovered:
  - `containers/dialog.tsx.hbs:208` — dialog action-bar `<Button>`, structurally identical to `page.tsx.hbs:99`.
  - `components/widgets/AssociationButton.tsx.hbs:63` — shared TSX component used from `containers/widget-fragments/button.hbs:48` for the `isOpenPageAction` branch.

  Both were confirmed in scope (same runtime emission class) and added to the sweep. `proposal.md` §(c) and this task file updated accordingly. See tasks 4.1a and 4.7a below.
- [x] 0.2 Dump the current union of `data-testid` occurrence counts per file: `grep -rc 'data-testid=' judo-ui-react/src/main/resources/actor/ | sort > /tmp/testid-counts.before.txt`. Used by task 5.4 for byte-parity audit.
- [x] 0.3 ~~Create fresh branch~~ — skipped per user direction; work stays on the parent branch `feature/JNG-6391_unified_data_id_and_testid` alongside the three landed sibling changes.

## 1. `buildButtonTestId` helper

- [x] 1.1 Append to `judo-ui-react/src/main/resources/actor/src/utilities/transfer-id.ts.hbs` after the existing `buildFieldTestId` block. Function body only — `AGENTS.md` §Code Instructions #9 prohibits JSDoc / explanatory comments in `.hbs`:
  ```ts
  export function buildButtonTestId(buttonId: TestIdKey, actionType?: string): string {
    const key = stringifyKey(buttonId);
    return actionType ? `button::${key}::${actionType}` : `button::${key}`;
  }
  ```
  Implemented with local variable renamed `id` → `key` to avoid shadowing `stringifyKey`'s own naming (cosmetic, matches sibling helpers).
- [x] 1.2 Verified via full `mvn clean install`; regenerated `transfer-id.ts` in every actor contains the new helper and no new comment lines.

## 2. `UiActionsHelper.getButtonActionType(EObject)` Java helper

- [x] 2.1 In `judo-ui-react/src/main/java/hu/blackbelt/judo/ui/generator/react/UiActionsHelper.java`, add:
  ```java
  @TemplateHelper
  public static String getButtonActionType(EObject button) {
      if (button == null) return "";
      EStructuralFeature f = button.eClass().getEStructuralFeature("actionDefinition");
      if (f == null) return "";
      Object ad = button.eGet(f);
      if (!(ad instanceof EObject)) return "";
      String type = ((EObject) ad).eClass().getName();
      // Byte-exact port of runtime getButtonActionType:
      // packages/test-ids/src/element.ts::getButtonActionType
      //   actionDef?.["@type"]?.replace("ui:", "").replace("ActionDefinition", "").toLowerCase()
      return type.replace("ui:", "").replace("ActionDefinition", "").toLowerCase();
  }
  ```
- [x] 2.2 Added `UiActionsHelperTest` with four `@Test` methods covering:
  - `normalizeButtonActionType_producesNormalizedStringForKnownSubclasses` — all eight action-definition subclasses.
  - `normalizeButtonActionType_stripsUiPrefixIfPresent` — `"ui:OpenCreateFormActionDefinition"` → `"opencreateform"`.
  - `normalizeButtonActionType_returnsEmptyStringForNull` — pure-core null input.
  - `getButtonActionType_returnsEmptyStringForNullButton` — EMF entry point null input.

  Implementation note: split into a pure `normalizeButtonActionType(String)` core and an EMF-facing `getButtonActionType(EObject)` wrapper, matching the pattern used by `UiGeneralHelper.resolveElementId` / `getElementId` (see `UiGeneralHelperTest`). The wrapper reads `actionDefinition` via `eGet` for robustness against future model shape changes. Non-EObject and missing-feature cases both return `""` (matches runtime's `joinTestId` filter behaviour for empty strings, per D7).
- [x] 2.3 `mvn -pl judo-ui-react test` — 15/15 tests green (4 new + 11 pre-existing).

## 3. `ToolBarActionProps<T>` shape + generation-site sweep

- [x] 3.1 `judo-ui-react/src/main/resources/actor/src/utilities/table.ts.hbs` — added `actionType: string;` to `ToolBarActionProps<T>`, immediately after `id`.
- [x] 3.2 `judo-ui-react/src/main/resources/actor/src/containers/components/table/index.tsx.hbs` — the single toolbar-action literal (inside `{{# each table.tableActionButtonGroup.buttons as |button| }}`) gained `actionType: "{{ getButtonActionType button }}",`. Fan-out to ≥ 15 emitted literals per generated actor, verified via snapshot diff on `ActionGroupTest__god/.../ViewMatterTableTableComponent/index.tsx.snapshot` and `ActionGroupTestPro__god/.../ViewGalaxyTableTableComponent/index.tsx.snapshot` — emitted values include `opencreateform`, `openaddselector`, `opensetselector`, `filter`, `refresh`, `export`, `clear`, `bulkremove`, `bulkdelete`, `inlinecreaterow`, matching runtime normalization byte-for-byte.
- [x] 3.3 Verified via `mvn clean install` snapshot regeneration; grep on `target/frontend-react/**/index.tsx` confirms every toolbar-action literal carries `actionType:`.

## 4. Emission-site sweep

- [x] 4.1 `judo-ui-react/src/main/resources/actor/src/containers/page.tsx.hbs:99` — replaced `buildFieldTestId('{{ getElementId button }}')` with `buildButtonTestId('{{ getElementId button }}', '{{ getButtonActionType button }}')`. Import added at line 22.
- [x] 4.1a `judo-ui-react/src/main/resources/actor/src/containers/dialog.tsx.hbs:208` — **added during implementation** (task 0.1 discovery). Same replacement as 4.1; loop var `button`. Import added at line 34.
- [x] 4.2 `judo-ui-react/src/main/resources/actor/src/containers/widget-fragments/button.hbs:23` — same replacement (loop var `child`). No import change needed — fragment inherits scope from `containers/{page,dialog}.tsx.hbs` / `container.tsx.hbs`.
- [x] 4.3 `judo-ui-react/src/main/resources/actor/src/containers/widget-fragments/buttongroup.hbs:28` — same (loop var `button`). No import change needed (fragment).
- [x] 4.4 `judo-ui-react/src/main/resources/actor/src/containers/widget-fragments/fab-button-group.fragment.hbs:24,45` — same replacement on both lines. No import change needed (fragment).
- [x] 4.5 `judo-ui-react/src/main/resources/actor/src/containers/widget-fragments/flex.hbs:43,111` — same replacement on both lines (disambiguated via `e.stopPropagation()` vs plain `onClick` context). No import change needed (fragment).
- [x] 4.6 `judo-ui-react/src/main/resources/actor/src/components/table/EagerTable.tsx.hbs:582` — replaced `data-testid={buildFieldTestId(toolBarAction.id)}` with `data-testid={buildButtonTestId(toolBarAction.id, toolBarAction.actionType)}`. Import updated at line 57.
- [x] 4.7 `judo-ui-react/src/main/resources/actor/src/components/table/LazyTable.tsx.hbs:744` — same replacement. Import updated at line 61.
- [x] 4.7a `judo-ui-react/src/main/resources/actor/src/components/widgets/AssociationButton.tsx.hbs:63` — **added during implementation** (task 0.1 discovery). Shared TSX component: added optional `actionType?: string` to `AssociationBaseProps`, threaded through the destructuring in `AssociationButton(...)`, and swapped `buildFieldTestId(id)` → `buildButtonTestId(id, actionType)`. Caller updated at `containers/widget-fragments/button.hbs:48` to pass `actionType="{{ getButtonActionType child }}"` (always `"openpage"` in practice — the branch is gated on `isOpenPageAction`). Import at line 9 changed from `buildFieldTestId` to `buildButtonTestId`.
- [x] 4.8 Imports: shared `fragments/container/common-imports.fragment.hbs` line 4 gained `buildButtonTestId` for all fragment consumers; direct-import files (`page.tsx.hbs`, `dialog.tsx.hbs`, `EagerTable.tsx.hbs`, `LazyTable.tsx.hbs`, `AssociationButton.tsx.hbs`) each updated in-place.
- [ ] 4.9 **Deferred** — `components/table/table-row-actions.tsx.hbs:80` left unchanged. Explicit note in PR body per `design.md` D3.

## 5. Verification gates

- [x] 5.1 `grep -rn "data-testid={buildFieldTestId(toolBarAction.id)}" judo-ui-react/src/main/resources/actor/` — zero matches.
- [x] 5.2 `grep -rn "data-testid={buildFieldTestId('{{ getElementId button }}')}" judo-ui-react/src/main/resources/actor/` — zero matches.
- [x] 5.3 `grep -rn "buildButtonTestId" judo-ui-react/src/main/resources/actor/` — nine emission sites (7 original + `dialog.tsx.hbs` + `AssociationButton.tsx.hbs`) plus the helper definition and five imports (`common-imports.fragment.hbs`, `page.tsx.hbs`, `dialog.tsx.hbs`, `EagerTable.tsx.hbs`, `LazyTable.tsx.hbs`, `AssociationButton.tsx.hbs`).
- [x] 5.4 Byte-parity audit: `diff /tmp/testid-counts.before.txt /tmp/testid-counts.after.txt` returned empty — exact per-file line-count parity.
- [ ] 5.5 `openspec validate align-buttons-with-runtime --strict` reports the change as valid.

## 6. Spec updates

- [x] 6.1 / 6.2 / 6.3 All three delta items pre-written in `openspec/changes/align-buttons-with-runtime/specs/transfer-identity/spec.md` (Requirement + five Scenarios covering the toolbar Create case, generic standalone case, bare no-action-definition case, field-scoped retention, and cross-engine Playwright addressability). Updated during implementation to enumerate the two additional emission files (`containers/dialog.tsx.hbs`, `components/widgets/AssociationButton.tsx.hbs`) plus the shared-imports fragment.

  Note on target location: the base `openspec/specs/transfer-identity/spec.md` capability file does not yet exist in the repo (created only when the parent `unify-data-id-and-testid-with-runtime` change is archived). Once that archival happens, this change's delta will merge into the base file per standard OpenSpec workflow.

## 7. Integration build + snapshot refresh

- [x] 7.1 `mvn clean install -DskipPrepareNodeJS --fail-at-end` — confirmed drifts in ActionGroupTest__god and ActionGroupTestPro__god only; other four snapshotted itests unchanged.
- [x] 7.2 Refreshed via `mvn clean install -DskipPrepareNodeJS -DforceSnapshotOverwrite=true` (the `judo-diff-checker-maven-plugin` supports overwrite mode directly — simpler than manual `cp`). Result: 12 snapshot files modified across ActionGroupTest__god (9) and ActionGroupTestPro__god (3). Emitted testids verified for `back`, `cancel`, `delete`, `refresh` in ViewGalaxyView page-container and `opencreateform` / `openaddselector` / `opensetselector` / `filter` / `export` / `refresh` / `clear` / `bulkremove` / `bulkdelete` / `inlinecreaterow` in the table toolbar.
- [x] 7.3 `mvn clean install -DskipPrepareNodeJS` (no force flag) — BUILD SUCCESS, `checkDiffs` reports zero drifts.

## 8. Cross-engine parity spot-check

- [ ] 8.1 If a runtime build of `RelationTest__actor` is locally available: open the table page for `TransferObjectA`. In devtools, verify `document.querySelector('[data-testid^="button::"]')` returns nodes with identical `data-testid` values on both engines for at least the Create, Filter, and Refresh toolbar buttons.
- [ ] 8.2 Run the migrated `judo-tatami-tests@feature/JNG-6411_Unify_data_testId_contract` `InlineEditTest.spec.ts:35` test against a template build. Expected: passes selector-level (the `testIds.button(toolbarActions.create)` call resolves). If it still fails, the residual failure is downstream (catalogue-side `actionType` gap), not template-side.
- [ ] 8.3 If runtime build is not locally available: annotate PR body with the exact selectors and expected values; defer verification to a follow-up integration ticket.

## 9. Commit + PR

- [ ] 9.1 Commit series (suggested): `1-buildButtonTestId-helper`, `2-UiActionsHelper-getButtonActionType`, `3-ToolBarActionProps-actionType-field`, `4-emission-sites-sweep`, `5-spec-update`, `6-snapshot-refresh-<itest>` (×6). Each commit prefixed `JNG-6391`.
- [ ] 9.2 Open PR against `develop`. PR body cites `judo-frontend-runtime@70fd3317` `packages/test-ids/src/element.ts::getButtonTestId` as the authoritative source, references the 2026-07-21 tatami-tests parity finding, and explicitly notes:
  - D3 row-action grammar is deferred pending runtime clarification.
  - `ToolBarActionProps<T>` gained a required `actionType` field — third-party consumers must update.
