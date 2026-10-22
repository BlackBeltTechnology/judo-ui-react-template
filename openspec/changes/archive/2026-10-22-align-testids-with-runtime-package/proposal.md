## Why

The parent change `unify-data-id-and-testid-with-runtime` (JNG-6391) shipped the template's side of the unified `data-testid` contract before the runtime side existed. On 2026-07-14 Bence Dunai landed the runtime's counterpart on `judo-frontend-runtime@feature/unify-data-testid-contract` (single commit `70fd3317`, new package layout: shared `resolveTransferId` in `@judo/model-api/data/transfer-identity.ts`, re-exported through `@judo/test-ids`, plus new field/role helpers in `packages/test-ids/src/element.ts`).

Direct comparison of that runtime output against this template's current output surfaces three residual drifts that break byte-identical `data-testid` values on real DOM nodes rendered by both engines. All three are template-side defects — the runtime is now the ground truth per `dataid-template-runtime.pdf` §5 ("a runtime marad az alap, a react template igazodik hozzá").

### G1 — Relation set-selector button role mislabeled

The runtime maps `opensetselector` action definitions to `field::<id>::button::set` via `getFieldButtonRole()` in `packages/test-ids/src/element.ts:118-131`. Rendered by `LinkRenderer.tsx:733` on every `SingleRelation` and `Tags` widget.

This template emits `field::<id>::button::selector` for the identical DOM button:

- `judo-ui-react/src/main/resources/actor/src/components/widgets/SingleRelationInput.tsx.hbs:242` — `buildFieldTestId(id, 'button::selector')`
- `judo-ui-react/src/main/resources/actor/src/components/widgets/Tags.tsx.hbs:321` — `buildFieldTestId(id, 'button::selector')`

Playwright `getByTestId('field::<id>::button::set')` finds one element on runtime output, zero on template output. Blocker for the primary shared use case (open the relation picker).

### G2 — Outer wrapper carries a spurious `container` role

Every widget rendered by the runtime carries `data-testid={testId}` on its outermost `<Box>` — where `testId = getFieldTestId(element) = field::<id>` (bare, no role suffix). Verified against `packages/components/src/inputs/{TextInputComponent,DateInputComponent,EnumerationComboComponent}.tsx` and `packages/components/src/renderers/LinkRenderer.tsx:645` on `feature/unify-data-testid-contract`.

This template splits widget templates into two classes:

1. **Input widget-fragments** (`containers/widget-fragments/{textinput,dateinput,enumerationcombo,…}.hbs`) already emit bare `field::<id>` via `buildFieldTestId('{{ getXMIID child }}')` on their outermost element. Aligned with runtime. **No change.**
2. **Composite widget templates** (`components/widgets/{SingleRelationInput,Tags,TextWithTypeAhead,BinaryInput,AssociationButton}.tsx.hbs`, `components/ModeledTabs.tsx.hbs`) emit `buildFieldTestId(id, 'container')` on the outer wrapper. That produces `field::<id>::container` — no runtime counterpart. `page.getByTestId('field::<id>')` finds one element on runtime, zero on template; `page.getByTestId('field::<id>::container')` finds one on template, zero on runtime. Symmetric miss.

Six template sites:

- `judo-ui-react/src/main/resources/actor/src/components/widgets/SingleRelationInput.tsx.hbs:169`
- `judo-ui-react/src/main/resources/actor/src/components/widgets/Tags.tsx.hbs:196`
- `judo-ui-react/src/main/resources/actor/src/components/widgets/TextWithTypeAhead.tsx.hbs:94`
- `judo-ui-react/src/main/resources/actor/src/components/widgets/BinaryInput.tsx.hbs:50`
- `judo-ui-react/src/main/resources/actor/src/components/widgets/AssociationButton.tsx.hbs:63`
- `judo-ui-react/src/main/resources/actor/src/components/ModeledTabs.tsx.hbs:93`

### G2-deferred — open questions requiring live DOM inspection

The following drifts exist but cannot be resolved without side-by-side inspection of runtime and template rendered DOM. They are recorded here for the follow-up change:

- **`SingleRelationInput.tsx.hbs:278`** emits `buildFieldTestId(id, 'dropdown')` on a `<MenuList>`. Runtime `LinkRenderer.tsx` emits both `field::<id>::dropdown` and `field::<id>::menu` on distinct DOM levels. Without a rendered DOM comparison it is not certain which of the two the template's `<MenuList>` corresponds to.
- **`field::<id>::actions`** slot on the icon-button strip is emitted by runtime (`LinkRenderer.tsx:708`) but has no template counterpart. Additive; no byte-parity target broken today, so deferred rather than guessed.
- **`NavCollapse.tsx.hbs:295,337`** and **`NavGroup.tsx.hbs:154`** emit `menu::popper` / `menu::boundary` on the drawer sidebar's mini-drawer collapse popper. These are navigation-domain DOM (runtime `packages/test-ids/src/navigation.ts`), covered by the deferred navigation-slot-naming change.
- **`ConfirmationDialog.tsx.hbs`, `FilterDialog.tsx.hbs`** emit `container` on dialog outer elements. Dialogs are runtime-domain-prefixed (`dialog::<id>`) per `packages/test-ids/src/dialog.ts` — handled by the deferred dialog-slot-naming change.
- **`DropdownButton.tsx.hbs:93`** emits `container` on a button-group-like widget with no clear runtime counterpart.
- **`containers/widget-fragments/flex.hbs`** emits `container` on four layout wrappers (Accordion, Card, Grid, Stack). The runtime does not render layout via template code paths — no direct byte-parity target.

### G3 — Element-id source: `sourceId` never consulted

Runtime `getElementTestId(element, prefix)` in `packages/test-ids/src/element.ts:22` resolves the element's identity as `element.sourceId || element['xmi:id'] || element.name || 'unknown'`. `sourceId` wins when present.

This template's Java helper `UiCommonsHelper.getXMIID(EObject)` at `judo-ui-typescript-rest-template/judo-ui-typescript-rest-commons/src/main/java/hu/blackbelt/judo/ui/generator/typescript/rest/commons/UiCommonsHelper.java` returns only `((XMIResource) element.eResource()).getID(element)`. `sourceId` is never consulted at generation time. Verified by disassembling the currently-installed artifact (`~/.m2/.../judo-ui-typescript-rest-commons-*.jar` — `getXMIID` bytecode contains only `XMIResource.getID` invocation).

Consequence: on any model element that carries a `sourceId` distinct from its `xmi:id`, the runtime and template emit different testids for the same DOM node. Currently a **silent** parity break — the two engines still work in isolation, but a shared Playwright selector will silently fail on all such elements. Impact depends on how many PSM→UI-model elements carry `sourceId`; not empirically bounded today.

### Out of scope

Two categories of drift are deliberately excluded from this change:

- **Dialog / navigation / tabs slot naming.** The runtime ships domain-prefixed helpers (`getDialogTitleTestId → dialog::<id>::title`, `getNavItemTestId → nav::item::<path>`, `getTabTestId → tabs::<ctrlId>::<tabId>`) in `packages/test-ids/src/{dialog,navigation,tabs}.ts`. This template funnels the same DOM levels through `buildFieldTestId(id, 'dialog-title')` etc. — hyphenated single-segment roles. That is a genuine byte-parity break on dialog headers, nav items, and tab controllers, but its resolution is not settled by `dataid-template-runtime.pdf` (the PDF is silent on those slots) and needs a design decision between "adopt runtime's domain prefixes" and "ask runtime to funnel through `field::` for uniformity". Deferred to a follow-up change.
- **Helper rename `build*TestId` → `get*TestId`.** Cosmetic. Zero effect on emitted strings. Deferred as an optional janitorial change if we want npm-package-level naming parity with `@judo/test-ids`.

## What Changes

Two mechanical template edits (G1, G2), one Java helper addition + template sweep (G3), and a snapshot refresh across the six itests.

### (a) `button::selector` → `button::set`

Rename the role literal at both emission sites:

- `SingleRelationInput.tsx.hbs:242` — `buildFieldTestId(id, 'button::selector')` → `buildFieldTestId(id, 'button::set')`.
- `Tags.tsx.hbs:321` — same replacement.

No other role literal changes. The role vocabulary in `openspec/specs/transfer-identity/spec.md` is updated to drop `selector` and clarify that `set` is the label for the primary open-selector-dialog button (owning `opensetselector` action definitions in the model).

### (b) Drop the `container` role on composite widget outer wrappers

Six composite widget templates emit `buildFieldTestId(id, 'container')` on their outermost element. Change each to bare `buildFieldTestId(id)` — no role argument — producing `field::<id>` to match the runtime's outer-wrapper convention:

- `SingleRelationInput.tsx.hbs:169` — `<Grid container>` wrapper.
- `Tags.tsx.hbs:196` — `<Box>` wrapper.
- `TextWithTypeAhead.tsx.hbs:94` — `<Box>` wrapper.
- `BinaryInput.tsx.hbs:50` — `<Grid container>` wrapper.
- `AssociationButton.tsx.hbs:63` — outer element.
- `ModeledTabs.tsx.hbs:93` — `<Box>` wrapper.

The role vocabulary is updated to remove `container` as an accepted role and add a note that widget outer wrappers SHALL carry the bare `field::<id>` testid.

The `container` role is retained (but marked "template-only, no runtime counterpart") on the four out-of-scope sites (dialogs, DropdownButton, flex.hbs layout containers) until their respective deferred changes land.

### (c) `sourceId` support in element-id resolution

The XMI-id resolution used by `getXMIID(EObject)` becomes source-id-aware. Rather than modifying `UiCommonsHelper.getXMIID` in the sister Phase-1 repo (its contract is more general than test-ids), a new sibling helper `getElementId(EObject)` is added to this repo's `UiGeneralHelper`:

```java
// UiGeneralHelper (judo-ui-react/src/main/java/…/UiGeneralHelper.java)
public static String getElementId(EObject element) {
    String sourceId = readSourceIdViaEStructuralFeature(element);
    if (sourceId != null && !sourceId.isEmpty()) return sourceId;
    return getXMIID(element).replaceAll("@", "");
}
```

The `readSourceIdViaEStructuralFeature` step reads the `sourceId` `EAttribute` from `ui.data` metamodel elements that declare it (subset of `ClassType` / `TransferAttribute` / `Widget` — precise list determined during implementation by reading the `.ecore` in `judo-meta-ui`). Elements without a `sourceId` feature fall through to the existing `getXMIID` path unchanged.

Every current template usage of `getXMIID` **for the purpose of emitting a `data-testid`** switches to `getElementId`. The bulk of these are the `buildFieldTestId('{{ getXMIID X }}')` sites — replaced with `buildFieldTestId('{{ getElementId X }}')`.

`getXMIID` is retained for its non-testid uses (Pandino interface keys, i18n keys, container-name suffix detection at `UiPageContainerHelper.java:193`), because those keys are internal to the generated app and are not required to match the runtime.

### (d) Role-vocabulary spec update

`openspec/specs/transfer-identity/spec.md` is amended:

- The `buildFieldTestId` role table is updated: `selector` is removed, `menu::popper` / `menu::boundary` are removed, `set` is clarified to mean "primary open-selector-dialog button (owner of an `opensetselector` action definition in the model)", `menu` and `actions` are added as new roles for the relation-widget dropdown chrome.
- A normative pointer to `@judo/test-ids@0.33+` (or whatever version tags `feature/unify-data-testid-contract`) is added under the "Cross-engine parity" scenario: "template and runtime SHALL agree on the role literal for the same DOM node; the runtime's `getFieldButtonRole()` map is the authoritative source for button-role names."

### (e) Snapshot refresh

Every `.tsx.snapshot` under `judo-ui-react-itest/**/src/test/resources/snapshots/frontend-react/**` whose regenerated form carries `button::set` (from G1), `field::<id>::actions` or `field::<id>::menu` (from G2), or a `sourceId`-resolved element id different from its `xmi:id` (from G3) SHALL be refreshed. Estimated ≤50 files based on the surface — G1/G2 touch only the three relation widgets; G3 touches every widget's outer wrapper *only if the underlying model carries `sourceId`*.

### What this change does NOT do

- Does **not** touch dialog / navigation / tabs / breadcrumb / user-menu / actor-selector slot naming. Those are deferred to a separate change once the design decision (domain prefixes vs `field::` uniformity) lands.
- Does **not** rename `build*TestId` to `get*TestId`. Cosmetic; deferred.
- Does **not** add `joinTestId`, `slugify`, or `sanitizeId` utilities to `transfer-id.ts.hbs`. The template's string-interpolation style produces identical output; no functional need.
- Does **not** modify `UiCommonsHelper.getXMIID` in the sister repo. New helper `getElementId` lives in this repo's `UiGeneralHelper`.
- Does **not** change the `resolveTransferId` fallback chain, `newTempId`, `isNewRow`, or any other helper already shipped by the parent change.
- Does **not** rename any exported symbol from `transfer-id.ts`.
- Does **not** modify the `data-testid` scheme for tables (`table::<id>::row::<resolved>::cell::<col>`). Runtime and template already agree byte-for-byte.

## Capabilities

### Modified Capabilities

- **`transfer-identity`** — role vocabulary amended (see §(d)). Adds normative reference to the runtime's `getFieldButtonRole()` as authoritative source for button-role labels. Adds `sourceId`-first element-id resolution as a template-side responsibility.
- **`relation-management`** — `SingleRelationInput`, `Tags`, `TextWithTypeAhead` widgets emit `field::<id>::actions` on the icon-button strip and `field::<id>::menu` on the dropdown popper. `button::selector` on the primary open-selector button becomes `button::set`.

### Unaffected Capabilities

- `data-tables`, `input-widgets` (except the three relation widgets), `page-system`, `layout-system`, all dialog and navigation capabilities — no changes; existing testids continue to match runtime where they already did.

## Impact

- **`judo-ui-react/src/main/java/hu/blackbelt/judo/ui/generator/react/UiGeneralHelper.java`** — one new public static method `getElementId(EObject)`, ~15 lines. Uses reflection on `EStructuralFeature` to read `sourceId` where present.
- **G1 sites (2 files, 2 lines):** `SingleRelationInput.tsx.hbs:242`, `Tags.tsx.hbs:321` — `button::selector` → `button::set`.
- **G2 sites (6 files, ~6 lines):** `SingleRelationInput.tsx.hbs:169`, `Tags.tsx.hbs:196`, `TextWithTypeAhead.tsx.hbs:94`, `BinaryInput.tsx.hbs:50`, `AssociationButton.tsx.hbs:63`, `ModeledTabs.tsx.hbs:93` — drop `'container'` role.
- **`getXMIID` → `getElementId` sweep** in every `buildFieldTestId('{{ getXMIID X }}')` site — ~15 template files, ~30 lines by grep count.
- **`openspec/specs/transfer-identity/spec.md`** — role table amended, one new scenario. ~15 lines net.
- **Snapshot refresh** — estimated ≤50 `.tsx.snapshot` files across the six itests. Mechanical procedure per `AGENTS.md` §5.
- **Downstream consumers** — no external consumer depends on the `button::selector`, `menu::popper`, or `menu::boundary` literals; these were introduced only by the parent change (JNG-6391) and were never emitted before that. The `getElementId` helper is additive.
