## Decisions

### D1. Runtime is now the ground truth; template conforms.

The parent change was drafted before the runtime side existed and quoted `dataid-template-runtime.pdf` §3 as the specification. That PDF is silent on individual role literals (it only illustrates `button::set` and `input`); the runtime code that landed on 2026-07-14 fills in the vocabulary (`getFieldButtonRole()`, `LinkRenderer.tsx` DOM slot names). Where PDF and runtime code disagree, this change follows the runtime code, because §5 of the PDF is explicit: "a runtime marad az alap, a react template igazodik hozzá."

Rejected: keep the template's chosen labels (`selector`, `menu::popper`, `menu::boundary`) and file a runtime-side change to adopt them. Reason: (a) the runtime is already released as `@judo/test-ids@0.33` per the branch's commit history and will be consumed by downstream apps; (b) `set` / `menu` / `actions` are shorter and more grep-friendly than the template's alternatives; (c) `menu::popper` conflated the MUI implementation detail (a Popper) with the semantic role (a menu surface).

### D2. `sourceId` support goes in `UiGeneralHelper`, not `UiCommonsHelper`.

`UiCommonsHelper.getXMIID` lives in the sister Phase-1 repo (`judo-ui-typescript-rest-template/judo-ui-typescript-rest-commons`). Its return value is used by dozens of Phase-1 templates as a stable EMF-object handle — Pandino keys, i18n keys, Axios path parameters. Changing its return value on any object with a `sourceId` would ripple through every downstream artifact and require re-releasing Phase 1.

`getElementId` in this repo's `UiGeneralHelper` is scoped to *test-id emission only*. It reads `sourceId` when the EObject exposes that structural feature, otherwise falls through to the existing `getXMIID` path. No effect on Phase-1 output. No cross-repo coupling.

Rejected: overload `getXMIID` here to check `sourceId` first, keep the same name. Reason: two helpers named `getXMIID` returning different values for the same input across the two repos is precisely the kind of silent drift this change series is trying to eliminate.

Rejected: fix by convention — audit the models, confirm `sourceId` is unused today. Reason: the runtime's `getElementTestId` will consult `sourceId` regardless of what our models currently emit, so any future model change that introduces a `sourceId` would immediately break parity in production without a diff signal here. Making the resolution explicit in Java is a permanent fix; auditing today's data is a snapshot.

### D3. Drop the `container` role rather than renaming it.

The runtime never emits `field::<id>::container`. Its outer widget wrappers carry bare `data-testid={testId}` where `testId = getFieldTestId(element) = field::<id>` — verified across `TextInputComponent.tsx:101`, `DateInputComponent.tsx:82`, `EnumerationComboComponent.tsx:61`, and `LinkRenderer.tsx:645`. There is no runtime role to align to; the template's `container` suffix is a template-only extension with no test-parity target.

Our input widget-fragments (`containers/widget-fragments/{textinput,dateinput,enumerationcombo,…}.hbs`) already emit bare `field::<id>` — they got this right by accident during the parent JNG-6391 sweep. The six composite widget templates that added `'container'` are the outliers.

Rejected: keep `container` and file a runtime-side ticket to add it. Reason: `container` is redundant with the widget's outer wrapper being the default `data-testid` target already. No test needs to distinguish "the outer wrapper" from "the widget itself" — they are the same DOM node.

Rejected: rename `container` to `wrapper` or `field-container` for clarity. Reason: same argument as above — no role name that isn't in the runtime's vocabulary produces byte-parity.

### D4. `container` retained on dialogs, DropdownButton, and layout fragments.

Four out-of-scope surfaces still emit `container`: `ConfirmationDialog.tsx.hbs`, `FilterDialog.tsx.hbs`, `DropdownButton.tsx.hbs`, and `containers/widget-fragments/flex.hbs` (Accordion / Card / Grid / Stack). These are handled by separate deferred changes (dialog domain naming, DropdownButton contract, layout-slot contract). Dropping `container` on them now would remove an emission with no better replacement to migrate to, breaking any test that currently binds against `field::<id>::container` for those specific DOM levels.

Rejected: sweep all `container` emissions in one pass. Reason: the widget-outer-wrapper case has a clear runtime target (bare `field::<id>`); the dialog / button-group / layout cases do not. Doing them together conflates two design decisions.

### D5. `dropdown` vs `menu` on `SingleRelationInput` MenuList is left open.

The MenuList at `SingleRelationInput.tsx.hbs:278` carries `buildFieldTestId(id, 'dropdown')`. Runtime `LinkRenderer.tsx` emits both `field::<id>::dropdown` and `field::<id>::menu` on distinct DOM levels — without a rendered-DOM comparison it is not certain which the template's `<MenuList>` corresponds to. Guessing here risks making the drift worse rather than better.

Deferred: added to the open-questions list in the deferred G2 bucket. To be resolved by rendering the same model on both engines, using devtools to identify the actual DOM levels, and mapping the template's `<MenuList>` to whichever runtime slot occupies the equivalent position.

Rejected: rename `dropdown` to `menu` speculatively. Reason: if the template's `<MenuList>` corresponds to the runtime's `field::<id>::dropdown` (autocomplete-suggestions popper) rather than `field::<id>::menu` (relation-picker popper), the rename creates new divergence rather than closing existing divergence.

### D5. Snapshot churn is bounded, unlike the parent change.

Unlike JNG-6391, this change touches at most three widget templates and one Java helper. G1/G2 snapshots are limited to itests that instantiate `SingleRelationInput`, `Tags`, or `TextWithTypeAhead` — verified by grepping the snapshot tree for those component names. G3 snapshots depend on how many model elements carry `sourceId` — if zero, G3 causes no churn.

Refresh procedure remains the `AGENTS.md` §5 copy-from-target. One snapshot commit per itest keeps review diffs navigable; total commit count for this change is expected in the 8–10 range.

### D6. `getXMIID` retained for non-testid uses.

Pandino interface keys, i18n keys, container-name detection, action-definition-map keys — none of these are observable through `data-testid`. They must remain stable across template rebuilds because downstream extensions bind against them. Changing them to `sourceId`-first would break every downstream extension silently. The scope of `getElementId` is *strictly* `data-testid` emission and any other UI-observable identity string that must match runtime.

Rejected: switch every `getXMIID` call to `getElementId`. Reason: the two helpers exist precisely because they serve two contracts. Conflating them re-creates the mistake this change avoids by not touching `UiCommonsHelper`.

## Risks / Trade-offs

| Risk | Mitigation |
|---|---|
| Renaming `button::selector` to `button::set` breaks any Playwright suite already authored against template output on the JNG-6391 branch. | The JNG-6391 branch has not yet been merged to `develop` (verify at implementation time). If any suite exists, it targets template output that is now understood to be non-canonical; the fix is a one-line selector rename. |
| The `getElementId` helper relies on reflectively reading a `sourceId` `EStructuralFeature` from the EMF metamodel. If the feature is not present on the concrete `EObject`, reflection returns null and we fall through — safe. If the feature *is* present but its runtime value is unexpected (e.g. an empty string), we treat as null. | Explicit null-and-empty check in the helper. Unit test covers both branches. |
| Snapshot refresh masks a real regression — e.g. an emitted `data-testid` is silently dropped during the sweep. | Grep the union-set of `data-testid` occurrences in `target/frontend-react/**` before and after; the total count changes only by the deliberate additions (`::actions` slots) and deliberate collapses (`menu::popper` + `menu::boundary` → `menu`, net −1 per widget). Any other delta signals a regression. |
| The runtime's `getFieldButtonRole` map evolves — a future runtime release adds new mappings we don't know about. | The role vocabulary in the spec cites the runtime's `getFieldButtonRole` as authoritative. Divergence becomes a review-time signal at the next sync. Not a preventable risk. |
| The runtime uses `element.name` as a third-tier fallback in `getElementTestId`; our `getElementId` does not. | Documented in the spec as an intentional deviation: `name` is a human-readable label subject to i18n. Falling back to `name` at generate time would embed English labels into testids, which would differ from PSM-only models rendered in the runtime and vice versa. Both engines converge on `sourceId || xmi:id` for the elements we care about; `name` fallback is a runtime last-resort that never fires in practice for the shared testid contract. |
| G3 introduces a new dependency between test-id generation and the `ui.data` metamodel version. If a downstream project pins an older metamodel that lacks `sourceId`, `getElementId` falls through silently. | This is the desired behaviour — silent fall-through matches runtime behaviour (which also treats absent `sourceId` as "use `xmi:id`"). No new pin required. |

## Non-goals

1. No modification to `judo-ui-typescript-rest-template` or `UiCommonsHelper`. (D2, D6)
2. No adoption of runtime's `getDialog*TestId`, `getNavItem*TestId`, `getTab*TestId`, `getBreadcrumbTestId`, `getUserMenuTestId`, `getActorSelectorTestId`, `getOption*TestId`, `getChip*TestId` helpers. Those slots' naming is deferred to a follow-up change once the domain-prefix-vs-`field::` design decision lands.
3. No rename of `build*TestId` to `get*TestId`. Cosmetic only. Deferred.
4. No `joinTestId` / `slugify` / `sanitizeId` utilities added. String concatenation via template literals produces identical output.
5. No change to the `resolveTransferId` chain, `newTempId`, `isNewRow`, or the `__tempId`/`__isNew` seeding scheme. All already correct.
6. No change to table row / cell / action / column-header / filter / toolbar / pagination testid grammar. Runtime and template already agree.
7. No new Playwright tests. Cross-engine spot-check remains the JNG-6391 task 8.1 responsibility.
