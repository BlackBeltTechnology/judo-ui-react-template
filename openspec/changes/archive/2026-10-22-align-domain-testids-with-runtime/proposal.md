## Why

The parent changes `unify-data-id-and-testid-with-runtime` and `align-testids-with-runtime-package` closed byte-parity gaps on widgets and tables. Every widget-level and table-level `data-testid` emitted by this template now byte-matches the JUDO frontend runtime.

Four domain surfaces still diverge — every emission across them is a Playwright-suite parity break because the runtime and template emit completely different testids for the same DOM node:

### H1 — Dialog domain

Runtime `packages/test-ids/src/dialog.ts` exports eight helpers producing `dialog::<id>[::title|content|actions|close|confirm|cancel]`. Template dialogs emit under `field::<id>`:

- `ConfirmationDialog.tsx.hbs:45-56` — outer `field::<id>::container`, `field::<id>::dialog-title`, `field::<id>::button::cancel|confirm`. Also carries a bare inline `data-testid="scroll-dialog-description"`.
- `FilterDialog.tsx.hbs:308,328,386-393` — outer `field::<id>::container`, `field::<id>::dialog-title`, `field::<id>::button::apply|cancel|close|clear-all`.
- `OperationFaultDialog.tsx.hbs:51-73` — bare inline literals `"fault-dialog-title"`, `"fault-dialog-content"`, `"close-fault-dialog"`, and per-fault `'faults.' + faultObjectKey + '.' + key`. None routes through a helper.

Runtime selectors: `getByTestId('dialog::<id>::title')`, `dialog::<id>::confirm`, etc. — return zero elements on template output.

### H2 — Navigation domain

Runtime `packages/test-ids/src/navigation.ts` exports `getNavItemTestId(item, parentPath?)` producing `nav::item::<path>` with parent nesting, plus `::icon`, `::label`, `::expand` role suffixes. Template drawer navigation emits under `field::<id>` with `menu::popper` / `menu::boundary` sub-roles:

- `NavItem.tsx.hbs:99,142` — `field::<item.id>` bare (twice, on two DOM levels — no distinguishing role).
- `NavCollapse.tsx.hbs:295,337` — `field::<menu.id>::menu::boundary` on `<ListItemButton>`, `field::<menu.id>::menu::popper` on `<PopperStyled>`.
- `NavGroup.tsx.hbs:154` — `field::<item.id>::menu::popper` on nested `<PopperStyled>`.

The template's dual-emission on NavItem (same testid on two DOM levels) is also a testid collision — `page.getByTestId('field::<id>')` returns two elements.

### H3 — Tabs domain

Runtime `packages/test-ids/src/tabs.ts` exports `getTabControllerTestId(controller)`, `getTabTestId(ctrlId, tab, index)`, `getTabPanelTestId(...)`, `getTabListTestId(ctrlId)` producing `tabs::<ctrlId>[::<tabId>[::panel] | ::list]`. Template emits under `field::<id>`:

- `ModeledTabs.tsx.hbs:27,93,104` — `field::<id>::panel` on `TabPanel`, bare `field::<id>` on outer `<Box>`, `field::<id>::tab::${c.id}` on individual `<Tab>` elements.

Template lacks a `list` role for the tab list container.

### H4 — Breadcrumb + inline testid literals

Runtime `packages/test-ids/src/navigation.ts::getBreadcrumbTestId(index)` produces `breadcrumb::<index>`. Template's `CustomBreadcrumb.tsx.hbs:195` emits a single anonymous `data-testid="application-breadcrumb"` on the outer `<Breadcrumbs>` element, with no per-item testids.

Also — a handful of bare inline literals across the layout:

- `layout/Drawer/DrawerContent/DrawerProfile/index.tsx.hbs:220` — `data-testid="profile"`
- `layout/BottomMenu/BottomProfile/index.tsx.hbs:135` — `data-testid="profile"` (duplicate literal!)
- `layout/BottomMenu/index.tsx.hbs:295` — `data-testid="submenu"`
- `components/dialog/OperationFaultDialog.tsx.hbs:51,57,73` — bare `"fault-dialog-title"`, `"fault-dialog-content"`, `"close-fault-dialog"`
- `components/ApplicationSelector.tsx.hbs:46,81,92` — `"GodView-dialog-close"`, `"application-changer-close"`, `"application-changer-submit"`

None of these has a direct runtime counterpart. They are template-only DOM. This change re-routes the ones with clear runtime targets (fault-dialog through the new dialog helpers) and leaves the truly template-only ones alone (profile, submenu, ApplicationSelector — the runtime has no matching DOM).

## What Changes

Four new build helpers in `transfer-id.ts.hbs`, one sweep across seven templates, spec update, and a bounded snapshot refresh.

### (a) New helpers in `transfer-id.ts.hbs`

Appended alongside the existing `buildFieldTestId` / `buildTableTestId` / `buildRowTestId` / `buildCellTestId`:

```ts
/** Build the outer `data-testid` for a dialog. Runtime parity: `dialog::<id>`. */
export function buildDialogTestId(dialogId: TestIdKey): string;

/** Build a role-suffixed `data-testid` inside a dialog.
 *  Role vocabulary matches runtime `packages/test-ids/src/dialog.ts`:
 *  `title` | `content` | `actions` | `close` | `confirm` | `cancel`.
 *  Callers MAY nest with `::` for compound roles. */
export function buildDialogRoleTestId(dialogId: TestIdKey, role: string): string;

/** Build the outer `data-testid` for a tab controller. Runtime parity: `tabs::<ctrlId>`. */
export function buildTabControllerTestId(ctrlId: TestIdKey): string;

/** Build a per-tab `data-testid`. Runtime parity: `tabs::<ctrlId>::<tabId>`. */
export function buildTabTestId(ctrlId: TestIdKey, tabId: TestIdKey): string;

/** Build a tab panel `data-testid`. Runtime parity: `tabs::<ctrlId>::<tabId>::panel`. */
export function buildTabPanelTestId(ctrlId: TestIdKey, tabId: TestIdKey): string;

/** Build the tab list `data-testid`. Runtime parity: `tabs::<ctrlId>::list`. */
export function buildTabListTestId(ctrlId: TestIdKey): string;

/** Build the outer `data-testid` for a nav item, optionally nested under a parent path.
 *  Runtime parity: `nav::item::<path>`. */
export function buildNavItemTestId(itemId: TestIdKey, parentPath?: string): string;

/** Build a role-suffixed `data-testid` under a nav item.
 *  Role vocabulary: `icon` | `label` | `expand`. */
export function buildNavItemRoleTestId(navItemTestId: string, role: string): string;

/** Build the `data-testid` for a breadcrumb item at a given index.
 *  Runtime parity: `breadcrumb::<index>`. */
export function buildBreadcrumbTestId(index: number): string;
```

All helpers are pure string functions; no closures over module state. Return type is always `string`.

### (b) Dialog sweep

- `ConfirmationDialog.tsx.hbs` —
  - Outer `<Dialog>` `field::<id>::container` → `buildDialogTestId(id)` (`dialog::<id>`).
  - `<DialogTitle>` `field::<id>::dialog-title` → `buildDialogRoleTestId(id, 'title')` (`dialog::<id>::title`).
  - Bare `"scroll-dialog-description"` on `<DialogContentText>` → `buildDialogRoleTestId(id, 'content')` (`dialog::<id>::content`).
  - `<Button>` cancel `field::<id>::button::cancel` → `buildDialogRoleTestId(id, 'cancel')` (`dialog::<id>::cancel`).
  - `<Button>` confirm `field::<id>::button::confirm` → `buildDialogRoleTestId(id, 'confirm')` (`dialog::<id>::confirm`).
- `FilterDialog.tsx.hbs` —
  - Outer `field::<id>::container` → `buildDialogTestId(id)`.
  - `<DialogTitle>` `field::<id>::dialog-title` → `buildDialogRoleTestId(id, 'title')`.
  - `field::<id>::button::apply` → `buildDialogRoleTestId(id, 'confirm')` (semantic mapping: "apply" is the confirm-equivalent for a filter dialog).
  - `field::<id>::button::cancel` → `buildDialogRoleTestId(id, 'cancel')`.
  - `field::<id>::button::close` → `buildDialogRoleTestId(id, 'close')`.
  - `field::<id>::button::clear-all` → **retained** — no runtime counterpart. Documented as template-only in the spec's role vocabulary.
  - `field::<id>::operator`, `field::<id>::value` on filter operator/value selectors → **retained** (widget-level roles inside the dialog, not dialog-domain).
- `OperationFaultDialog.tsx.hbs` — bare inline literals gain a synthetic dialog id (`'operation-fault'` — a stable constant, since this dialog has no model xmi:id):
  - `"fault-dialog-title"` → `buildDialogRoleTestId('operation-fault', 'title')`.
  - `"fault-dialog-content"` → `buildDialogRoleTestId('operation-fault', 'content')`.
  - `"close-fault-dialog"` → `buildDialogRoleTestId('operation-fault', 'close')`.
  - `'faults.' + faultObjectKey + '.' + key` per-fault list items → retained as template-only (no runtime counterpart).

### (c) Tabs sweep

- `ModeledTabs.tsx.hbs` —
  - Outer `<Box>` bare `field::<id>` → `buildTabControllerTestId(id)` (`tabs::<id>`).
  - `<TabPanel>` `field::<id>::panel` → `buildTabPanelTestId(id, c.id)` (`tabs::<id>::<tabId>::panel`) — note the runtime nests panel under the tab id, not the controller id alone. Template SHALL adopt the same nesting.
  - Individual `<Tab>` `field::<id>::tab::${c.id}` → `buildTabTestId(id, c.id)` (`tabs::<id>::<tabId>`).
  - `<TabList>` currently has no testid. Add `buildTabListTestId(id)` (`tabs::<id>::list`) on the `<Tabs>` container that wraps the tab strip.

### (d) Nav sweep

- `NavItem.tsx.hbs` —
  - `<ListItemButton>` line 99 `field::<item.id>` → `buildNavItemTestId(item.id, parentPath)`. Parent path is threaded through props (add `parentPath?: string` to `NavItemProps`).
  - Nested emission at line 142 `field::<item.id>` (same id, second DOM level — a collision) → remove this second emission or rename to a distinguishing role (`buildNavItemRoleTestId(navItemTestId, 'label')` or similar). Implementation-time decision after inspecting the surrounding DOM.
- `NavCollapse.tsx.hbs` —
  - Line 295 `<ListItemButton>` `field::<menu.id>::menu::boundary` → `buildNavItemTestId(menu.id, parentPath)`.
  - Line 337 `<PopperStyled>` `field::<menu.id>::menu::popper` → `buildNavItemRoleTestId(navItemTestId, 'expand')` (runtime's `nav::item::<path>::expand` is the semantic equivalent — the popper reveals the expanded children).
- `NavGroup.tsx.hbs` —
  - Line 154 `<PopperStyled>` `field::<item.id>::menu::popper` → same treatment as NavCollapse line 337.

Parent-path threading: nav items nest hierarchically (menu.item → sub-menu.item → sub-sub-item). The runtime's `getNavItemTestId(item, parentPath)` composes `nav::item::<parentPath>::<itemId>`. The template SHALL thread the parent path down through props: root menu invocation passes `parentPath=undefined`; each nested `<NavCollapse>` and `<NavGroup>` passes `parentPath={buildNavItemPath(parentPath, current.id)}` to its children. A small utility `buildNavItemPath(parentPath, currentId)` is added alongside the helpers for this composition.

### (e) Breadcrumb

- `CustomBreadcrumb.tsx.hbs` — the outer `<Breadcrumbs data-testid="application-breadcrumb">` gains per-item testids on each rendered `<Link>` / `<Typography>` inside the `.map(...)` — `buildBreadcrumbTestId(index)`. The outer `"application-breadcrumb"` literal is **retained** as a template-only outer-container hook (no runtime counterpart at the outer level; runtime only exposes per-item testids).

### (f) Options / chips deferred

Runtime `packages/test-ids/src/options.ts` exports `getOptionTestId(inputId, option, index)`, `getChipTestId`, `getChipDeleteTestId`, plus `getAutocompleteInput/DropdownTestId`, `getSelectTrigger/MenuTestId`. Template emits:

- `TrinaryLogicCombobox.tsx.hbs:65-67` — `field::<id>::option::true|false|undefined` (three static options). Format matches runtime shape (`<inputId>::option::<optId>`) but the "input-id" here is `field::<id>` rather than a bare id. **Deferred** — this needs the runtime's exact grammar clarified. Two interpretations:
  - Runtime `getOptionTestId('field::<id>', ...)` would emit `field::<id>::option::<opt>` — matches template.
  - Runtime `getOptionTestId('<id>', ...)` would emit `<id>::option::<opt>` — differs from template.
- Multi-select chips in `Tags.tsx.hbs` — currently no chip testids. Additive if we add them; runtime emits per-chip. Deferred.

### (g) User-menu, actor-selector deferred

`ApplicationSelector.tsx.hbs` bare literals (`GodView-dialog-close`, `application-changer-close`, `application-changer-submit`) — runtime `getUserMenuTestId`, `getActorSelectorTestId` exist but map to different DOM structure. Deferred pending live DOM comparison.

### (h) Snapshot refresh

Every affected `.tsx.snapshot` regenerated via `-DforceSnapshotOverwrite=true`. Expected surface: ConfirmationDialog is used by page/dialog templates, FilterDialog is used by every table with filters, ModeledTabs is used by every multi-tab view container. Nav is used by every actor's drawer. Estimated 15–40 snapshot files.

### What this change does NOT do

- Does **not** touch options / chips grammar (§f). Follow-up.
- Does **not** touch `ApplicationSelector` (§g). Follow-up.
- Does **not** touch layout containers in `flex.hbs` (Accordion / Card / Grid / Stack) — no runtime counterpart.
- Does **not** touch `DropdownButton.tsx.hbs` — no runtime counterpart.
- Does **not** rename `build*TestId` → `get*TestId`. Cosmetic; deferred.
- Does **not** remove template-only inline literals with no runtime target (`profile`, `submenu`, `application-breadcrumb` outer, ApplicationSelector). Keeps them as template-only hooks.
- Does **not** change `sourceId`-first identity resolution, the `resolveTransferId` chain, or the existing widget/table testid grammar.

## Capabilities

### Modified Capabilities

- **`transfer-identity`** — extends role vocabulary and helper surface. Adds dialog, tabs, nav-item, breadcrumb domain grammars. Adds normative pointers to `@judo/test-ids` `dialog.ts`, `navigation.ts`, `tabs.ts` as source of truth.
- **`layout-system`** — refers nav-item and breadcrumb testid construction to `transfer-identity`. Drawer nav components no longer emit under `field::`.
- **`page-system`** — dialogs (ConfirmationDialog, FilterDialog, OperationFaultDialog) refer dialog-domain testids to `transfer-identity`.
- **`data-tables`** — FilterDialog is table-scoped; its dialog testids now route through `buildDialog*TestId`. No change to table row / cell testids.

## Impact

- **`actor/src/utilities/transfer-id.ts.hbs`** — appended ~60 lines of new helpers.
- **`components/dialog/ConfirmationDialog.tsx.hbs`** — 5 testid re-routes.
- **`components/dialog/FilterDialog.tsx.hbs`** — 5 testid re-routes.
- **`components/dialog/OperationFaultDialog.tsx.hbs`** — 3 testid re-routes; per-fault list items retained as template-only.
- **`components/ModeledTabs.tsx.hbs`** — 3 testid re-routes + 1 new `list` emission.
- **`layout/Drawer/DrawerContent/Navigation/NavItem.tsx.hbs`** — 2 testid changes + `parentPath` prop plumbing.
- **`layout/Drawer/DrawerContent/Navigation/NavCollapse.tsx.hbs`** — 2 testid re-routes.
- **`layout/Drawer/DrawerContent/Navigation/NavGroup.tsx.hbs`** — 1 testid re-route.
- **`components/CustomBreadcrumb.tsx.hbs`** — 1 additive emission per rendered breadcrumb item.
- **`openspec/specs/transfer-identity/spec.md`** — role table expansion, four new domain sections, ~30 lines.
- **Snapshot refresh** — estimated 15–40 files across the six itests.
- **Downstream consumers** — no external consumer imports the current `field::<id>::dialog-title`, `field::<id>::tab::<...>`, `field::<menu.id>::menu::popper`, or `"fault-dialog-title"` literals; these were introduced only by the parent change (JNG-6391) and its predecessors.
