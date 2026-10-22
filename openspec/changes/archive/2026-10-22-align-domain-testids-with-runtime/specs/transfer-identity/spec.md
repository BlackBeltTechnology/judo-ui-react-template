## ADDED Requirements

### Requirement: Dialog testid grammar

Every dialog outer element and every semantic sub-part of a dialog SHALL carry a `data-testid` constructed through `buildDialogTestId(dialogId)` or `buildDialogRoleTestId(dialogId, role)` exported from `src/utilities/transfer-id.ts`. The grammar SHALL be:

- Outer `<Dialog>` element: `dialog::<dialogId>`
- Semantic sub-parts: `dialog::<dialogId>::<role>` where `role` is one of the following values or a `::`-nested compound:

| Role | Applies to |
|---|---|
| `title` | The `<DialogTitle>` element |
| `content` | The `<DialogContent>` or the `<DialogContentText>` inside it |
| `actions` | The `<DialogActions>` container |
| `close` | The dialog's close (X) button, or the "Close" button on a fault-style dialog |
| `confirm` | The primary affirmative action button (OK, Save, Apply — including FilterDialog's Apply) |
| `cancel` | The dismiss / negative action button |

The runtime's `packages/test-ids/src/dialog.ts` is the authoritative source for this vocabulary. Any future addition to the role set SHALL be added to the runtime first, then mirrored here.

Dialogs without a model xmi:id (template-internal dialogs like `OperationFaultDialog`) SHALL use a stable synthetic string constant as the `dialogId`. The synthetic id for `OperationFaultDialog` is `'operation-fault'`. Additional synthetic ids SHALL be documented in this spec as they are introduced.

Widget-level testids emitted from within a dialog (e.g. filter operator selectors inside `FilterDialog`) SHALL continue to route through `buildFieldTestId` and its role vocabulary — the dialog grammar covers only the dialog shell, not the widgets inside it.

The `button::clear-all` role on the multi-value clear button (`FilterDialog`, `Tags`) SHALL be retained under `buildFieldTestId` semantics because the runtime has no equivalent DOM level. This is a template-only role and SHALL be documented as such in the field role table.

#### Scenario: ConfirmationDialog emits dialog-domain testids

- **GIVEN** a `ConfirmationDialog` rendered with prop `id = "esm/_myConfirmDialog"`
- **WHEN** the dialog is open
- **THEN** the outer `<Dialog>` carries `data-testid="dialog::esm/_myConfirmDialog"`
- **AND** the `<DialogTitle>` carries `data-testid="dialog::esm/_myConfirmDialog::title"`
- **AND** the `<DialogContentText>` carries `data-testid="dialog::esm/_myConfirmDialog::content"`
- **AND** the cancel `<Button>` carries `data-testid="dialog::esm/_myConfirmDialog::cancel"`
- **AND** the confirm `<Button>` carries `data-testid="dialog::esm/_myConfirmDialog::confirm"`

#### Scenario: FilterDialog Apply maps to confirm

- **GIVEN** a `FilterDialog` rendered with prop `id = "esm/_myFilter"`
- **WHEN** the dialog is open
- **THEN** the "Apply" `<Button>` carries `data-testid="dialog::esm/_myFilter::confirm"`
- **AND** the "Cancel" `<Button>` carries `data-testid="dialog::esm/_myFilter::cancel"`
- **AND** the "Close" `<IconButton>` at the top of the filter row carries `data-testid="dialog::esm/_myFilter::close"`
- **AND** the "Clear all" `<Button>` carries `data-testid="field::esm/_myFilter::button::clear-all"` (template-only, no runtime counterpart)

#### Scenario: OperationFaultDialog uses the synthetic id

- **WHEN** an operation fault dialog is rendered
- **THEN** the `<DialogTitle>` carries `data-testid="dialog::operation-fault::title"`
- **AND** the `<DialogContent>` carries `data-testid="dialog::operation-fault::content"`
- **AND** the "Close" `<Button>` carries `data-testid="dialog::operation-fault::close"`

### Requirement: Tab testid grammar

Every tab controller, individual tab, tab panel, and tab-list container SHALL carry a `data-testid` constructed through the tab helpers exported from `src/utilities/transfer-id.ts`:

- Outer wrapper of a `<ModeledTabs>` component: `tabs::<controllerId>` via `buildTabControllerTestId(ctrlId)`
- Tab strip container (the `<Tabs>` MUI element wrapping the tab bar): `tabs::<controllerId>::list` via `buildTabListTestId(ctrlId)`
- Individual `<Tab>` element: `tabs::<controllerId>::<tabId>` via `buildTabTestId(ctrlId, tabId)`
- Individual `<TabPanel>` element: `tabs::<controllerId>::<tabId>::panel` via `buildTabPanelTestId(ctrlId, tabId)`

The controller id is the XMI id (via `getElementId`) of the model's tab-controller visual element. The tab id is the XMI id of each child tab. Runtime parity: `packages/test-ids/src/tabs.ts::getTab*TestId`.

Templates SHALL NOT emit `field::<id>::tab::<...>`, `field::<id>::panel`, or bare `field::<id>` on any tab-related DOM.

#### Scenario: ModeledTabs emits tab-domain testids

- **GIVEN** a `ModeledTabs` component with controller id `esm/_myTabsCtrl` and two tabs with ids `esm/_tabA` and `esm/_tabB`
- **WHEN** the component is rendered
- **THEN** the outer wrapper carries `data-testid="tabs::esm/_myTabsCtrl"`
- **AND** the `<Tabs>` strip carries `data-testid="tabs::esm/_myTabsCtrl::list"`
- **AND** the two `<Tab>` elements carry `data-testid="tabs::esm/_myTabsCtrl::esm/_tabA"` and `"tabs::esm/_myTabsCtrl::esm/_tabB"`
- **AND** the two `<TabPanel>` elements carry `data-testid="tabs::esm/_myTabsCtrl::esm/_tabA::panel"` and `"tabs::esm/_myTabsCtrl::esm/_tabB::panel"`

### Requirement: Nav-item testid grammar

Every drawer navigation item SHALL carry a hierarchical `data-testid` reflecting its position in the menu tree. The grammar:

- A root-level nav item: `nav::item::<itemId>`
- A nested nav item under a parent path `P`: `nav::item::<P>::<itemId>`
- The parent path is composed by successive nesting: root menu → sub-menu → sub-sub-item yields `nav::item::<rootId>::<subMenuId>::<subSubItemId>`

Under a given nav item testid, semantic sub-parts SHALL carry role-suffixed testids via `buildNavItemRoleTestId(navItemTestId, role)`:

| Role | Applies to |
|---|---|
| `icon` | The nav item's icon element |
| `label` | The nav item's label text container |
| `expand` | The expand-arrow icon or the popper surface that reveals nested children in a mini-drawer collapse |

The parent path SHALL be threaded through React props (`parentPath?: string`) from the root menu invocation down through each nested `<NavCollapse>` and `<NavGroup>`. Path composition is done via `buildNavItemPath(parentPath, currentId)` from `src/utilities/transfer-id.ts`.

Nav-related emissions in `NavItem.tsx.hbs`, `NavCollapse.tsx.hbs`, `NavGroup.tsx.hbs` SHALL NOT emit under `field::` or use `menu::popper` / `menu::boundary` sub-roles.

Two DOM levels within a single nav item SHALL NOT share a testid. The previous double-emission on `NavItem.tsx.hbs` at lines 99 and 142 SHALL be resolved either by removal (if the second emission is redundant) or by role-suffixing (`buildNavItemRoleTestId(navTestId, 'label')` or similar).

#### Scenario: Nested drawer nav emits path-composed testids

- **GIVEN** a drawer menu with root item id `root`, containing a collapse item id `settings`, containing a sub-item id `preferences`
- **WHEN** the drawer is rendered
- **THEN** the root `<ListItemButton>` carries `data-testid="nav::item::root"`
- **AND** the settings collapse boundary carries `data-testid="nav::item::root::settings"`
- **AND** the settings expand popper carries `data-testid="nav::item::root::settings::expand"`
- **AND** the preferences sub-item carries `data-testid="nav::item::root::settings::preferences"`

#### Scenario: NavItem does not emit two testids on the same widget

- **WHEN** any `<NavItem>` is rendered
- **THEN** `page.getByTestId('nav::item::<any-path>')` returns exactly one element per rendered nav item — not two

### Requirement: Breadcrumb testid grammar

Every rendered breadcrumb item SHALL carry a `data-testid` constructed through `buildBreadcrumbTestId(index)` exported from `src/utilities/transfer-id.ts`, producing `breadcrumb::<index>` where `index` is the zero-based position of the item in the breadcrumb chain (root breadcrumb at index 0).

The outer `<Breadcrumbs>` element MAY retain its existing bare `data-testid="application-breadcrumb"` — this is a template-only outer-container hook with no runtime counterpart and no byte-parity constraint. It SHALL NOT be renamed to a `breadcrumb::` grammar because the runtime does not expose an outer-container testid at that level.

#### Scenario: Breadcrumb items carry indexed testids

- **GIVEN** a `CustomBreadcrumb` rendering three items
- **WHEN** the breadcrumbs are displayed
- **THEN** the three items carry `data-testid="breadcrumb::0"`, `data-testid="breadcrumb::1"`, `data-testid="breadcrumb::2"` in render order
- **AND** the outer `<Breadcrumbs>` element carries `data-testid="application-breadcrumb"` (retained template-only)

### Requirement: Domain vocabulary is authoritative from the runtime

The vocabulary of role names accepted by `buildDialogRoleTestId`, `buildTabTestId` (via the tab id / panel suffix), `buildNavItemRoleTestId`, and `buildBreadcrumbTestId` SHALL be maintained in lock-step with the runtime's `@judo/test-ids/src/{dialog,navigation,tabs}.ts`. Any addition, removal, or rename SHALL be raised as a new openspec change with a normative pointer to the runtime commit that introduced the vocabulary change.

Retired role names (SHALL NOT be re-introduced without a runtime-side counterpart):

- `dialog-title` under `field::` — replaced by `dialog::<id>::title`.
- `panel` under `field::` — replaced by `tabs::<ctrl>::<tab>::panel`.
- `tab::<id>` under `field::` — replaced by `tabs::<ctrl>::<tab>`.
- `menu::popper` under `field::` — replaced by `nav::item::<path>::expand`.
- `menu::boundary` under `field::` — collapsed into the nav-item's outer testid (no separate boundary role in the runtime's model).
- `button::apply` under `field::` on dialogs — replaced by `dialog::<id>::confirm`.
- Bare inline literals `"fault-dialog-title"`, `"fault-dialog-content"`, `"close-fault-dialog"` — replaced by `dialog::operation-fault::{title,content,close}`.

#### Scenario: New role name requires a runtime-side counterpart

- **GIVEN** a proposal to introduce a new role name (e.g. `dialog::<id>::secondary-action`) not present in `@judo/test-ids/src/dialog.ts`
- **WHEN** the change is submitted for review
- **THEN** the change is rejected until the runtime side ships the corresponding helper
- **AND** once the runtime is updated, the template's role vocabulary is amended in lock-step by a new openspec change that cites the runtime commit

#### Scenario: Retired role name resurfaces

- **GIVEN** a template edit reintroduces one of the retired roles (`dialog-title`, `menu::popper`, `button::apply` on a dialog button, etc.)
- **WHEN** the change is submitted for review
- **THEN** the change is rejected as a regression against this requirement

**Templates**:
- `actor/src/utilities/transfer-id.ts.hbs` (10 new helpers appended)
- `actor/src/components/dialog/ConfirmationDialog.tsx.hbs`
- `actor/src/components/dialog/FilterDialog.tsx.hbs`
- `actor/src/components/dialog/OperationFaultDialog.tsx.hbs`
- `actor/src/components/ModeledTabs.tsx.hbs`
- `actor/src/components/CustomBreadcrumb.tsx.hbs`
- `actor/src/layout/Drawer/DrawerContent/Navigation/NavItem.tsx.hbs`
- `actor/src/layout/Drawer/DrawerContent/Navigation/NavCollapse.tsx.hbs`
- `actor/src/layout/Drawer/DrawerContent/Navigation/NavGroup.tsx.hbs`

**Out of scope for this change** (tracked as separate follow-ups):

- Options / chips grammar (`<inputId>::option::<optId>`, `<inputId>::chip::<optId>::delete`) — needs runtime devtools inspection to resolve `field::` prefix ambiguity.
- `ApplicationSelector` bare inline literals (`GodView-dialog-close`, `application-changer-close`, `application-changer-submit`) — template-only DOM with no direct runtime counterpart.
- User-menu grammar (`user-menu::<action>`) — no template counterpart identified.
- Actor-selector grammar (`actor-selector::option::<name>`) — same.
- Layout containers in `flex.hbs` (Accordion / Card / Grid / Stack) — no runtime counterpart.
- `DropdownButton.tsx.hbs` `container` role — no runtime counterpart.
- Cosmetic `build*TestId` → `get*TestId` rename.
