## Definition of Done

- `./mvnw clean install` exits green; the React-template's itest regenerates fixture frontends and runs them under Vitest / Playwright without new failures.
- Regenerated `Tags.tsx` (under any `judo-ui-react-itest/.../target/frontend-react/src/components/widgets/Tags.tsx`) contains the three new `data-testid` attributes (`${id}-clear-all`, `${id}-inline-create`, `${id}-open-selector`) on the three IconButtons inside the `TagsButtonGroup`.
- Regenerated `SingleRelationInput.tsx` contains `data-testid={`${id}-${buttonProp.name}`}` on the `buttonProps`-mapped IconButton and `data-testid={`${id}-dropdown-toggle`}` on the chevron toggle.
- No existing testid is removed or modified. The existing `${id}-view` (Tags.tsx line 253), `${id}-delete` (line 223), `${id}-open-dialog` (line 239), `${id}-download` (line 263), and root `data-testid={id}` (line 200) remain untouched.
- Commit message: `JNG-6391 emit data-testid on TabularReferenceField inline action buttons (F6)`.

## 1. Pre-implementation verification

- [ ] 1.1 Verify the `id` prop on `<Tags>` is always bound to the parent TabularReferenceField's XMI ID. Grep all `<Tags ` JSX usages under `judo-ui-react/src/main/resources/actor/**/*.hbs`. Expected: exactly one call site in `containers/components/tag/index.tsx.hbs`, passing the same XMI ID that is also used as the field container's `data-testid` (`{{ getXMIID table }}`). Record the finding in a brief PR-description note.
- [ ] 1.2 Verify the `id` prop on `<SingleRelationInput>` is bound analogously. Grep all `<SingleRelationInput ` usages. Confirm `id={...}` is the XMI ID of the parent relation field.
- [ ] 1.3 Confirm no existing `data-testid={`${id}-clear-all`}` / `-inline-create` / `-open-selector` / `-dropdown-toggle` / `-set` / `-create` substring exists anywhere under `judo-ui-react/src/main/resources/actor/` (would indicate a prior collision).

## 2. Tags.tsx.hbs — three new testids

- [ ] 2.1 In `judo-ui-react/src/main/resources/actor/src/components/widgets/Tags.tsx.hbs`, locate the `TagsButtonGroup` block at line 302-324.
- [ ] 2.2 On line 310 (the Clear-all IconButton inside `{!readOnly && onClearDialogsClick ? (...)`), add `data-testid={`${id}-clear-all`}` as the first attribute on the `<IconButton>`.
- [ ] 2.3 On line 315 (the Inline-create IconButton inside `{!readOnly && onCreateDialogsClick ? (...)`), add `data-testid={`${id}-inline-create`}` as the first attribute.
- [ ] 2.4 On line 320 (the Open-selector IconButton inside `{!readOnly && onSearchDialogsClick ? (...)`), add `data-testid={`${id}-open-selector`}` as the first attribute.

## 3. SingleRelationInput.tsx.hbs — two new testids

- [ ] 3.1 In `judo-ui-react/src/main/resources/actor/src/components/widgets/SingleRelationInput.tsx.hbs`, locate the `buttonProps.filter(...).filter(...).map((buttonProp) => ...)` block (lines ~222-238).
- [ ] 3.2 On the `<IconButton>` opening tag inside `.map((buttonProp) => (...))` (line ~225), add `data-testid={`${id}-${buttonProp.name}`}` as the first attribute.
- [ ] 3.3 On the chevron dropdown toggle IconButton at line ~240 (`<IconButton className={`${name}-dropdown`} ...>`), add `data-testid={`${id}-dropdown-toggle`}` as the first attribute.

## 4. Spec lockdown

- [ ] 4.1 Author `openspec/changes/testid-tabular-reference-field-actions/specs/tabular-reference-field-actions/spec.md` declaring the three ADDED Requirements:
  - R1: Tags widget emits inline action testids
  - R2: SingleRelationInput widget emits inline action testids
  - R3: No conflict with existing testids (cohabitation requirement)
- [ ] 4.2 Each Requirement has 1-2 scenarios. Total 4-5 scenarios across the three Requirements (see spec.md).

## 5. Integration build

- [ ] 5.1 Run `./mvnw clean install` from the React-template repo root. Confirm:
  - All reactor modules report SUCCESS.
  - The itest module's `pnpm install` step and Vitest / Playwright runs do not report new errors related to this change.
- [ ] 5.2 Grep the regenerated `judo-ui-react-itest/ActionGroupTestPro/action_group_test_pro__god/target/frontend-react/src/components/widgets/Tags.tsx` for the three new testid templates. Confirm each appears exactly once.
- [ ] 5.3 Grep the regenerated `.../components/widgets/SingleRelationInput.tsx` for the two new testid templates.
- [ ] 5.4 Confirm no pre-existing testid was removed: re-grep for `data-testid={`${id}-view`}`, `data-testid={`${id}-delete`}`, `data-testid={`${id}-open-dialog`}`, `data-testid={`${id}-download`}` in the regenerated `Tags.tsx` — each must still appear once.

## 6. Commit

- [ ] 6.1 `git add` only:
  - `judo-ui-react/src/main/resources/actor/src/components/widgets/Tags.tsx.hbs`
  - `judo-ui-react/src/main/resources/actor/src/components/widgets/SingleRelationInput.tsx.hbs`
  - `openspec/changes/testid-tabular-reference-field-actions/**`
- [ ] 6.2 Commit on the current `feature/JNG-6391_Test_Data-TestId` branch with the Definition-of-Done message above.
- [ ] 6.3 Do NOT modify the companion `judo-ui-e2e-template` catalogue in this commit — that is a separate Class II companion change folder.
- [ ] 6.4 Do NOT modify downstream `judo-tatami-tests` in this commit. Spec-conversion sites are addressed in a follow-up after both halves of this Class II pair ship.
