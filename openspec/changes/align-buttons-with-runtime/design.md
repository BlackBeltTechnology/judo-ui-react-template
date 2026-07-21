## Decisions

### D1. New `button::` domain, not a nested `field::<id>::button` form.

The runtime emits `button::<xmiId>::<actionType>` on every standalone button — a top-level domain sibling to `field::`, `table::`, `dialog::`, `nav::`, `tabs::`. It does not nest under `field::`. Verified across the six renderers listed in `proposal.md` §Why.

This template must adopt the same top-level domain rather than reusing the existing `field::` prefix, because:
- The runtime's grammar is authoritative per `dataid-template-runtime.pdf` §5.
- A standalone button is not a "field" — it has no bound attribute, no input, no autocomplete. Reusing `field::` conflates two semantically distinct DOM classes.
- A shared Playwright selector `page.getByTestId('button::<id>::opencreateform')` must resolve on both engines.

Rejected: emit `field::<id>::button::<action>` on standalone buttons. Reason: doesn't byte-match the runtime; introduces a phantom "field" wrapper that has no runtime counterpart.

Rejected: emit `button::<id>` without the `<actionType>` suffix. Reason: the runtime always appends it (via `getButtonActionType`). A tail-less form differs from the runtime for every button in the DOM.

### D2. The `<actionType>` suffix is derived at generation time, not at render time.

The runtime derives the suffix at render time by reading `button.actionDefinition["@type"]` off the parsed model. This template's rendered `ToolBarActionProps<T>` object is a code-gen JS literal without an `actionDefinition` field, so a corresponding render-time derivation is impossible.

Chosen: compute the normalized `actionType` string at Java-helper time from the EMF `EObject`'s `actionDefinition` reference, using the byte-exact port of runtime `getButtonActionType`. Emit it as a string field on the generated `ToolBarActionProps<T>` literal (per §(b) in `proposal.md`) and as a template literal in the eight emission sites (per §(c)).

Rejected: emit `actionDefinition['@type']` verbatim onto the JS object and let a helper normalize at render time. Reason: adds runtime work for a value that's static at generation time; puts a runtime dependency on the runtime's normalization scheme.

Rejected: keep the normalization scheme in a separate `judo-ui-react-utilities` package. Reason: over-engineering; a single-file Java helper suffices.

### D3. Row-action buttons remain in the row-scoped grammar for now (OPEN).

The runtime is self-inconsistent (see `proposal.md` §Open Questions D3): `RowActionCell.tsx` uses `getButtonTestId(button)` (→ `button::<xmiId>::<actionType>`) while `@judo/test-ids` exports `getRowActionTestId(tableId, transfer, actionName, index?)` (→ `table::<tableId>::row::<transferId>::action::<actionName>`).

Chosen for this change: leave `components/table/table-row-actions.tsx.hbs:80` unchanged and note the discrepancy in the PR body. Rationale: fixing this template to match either interpretation without runtime clarification risks locking in the wrong grammar. The current row-scoped form is at least self-consistent with `buildRowTestId`.

Rejected: switch to `button::<xmiId>::<actionType>` matching `RowActionCell.tsx`. Reason: presumes the runtime will fix `getRowActionTestId` to dead-code status; unproven.

Rejected: switch to `getRowActionTestId`-shape output. Reason: presumes the runtime will fix `RowActionCell.tsx` to use it; unproven.

**Follow-up JIRA ticket** to be raised after this change lands, addressing whichever direction the runtime clarifies.

### D4. Snapshot churn is bounded to button DOM emissions only.

Unlike `align-domain-testids-with-runtime`, which touched dialogs (used by every page with a save/confirm flow), this change touches only standalone-button DOM. Estimated 30–100 snapshot files across the six itests — determined empirically by running `judo-diff-checker-maven-plugin:checkDiffs` on first CI run.

Refresh procedure per `AGENTS.md` §5. One snapshot commit per itest keeps the diff navigable.

### D5. `getButtonActionType` is a new Java helper, not an addition to `UiCommonsHelper`.

`UiCommonsHelper.getXMIID` lives in the sister Phase-1 repo (`judo-ui-typescript-rest-template`). Its contract is broader than test-id concerns and its clients are not restricted to this template. Adding a new normalization helper there risks unrelated downstream breakage.

Chosen: place `getButtonActionType` in this template's own `UiActionsHelper.java`, next to the existing action-related helpers. Scoped to test-id emission only.

Rejected: reuse an existing helper. Reason: no existing helper in `UiActionsHelper` returns the runtime-normalized action-type string. Adding a new one is cleaner than repurposing.

### D6. `.hbs` additions are comment-free — covered by project rule.

`AGENTS.md` §Code Instructions #9 (added 2026-07-21) prohibits new comments in `.hbs` templates under `judo-ui-react/src/main/resources/actor/` because they leak verbatim into every generated app. This change complies: `buildButtonTestId` in `transfer-id.ts.hbs` has no JSDoc; the emission sites in step (c) receive no explanatory comments. Documentation of the grammar lives in `openspec/specs/transfer-identity/spec.md`. Java-side helpers can carry reference comments because Java sources are compile-time-only.

### D7. `buildButtonTestId` accepts an optional `actionType`; a bare `button::<id>` is a valid escape hatch.

There are model shapes where a button has no `actionDefinition` reference — pure decorative buttons, buttons whose action is defined outside the standard action-definition hierarchy (custom extensions). For those cases, `getButtonActionType` returns `null` and the emitted testid is bare `button::<id>` without a suffix.

The runtime's `joinTestId` filters null / empty strings out, so its `getButtonTestId` on the same shape produces the same bare form. Confirmed byte-parity.

Rejected: throw at generation time when `actionDefinition` is missing. Reason: over-strict; models can legitimately have decorative buttons; consistent-with-runtime bare form is a safe fallback.

## Risks / Trade-offs

| Risk | Mitigation |
|---|---|
| The runtime's `getButtonActionType` normalization (`.replace("ui:", "").replace("ActionDefinition", "").toLowerCase()`) is a string-mangling contract that could drift if the runtime later refactors it. | Copy the implementation as a comment in the Java helper so drift becomes a review-time signal. Add a normative pointer to the runtime file+line in `spec.md`. |
| Every generated `ToolBarActionProps` literal gaining an `actionType` field is a type-signature change; third-party consumers that hand-author literals will fail to compile until they add the field. | Make the field required (not optional) so the compiler forces awareness. Document in the PR body as a downstream migration item. |
| A button model shape where `@type` doesn't match the "ui:XxxActionDefinition" pattern would produce a wrong-looking testid (e.g. `button::<id>::somecustomtype`). | Same output shape as the runtime — the discrepancy exists on both engines identically, so cross-engine parity is preserved even if the label is unexpected. Documented as expected behaviour. |
| Row-action buttons (D3) remain in the row-scoped form pending runtime clarification, so `page.getByTestId('button::<xmiId>::rowdelete')` on a row-delete button will fail against this template but succeed against the runtime. | Documented explicitly. Downstream tests that target row-action buttons SHOULD address them via `getRowActionTestId` OR wait for the follow-up ticket. |
| Snapshot refresh masks a real regression (a `data-testid` silently disappearing during the sweep). | Grep for `data-testid=` occurrence counts before and after per template; expect exact line-count parity (`buildFieldTestId(x)` → `buildButtonTestId(x, y)` is a 1:1 replacement, not an insertion or deletion). Line-count parity outside ±0 blocks the commit. |
| A model change adds a new action-definition subclass that this template's Java helper doesn't yet map. | The generic `.replace(...).toLowerCase()` derivation works for any subclass matching the naming convention — no per-subclass switch statement. New subclasses inherit correct normalization automatically. |

## Non-goals

1. No modification to `judo-ui-typescript-rest-template` or `UiCommonsHelper`. (D5)
2. No changes to widget-scoped or field-scoped buttons (`field::<id>::button::<role>`). (D1)
3. No resolution of row-action grammar (D3). Deferred.
4. No rename of `build*TestId` helpers to `get*TestId`. Cosmetic; still deferred per `align-testids-with-runtime-package` design.
5. No new Playwright tests in this repo. Cross-engine spot-check is the parent JNG-6391 task 8.1 responsibility.
6. No addition of test IDs to elements that don't already have one. This change reformats existing coverage.
7. No modification to the runtime.

## Cross-references

- **Discovery evidence**: `/tmp/pi-tests-react.log`, `/tmp/pi-tests-runtime.log` (from the 2026-07-21 baseline run on `judo-tatami-tests@feature/JNG-6411_Unify_data_testId_contract`).
- **Runtime authoritative source**: `judo-frontend-runtime@70fd3317` — `packages/test-ids/src/element.ts` (`getButtonTestId`, `getButtonActionType`), plus the six renderer files listed in `proposal.md` §Why.
- **Prior openspec changes**: `unify-data-id-and-testid-with-runtime`, `align-testids-with-runtime-package`, `align-domain-testids-with-runtime` — all code-complete on branch `feature/JNG-6391_unified_data_id_and_testid`.
