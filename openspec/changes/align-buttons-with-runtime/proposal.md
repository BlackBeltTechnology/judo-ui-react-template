## Why

The prior three JNG-6391 changes unified the identity chain, the widget/field grammar, and the dialog/nav/tabs domain grammar. They did **not** touch the grammar for **standalone buttons**: buttons that live outside a field-wrapper — page-level actions, container action buttons, button groups, FAB button groups, and table toolbar buttons.

Runtime `judo-frontend-runtime@feature/unify-data-testid-contract` emits `button::<xmiId>::<actionType>` on every such DOM node. Verified across six renderers (all use `getButtonTestId(button)` from `@judo/test-ids/src/element.ts`):

- `packages/components/src/renderers/StandaloneButtonRenderer.tsx:59` — page-level standalone buttons.
- `packages/components/src/renderers/ButtonGroupRenderer.tsx:79` — button groups.
- `packages/components/src/renderers/InlineButtonGroupRenderer.tsx:171,203` — inline button groups.
- `packages/components/src/renderers/FabButtonGroupRenderer.tsx:134,236` — FAB button groups.
- `packages/components/src/renderers/RowActionCell.tsx:203,221` — per-row action buttons (featured + overflow menu items).
- No table-toolbar renderer directly visible in runtime source yet; the runtime's toolbar composition inherits the same helper via the `ButtonGroup`/`InlineButtonGroup` render path.

The `actionType` suffix is derived from `button.actionDefinition['@type']` via `packages/test-ids/src/element.ts::getButtonActionType`: `"ui:OpenCreateFormActionDefinition"` → `"opencreateform"`. The runtime maps every action-definition subclass to this lowercase, stripped suffix. Example emitted testids: `button::<xmiId>::opencreateform`, `button::<xmiId>::opensetselector`, `button::<xmiId>::rowdelete`, `button::<xmiId>::rowopenpage`.

This template emits `field::<xmiId>` (via `buildFieldTestId(id)`) on the same DOM elements. Verified emission sites:

- `judo-ui-react/src/main/resources/actor/src/containers/page.tsx.hbs:99` — page-level button.
- `judo-ui-react/src/main/resources/actor/src/containers/widget-fragments/button.hbs:23` — standalone button inside a container.
- `judo-ui-react/src/main/resources/actor/src/containers/widget-fragments/buttongroup.hbs:28` — button-group child button.
- `judo-ui-react/src/main/resources/actor/src/containers/widget-fragments/fab-button-group.fragment.hbs:24,45` — FAB button group.
- `judo-ui-react/src/main/resources/actor/src/containers/widget-fragments/flex.hbs:43,111` — buttons inside Accordion / Card flex wrappers.
- `judo-ui-react/src/main/resources/actor/src/components/table/EagerTable.tsx.hbs:582` — table-toolbar action button.
- `judo-ui-react/src/main/resources/actor/src/components/table/LazyTable.tsx.hbs:744` — table-toolbar action button.
- `judo-ui-react/src/main/resources/actor/src/components/table/table-row-actions.tsx.hbs:80` — per-row action button (currently `${buildRowTestId(id, row)}::button::${actionId}`, i.e. `table::<tableId>::row::<transferId>::button::<actionId>` — see D3 below for the row-action decision).

Observable evidence: on `judo-tatami-tests@feature/JNG-6411_Unify_data_testId_contract`, `InlineEditTest.spec.ts:35` calls `testIds.button(toolbarActions.create)` which returns `button::<xmiId>::opencreateform`. The React template emits `field::<xmiId>` on the same `<Button>`; `page.getByTestId('button::<xmiId>::opencreateform')` returns zero elements. The runtime emits `button::<xmiId>::opencreateform` and the selector matches. Same test, byte-different DOM.

The gap is not covered by any of the three landed changes:

- `unify-data-id-and-testid-with-runtime` — established `field::<id>` grammar for widgets; did not distinguish "widget wrapper" vs "standalone button".
- `align-testids-with-runtime-package` — corrected the *inside-field* button role (`field::<id>::button::selector` → `field::<id>::button::set`); did not touch standalone buttons.
- `align-domain-testids-with-runtime` — added `dialog::`, `nav::`, `tabs::`, `breadcrumb::` domain prefixes; did not add `button::`.

## What Changes

Single Class III change. Adds one new TypeScript helper, sweeps ~8 template emission sites, augments the toolbar-action data shape with the pre-normalized `actionType`, and refreshes snapshots.

### (a) New helper `buildButtonTestId` in `transfer-id.ts.hbs`

Appended alongside the existing `buildFieldTestId` / `buildTableTestId` / `buildRowTestId` / `buildCellTestId` / dialog / tabs / nav helpers. Signature:

```ts
export function buildButtonTestId(buttonId: TestIdKey, actionType?: string): string;
```

Return grammar: `button::<id>` when `actionType` is omitted / empty, otherwise `button::<id>::<actionType>`. Pure string function, no closures. Documentation lives in `openspec/specs/transfer-identity/spec.md`, not in the `.hbs` (project rule `AGENTS.md` §Code Instructions #9).

### (b) New `actionType` field on `ToolBarActionProps<T>`

`judo-ui-react/src/main/resources/actor/src/utilities/table.ts.hbs` — the `ToolBarActionProps<T>` interface gains an `actionType: string` field, pre-normalized at template render time to match the runtime's `getButtonActionType` output (`opencreateform`, `opensetselector`, `opencreateform`, `refresh`, `export`, `filter`, `inlinecreaterowaction` → normalized per the runtime's `.toLowerCase()` after stripping `ui:` prefix and `ActionDefinition` suffix).

The value SHALL be set by every template site that constructs a `ToolBarActionProps` object — `judo-ui-react/src/main/resources/actor/src/containers/components/table/index.tsx.hbs` (all toolbar-action object literals) — using a new Java helper `UiActionsHelper.getButtonActionType(actionDefinition)` that returns the same string the runtime produces.

### (c) Emission-site sweep

The emission sites switch from `buildFieldTestId(...)` to `buildButtonTestId(...)`. In addition to the seven sites originally enumerated in §Why, two more standalone-button emission sites were discovered during implementation and included in the sweep (dialog action-bar buttons and the `AssociationButton` widget used for `OpenPageActionDefinition` navigation):

- `containers/page.tsx.hbs:99` → `buildButtonTestId('{{ getElementId button }}', '{{ getButtonActionType button }}')`.
- `containers/dialog.tsx.hbs:208` → same. **Added during implementation** — structurally identical to `page.tsx.hbs:99` (dialog action-bar `<Button>` driven by `containerButtonGroupButtonDisabledConditions`).
- `containers/widget-fragments/button.hbs:23` → same.
- `containers/widget-fragments/buttongroup.hbs:28` → same.
- `containers/widget-fragments/fab-button-group.fragment.hbs:24,45` → same.
- `containers/widget-fragments/flex.hbs:43,111` → same.
- `components/table/EagerTable.tsx.hbs:582` → `buildButtonTestId(toolBarAction.id, toolBarAction.actionType)`.
- `components/table/LazyTable.tsx.hbs:744` → same.
- `components/widgets/AssociationButton.tsx.hbs:63` → `buildButtonTestId(id, actionType)`. **Added during implementation** — shared TSX component invoked from `containers/widget-fragments/button.hbs:48` for the `isOpenPageAction` branch. `AssociationBaseProps` gains an optional `actionType?: string` prop; the caller passes `actionType="{{ getButtonActionType child }}"`.
- `components/table/table-row-actions.tsx.hbs:80` — see D3 (deferred).

### (d) Java helper `UiActionsHelper.getButtonActionType(EObject)`

New public static method returning `actionDefinition["@type"]` after `.replace("ui:", "").replace("ActionDefinition", "").toLowerCase()` — the byte-exact port of runtime `getButtonActionType`. Unit-tested for the eight action-definition subclasses in current models (`OpenCreateFormActionDefinition`, `OpenSetSelectorActionDefinition`, `OpenAddSelectorActionDefinition`, `OpenPageActionDefinition`, `RowOpenPageActionDefinition`, `RowDeleteActionDefinition`, `CallOperationActionDefinition`, `RefreshActionDefinition`).

Returns `null` when the argument has no `actionDefinition` — the helper caller then omits the suffix.

### (e) Spec update

`openspec/specs/transfer-identity/spec.md` — new subsection under "Hierarchical `data-testid` scheme": the **button domain**. Table extends the role vocabulary to state that standalone buttons are addressed by `button::<id>::<actionType>`, distinct from field-scoped buttons (`field::<id>::button::<role>`). Adds a normative pointer to runtime `getButtonTestId`.

### (f) Snapshot refresh

Every `.tsx.snapshot` under `judo-ui-react-itest/**/src/test/resources/snapshots/frontend-react/**` that renders a re-formatted standalone button testid SHALL be refreshed. Estimated 30–100 files (bounded by number of button emissions per actor).

### What this change does NOT do

- Does **not** touch field-scoped buttons (`field::<id>::button::<role>`). Those are correct and covered by the prior three changes.
- Does **not** change how `actionDefinition` is authored in models. The change is purely observational: read the existing `@type`, normalize, emit.
- Does **not** modify the runtime.
- Does **not** rename `buildFieldTestId` or any other shipped helper.
- Does **not** resolve D3 below (row-actions grammar); that is called out as an open decision.

## Capabilities

### Modified Capabilities

- **`transfer-identity`** — extends the grammar with the `button::` domain. Adds the `buildButtonTestId` helper and normative reference to runtime `getButtonTestId`. Extends the role vocabulary table.

### Unaffected Capabilities

- `data-tables`, `input-widgets`, `relation-management`, `page-system`, `layout-system` — no changes; existing testids continue to match runtime where they already did.

## Impact

- **`actor/src/utilities/transfer-id.ts.hbs`** — one new function `buildButtonTestId`, ~10 lines.
- **`actor/src/utilities/table.ts.hbs`** — one new field on `ToolBarActionProps<T>`, ~1 line.
- **`actor/src/containers/components/table/index.tsx.hbs`** — every `ToolBarActionProps` literal gains `actionType: '<lowercased>'`. ~15 literals × 1 line = ~15 lines.
- **`judo-ui-react/src/main/java/hu/blackbelt/judo/ui/generator/react/UiActionsHelper.java`** — one new public static method `getButtonActionType(EObject)`. ~10 lines. Unit-tested for the eight known action-definition subclasses.
- **Emission-site sweep** — 10 template files (8 originally enumerated + `containers/dialog.tsx.hbs` + `components/widgets/AssociationButton.tsx.hbs`) plus one shared-imports fragment (`fragments/container/common-imports.fragment.hbs`). ~14 lines net.
- **`openspec/specs/transfer-identity/spec.md`** — role-vocabulary table extension + one new Scenario. ~15 lines.
- **Snapshot refresh** — estimated 30–100 files across the six itests. Mechanical procedure per `AGENTS.md` §5.
- **Downstream consumers** — the `ToolBarActionProps<T>` interface gains a required `actionType` field; any external consumer that constructs a `ToolBarActionProps` object literal SHALL add the field. Every generator-emitted literal is covered by (c); the change surfaces cleanly at the type-check level for third-party code.

## Open Questions

**D3 — row-action buttons: `button::` vs `table::row::action::`.**

Runtime `RowActionCell.tsx` at lines 203, 221 uses `getButtonTestId(button)` → `button::<xmiId>::<actionType>` for both the featured row-action buttons and the overflow-menu items. That says row-action DOM emits under the `button::` domain, ignoring the row context.

But `@judo/test-ids` also exports `getRowActionTestId(tableId, transfer, actionName, index?)` producing `table::<tableId>::row::<transferId>::action::<actionName>` — a row-scoped grammar. The template currently emits the row-scoped form at `components/table/table-row-actions.tsx.hbs:80`.

The runtime is self-inconsistent: it exports a row-scoped helper but its own renderer doesn't use it. Two possible resolutions:

- **Follow runtime's actual emission** — switch this template's row-action buttons to `button::<xmiId>::<actionType>` matching `RowActionCell.tsx`. The row-scoped `getRowActionTestId` becomes dead code in the test-ids package.
- **Follow the exported helper** — this template stays with `table::<tableId>::row::<transferId>::action::<actionName>`, and the runtime is expected to fix its `RowActionCell.tsx` to use `getRowActionTestId`.

Deferred pending runtime-side decision. This change addresses the seven unambiguous sites (§(c) items 1–7); table-row-actions is called out separately in `design.md` D3 and `tasks.md` §3.
