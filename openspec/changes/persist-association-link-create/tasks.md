# Tasks

## 1. Persist a create through an eager non-composition link (spec: relation-management)

- [x] 1.1 Add `OpenCreateFormActionTemplateTest` for the eager association/aggregation link (persisted), the eager composition link (draft), lazy link, form page and table (unchanged), and the template wiring. Verify that it fails before the change.
- [x] 1.2 Add `isLinkCreatePersistedOnSavedOwner` to `UiActionsHelper` and use it in `OpenCreateFormAction.fragment.hbs` for `isDraft`, `ownerValidation` and the post-create refresh. Verify that `mvn -pl judo-ui-react test` passes.
- [x] 1.3 Refresh the itest snapshots once with `-DforceSnapshotOverwrite`, then run `mvn clean install` without it. Verify zero drift.

## 2. Dual-engine check

- [ ] 2.1 Run Tatami ActionGroupTest `AstronomerSingleAggregationViewPage.spec.ts` and `GalaxyAttributesAndSingleRelation.spec.ts` on React with retries off. Verify that the create calls `~update/astronomer/~create` and all three cases pass.
