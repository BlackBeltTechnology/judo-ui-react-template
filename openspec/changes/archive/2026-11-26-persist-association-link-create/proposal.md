# Proposal

## Why

Tatami rows E3/E4 (JNG-6411) fail on React only. ActionGroupTest's Galaxy view has an eager `astronomer` link whose UI relation is `AGGREGATION` but which the server treats as an association. `OpenCreateFormAction.fragment.hbs` stages every create through an eager link as a draft (`isActionParentEagerElement`), so the new astronomer is embedded in the owner payload and `~update/astronomer/~create` is never called. The backend refuses it:

`IllegalStateException: On association there can be any attribute / reference inside the payload: ActionGroupTest.View.Galaxy#astronomer` (`UpdatePayloadDaoProcessor.java:423`, `/tmp/jng6411-e4-karaf.log`).

Effects (HARs `/tmp/jng6411-e3-react.har`, `/tmp/jng6411-e4-react.har`):

- E3 `AstronomerSingleAggregationViewPage.spec.ts:131`: the owner stays dirty and Save stays visible.
- E4 `GalaxyAttributesAndSingleRelation.spec.ts:263` and `:322`: after unset + create, `~validate` answers 500 and the field stays empty, because the refresh is skipped while `editMode` is set.

The runtime already does this correctly (Tatami archive `2026-10-22-unified-testid-e2e-runtime/tasks.md` 5.3a.3, 5.3a.10). `judo-frontend-runtime` `packages/components/src/renderers/LinkRenderer.tsx:695-700` (`stagesCreateLocally`) has this rule: a create through an eager link is staged only while the owner is unsaved, or when the relation is a `COMPOSITION`. Otherwise it is persisted through the relation's own endpoint.

## What Changes

- New helper `UiActionsHelper.isLinkCreatePersistedOnSavedOwner(page, action)`. It is true for an open-create-form action of an eager link (not a table) on a view page whose relation is not a composition.
- For such actions, `OpenCreateFormAction.fragment.hbs` computes `stagesDraft = isDraft || !data.__signedIdentifier` at run time. A saved owner opens the create form with `isDraft: false` and no `ownerValidation`, so the form calls `create<Relation>` (`~update/<relation>/~create`). An unsaved owner still stages.
- After a persisted create, the page reloads through its refresh action even in edit mode, so the field shows the returned target after an unset.
- Unchanged: tables, eager composition links, lazy links, form pages.

## Capabilities

### Modified Capabilities

- `relation-management`: adds the create-through-link persistence rule.

## Impact

- `UiActionsHelper.java`, `pages/actions/OpenCreateFormAction.fragment.hbs`, new `OpenCreateFormActionTemplateTest`.
- Snapshots: the Galaxy `AccessViewPage` of ActionGroupTest and ActionGroupTestPro (the `astronomer` link) change. RelationTest is unchanged.
