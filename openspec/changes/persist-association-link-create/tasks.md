# Tasks

## 1. Persist a create through an eager non-composition link (spec: relation-management)

- [x] 1.1 Add `OpenCreateFormActionTemplateTest` for the eager association/aggregation link (persisted), the eager composition link (draft), lazy link, form page and table (unchanged), and the template wiring. Verify that it fails before the change.
- [x] 1.2 Add `isLinkCreatePersistedOnSavedOwner` to `UiActionsHelper` and use it in `OpenCreateFormAction.fragment.hbs` for `isDraft`, `ownerValidation` and the post-create refresh. Verify that `mvn -pl judo-ui-react test` passes.
- [x] 1.3 Refresh the itest snapshots once with `-DforceSnapshotOverwrite`, then run `mvn clean install` without it. Verify zero drift.

## 1b. The selector's create follows the same rule (spec: relation-management)

- [x] 1b.1 Extend `OpenCreateFormActionTemplateTest`: the set selector of an eager non-composition link matches the rule, a relation selector reports a create with `openCreated`, and `OpenSetSelectorAction` reloads instead of staging. Verify that it fails before the change (`/tmp/jng6411-g3e-react-e4-red.log`).
- [x] 1b.2 Extend `isLinkCreatePersistedOnSavedOwner` to `OpenSetSelectorAction`. Make the relation selector's create report `onSubmit([created], 'submit', true)`, and make `OpenSetSelectorAction.fragment.hbs` reload a saved owner (`refresh(true)`) instead of calling `storeDiff`.
- [x] 1b.3 Refresh the itest snapshots once with `-DforceSnapshotOverwrite`, then rerun without it. Verify zero drift.

## 2. Dual-engine check

- [ ] 2.1 Run Tatami ActionGroupTest `AstronomerSingleAggregationViewPage.spec.ts` and `GalaxyAttributesAndSingleRelation.spec.ts` on React with retries off. Verify that the create calls `~update/astronomer/~create` and all three cases pass.
