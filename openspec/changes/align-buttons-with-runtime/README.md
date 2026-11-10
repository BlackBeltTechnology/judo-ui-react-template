# align-buttons-with-runtime

Close a `data-testid` byte-parity gap between this template and the runtime for **standalone buttons, button groups, and table-toolbar buttons**. The runtime emits `button::<xmiId>::<actionType>` on every such DOM node (`getButtonTestId` in `@judo/test-ids/src/element.ts`), while this template currently emits `field::<xmiId>` (`buildFieldTestId(id)`). Same DOM element, different testid — a shared Playwright selector cannot address it on both engines.

Discovered 2026-07-21 during runtime-vs-React parity testing of the migrated tatami-tests Playwright suite (`judo-tatami-tests@feature/JNG-6411_Unify_data_testId_contract`). The migrated helper `TestIdHelper.button()` produces `button::<id>::<action>`; runtime DOM matches; React DOM does not.

**JIRA**: JNG-6391-follow-up (name pending; extends the JNG-6391 line)
**Depends on**: `unify-data-id-and-testid-with-runtime`, `align-testids-with-runtime-package`, `align-domain-testids-with-runtime` (all code-complete on `feature/JNG-6391_unified_data_id_and_testid`).
**Authoritative source**: `judo-frontend-runtime@feature/unify-data-testid-contract` — the six renderers listed in `proposal.md` §Why, plus `packages/test-ids/src/element.ts` (`getButtonTestId`, `getButtonActionType`).
