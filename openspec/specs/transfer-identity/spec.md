# transfer-identity Specification

## Purpose
How a generated page identifies a transfer (row keys, new-row seeding) and the `data-testid` grammar it emits for fields, tables, rows, cells, buttons, dialogs, tabs, navigation and breadcrumbs. The grammar is shared with `judo-frontend-runtime` (`@judo/test-ids`), so one Playwright locator works on both engines.

## Requirements

### Requirement: Row-key resolution follows a runtime-aligned fallback chain

Every generator-emitted site that needs to identify a data row — MUI DataGrid `getRowId`, per-row validation maps, per-row state models, action target lookups, `Array.prototype.find(...__identifier === …)` comparisons — SHALL derive that identity through a single shared helper `resolveTransferId(row, index?)`. The helper SHALL evaluate the following fallback chain in this order and SHALL return the first non-`undefined`, non-empty result:

1. `row.__signedIdentifier`
2. `row.__identifier`
3. `row.__tempId`
4. `row.id`
5. `` `idx-${index}` `` when an index is supplied
6. `` `idx-undefined` `` as a last resort (never expected in practice)

The helper SHALL be exported from the actor-local file `src/utilities/transfer-id.ts`. It SHALL be a pure function of its arguments (no closures over module state, no side effects). Its return type SHALL be `string`.

Templates SHALL NOT emit inline row-key expressions such as `row.__identifier!` or `getRowIdentifier`-style closures.

#### Scenario: Saved row resolves to its signed identifier

- **GIVEN** a row `{ __signedIdentifier: "sig-abc", __identifier: "id-xyz", __tempId: undefined, id: 42 }`
- **WHEN** `resolveTransferId(row)` is called
- **THEN** the result is `"sig-abc"`

#### Scenario: Client-created row resolves to its temp id

- **GIVEN** a row `{ __signedIdentifier: undefined, __identifier: undefined, __tempId: "temp::1719660000000::a3f9c2b", __isNew: true }`
- **WHEN** `resolveTransferId(row)` is called
- **THEN** the result is `"temp::1719660000000::a3f9c2b"`

#### Scenario: Legacy draft-seeded row resolves to its identifier verbatim

- **GIVEN** a row `{ __identifier: "draft:f47ac10b-58cc-4372-a567-0e02b2c3d479" }` (residual seed from Phase 1's deserializer)
- **WHEN** `resolveTransferId(row)` is called
- **THEN** the result is `"draft:f47ac10b-58cc-4372-a567-0e02b2c3d479"`
- **AND** the value is stable across re-renders

#### Scenario: Row with no identity resolves to indexed key

- **GIVEN** a row `{}` at index `7`
- **WHEN** `resolveTransferId(row, 7)` is called
- **THEN** the result is `"idx-7"`

#### Scenario: MUI DataGrid uses the resolver as getRowId

- **WHEN** the generator emits an `EagerTable`, `LazyTable`, or inline relation-column DataGrid
- **THEN** the `<DataGrid>` element carries the prop `getRowId={resolveTransferId}` (or an equivalent `(r) => resolveTransferId(r)` if MUI's type inference demands it)
- **AND** no emitted DataGrid carries the string prop `identifierAttribute={'__identifier'}`

### Requirement: New-row seeding uses `__tempId` and `__isNew`

Every generator-emitted site that constructs a client-side transfer object for a row the user has *not yet saved* SHALL populate two fields and SHALL leave `__identifier` unset:

- `__tempId: newTempId()` where `newTempId()` returns a string of the shape `` `temp::${Date.now()}::${randomHex(7)}` ``.
- `__isNew: true`.

Sites SHALL NOT write `__identifier: \`${draftIdentifierPrefix}${uuidv4()}\`` or any other value derived from the `draft:` prefix. The template SHALL NOT import `draftIdentifierPrefix` from `~/services/data-api/common/utils` at any seed site.

The predicate `isNewRow(row)` SHALL return `true` when either:
- `row.__isNew === true`, OR
- (compatibility rung) `typeof row.__identifier === 'string' && row.__identifier.startsWith('draft:')`

The compatibility rung exists solely to correctly classify rows seeded by Phase 1's `rest/serializer.ts.hbs:92–93` deserializer and SHALL be removed once Phase 1 stops emitting `draft:` prefixes.

Every branch previously written as `rowData.__identifier!.startsWith(draftIdentifierPrefix)` SHALL be rewritten to `isNewRow(rowData)`.

#### Scenario: Inline table row-add produces a temp row

- **GIVEN** the user clicks the inline "add row" action on an `EagerTable` or `LazyTable`
- **WHEN** the template emits the new row object
- **THEN** the object contains `__tempId` matching `/^temp::\d+::[0-9a-f]+$/` and `__isNew: true`
- **AND** the object does NOT contain a `__identifier` field
- **AND** the object does NOT contain any string beginning with `"draft:"`

#### Scenario: Save-draft form return produces a temp row

- **GIVEN** an `OpenCreateFormAction` returning with `result === 'submit-draft'`
- **WHEN** the template emits the decorated row to append to the parent collection
- **THEN** the object contains `__tempId` and `__isNew: true`
- **AND** the object does NOT contain `__identifier`

#### Scenario: Legacy draft-seeded row still classifies as new

- **GIVEN** a row `{ __identifier: "draft:xyz" }` arriving from Phase 1's deserializer
- **WHEN** `isNewRow(row)` is called
- **THEN** the result is `true`

#### Scenario: Saved row does not classify as new

- **GIVEN** a row `{ __signedIdentifier: "sig-abc", __identifier: "id-xyz" }`
- **WHEN** `isNewRow(row)` is called
- **THEN** the result is `false`

### Requirement: Hierarchical `data-testid` scheme

Every `data-testid` attribute rendered by the generator SHALL be constructed through one of four helper functions exported from `src/utilities/transfer-id.ts`:

- `buildFieldTestId(xmiId, role?)` → `` `field::${xmiId}` `` or `` `field::${xmiId}::${role}` ``
- `buildTableTestId(tableId)` → `` `table::${tableId}` ``
- `buildRowTestId(tableId, row, index?)` → `` `table::${tableId}::row::${resolveTransferId(row, index)}` ``
- `buildCellTestId(tableId, row, columnName, index?)` → `` `table::${tableId}::row::${resolveTransferId(row, index)}::cell::${columnName}` ``

The first argument to `buildFieldTestId` (the element id) SHALL be produced at generation time by the `getElementId(EObject)` Java helper in `UiGeneralHelper`, not by `getXMIID`. `getElementId` reads the EObject's `sourceId` structural feature when present and non-empty; otherwise it falls back to the current `getXMIID` behaviour. This matches the runtime's `getElementTestId` in `@judo/test-ids/src/element.ts`, which resolves `element.sourceId || element['xmi:id'] || element.name`.

The `role` argument accepted by `buildFieldTestId` SHALL be one of the following values or SHALL nest with `::` for compound roles:

| Role | Applies to |
|---|---|
| (absent) | The outer wrapper `<div>` around any widget — both simple input widgets (`textinput`, `dateinput`, `enumerationcombo`, … via `containers/widget-fragments/*.hbs`) and composite widget templates (`SingleRelationInput`, `Tags`, `TextWithTypeAhead`, `BinaryInput`, `AssociationButton`, `ModeledTabs`). Runtime parity: `TextInputComponent.tsx:101`, `LinkRenderer.tsx:645`. |
| `input` | The DOM element receiving focus and keystrokes |
| `autocomplete` | The MUI `<Autocomplete>` element when nested inside a wrapper |
| `dropdown` | A popper / menu / dropdown element (typeahead results). Not yet reconciled with runtime's separate `menu` slot on the relation-picker popper — see open questions below. |
| `actions` | Icon-button strip container inside a relation widget's `InputAdornment` (`SingleRelationInput` and `Tags` only). Runtime parity: `LinkRenderer.tsx:706`. |
| `button::set` | Primary open-selector-dialog button (runtime `opensetselector` mapping per `@judo/test-ids/src/element.ts::getFieldButtonRole`) |
| `button::create` | Inline create button (runtime `opencreateform` mapping) |
| `button::view` | View / open-form button (runtime `openpage` / `rowopenpage` mapping) |
| `button::delete` | Row-delete button (runtime `rowdelete` mapping) |
| `button::unset` | Unset-current-value button on relation widgets. Emitted dynamically via `buildFieldTestId(id, \`button::${buttonProp.name}\`)` in `SingleRelationInput.tsx.hbs` when the model provides an unset-relation action. |
| `button::<modelActionName>` | Any other model-driven button (`view`, `create`, `set`, `unset`, `delete`, custom) rendered through the dynamic `buttonProp.name` path. |
| `button::add` | Add-item button (multi-value inputs; template-only role, no runtime counterpart today) |
| `button::remove` | Remove-item button (multi-value inputs, binary widget file-remove) |
| `button::clear-all` | Clear-all button on multi-value widgets (`Tags`, `FilterDialog`). Template-only — runtime has no equivalent DOM level. |
| `menu::item::<name>` | A named menu item |
| `filter::operator::<opName>` | Filter operator selector entry |

The button-role labels above are the authoritative vocabulary. Where a role name in this table conflicts with a name emitted by the runtime for the same DOM node, the runtime is the source of truth (per `dataid-template-runtime.pdf` §5). Any future divergence SHALL be resolved by amending this table, not by re-diverging emissions.

Templates SHALL NOT emit `data-testid` values via inline string concatenation such as `${id}-clear-all`, `${id}-open-selector`, `filter-operator-${…}`, or bare `${item.id}`. Every emission site SHALL route through one of the four helpers above.

For any single widget or row, no two DOM elements SHALL carry the same `data-testid` value. This SHALL be structurally guaranteed by giving each DOM level a distinct `role` argument.

Retired role names (previously emitted; SHALL NOT be re-introduced without an explicit runtime-side counterpart):

- `selector` — replaced by `set` on the primary open-selector button.
- `container` — replaced by bare `field::<id>` on composite widget outer wrappers (`SingleRelationInput`, `Tags`, `TextWithTypeAhead`, `BinaryInput`, `AssociationButton`, `ModeledTabs`). The `container` role is still emitted on dialog outer elements, `DropdownButton`, and `flex.hbs` layout wrappers pending separate design decisions (see out-of-scope notes in `openspec/changes/align-testids-with-runtime-package/proposal.md`).

Corrected role vocabulary entries (documented but never emitted in previous versions of this spec):

- `button::clear` — removed. The template never emitted this literal. Widgets with a clear-relation action use `button::unset` (dynamic, matches runtime); multi-value widgets use `button::clear-all` (template-only).

#### Scenario: RelationDefinedLink emits three distinct testids

- **GIVEN** a `SingleRelationInput` widget with XMI id `esm/_28BIQBn` and no `sourceId`
- **WHEN** the widget is rendered
- **THEN** the outer wrapper carries `data-testid="field::esm/_28BIQBn"` (bare, no role suffix)
- **AND** the autocomplete carries `data-testid="field::esm/_28BIQBn::autocomplete"`
- **AND** the underlying input carries `data-testid="field::esm/_28BIQBn::input"`
- **AND** the trailing icon-button strip carries `data-testid="field::esm/_28BIQBn::actions"`
- **AND** the primary open-selector button carries `data-testid="field::esm/_28BIQBn::button::set"`
- **AND** when the model provides an unset-relation action, the corresponding button carries `data-testid="field::esm/_28BIQBn::button::unset"`
- **AND** `page.getByTestId("field::esm/_28BIQBn::input")` returns exactly one element

#### Scenario: Element ID resolution prefers `sourceId` when present

- **GIVEN** a widget EObject whose `sourceId` is `"psm/_alpha"` and whose `xmi:id` is `"ui/_beta"`
- **WHEN** the template emits any `data-testid` for that widget
- **THEN** the id segment is `"psm/_alpha"` (not `"ui/_beta"`)
- **AND** the same widget rendered by the JUDO frontend runtime carries the same id segment `"psm/_alpha"`

#### Scenario: Element ID resolution falls back to `xmi:id` when `sourceId` is absent

- **GIVEN** a widget EObject with `xmi:id` `"ui/_beta"` and no `sourceId` structural feature (or an empty one)
- **WHEN** the template emits any `data-testid` for that widget
- **THEN** the id segment is `"ui/_beta"`

#### Scenario: Table row testid uses the resolver

- **GIVEN** a table with id `Actor/(esm/_ZVpS8M7)/ClassType` containing a saved row `{ __signedIdentifier: "sig-42" }`
- **WHEN** the row is rendered
- **THEN** its `data-testid` is `"table::Actor/(esm/_ZVpS8M7)/ClassType::row::sig-42"`
- **AND** cells within it carry `data-testid="table::Actor/(esm/_ZVpS8M7)/ClassType::row::sig-42::cell::<column>"`

#### Scenario: Retired role labels no longer emitted

- **WHEN** the generator emits any of the six composite widget templates (`SingleRelationInput`, `Tags`, `TextWithTypeAhead`, `BinaryInput`, `AssociationButton`, `ModeledTabs`)
- **THEN** no `data-testid` value contains the substrings `::selector` or `::container`
- **AND** the button previously labeled `::button::selector` now carries `::button::set`
- **AND** the outer wrapper previously labeled `::container` now carries bare `field::<id>`

#### Scenario: Ad-hoc suffixes are eliminated

- **WHEN** the generator emits the DropdownButton, per-row action buttons, or FilterDialog operator MenuItems
- **THEN** no `data-testid` value contains a hyphen-joined ad-hoc suffix (`-dropdown-toggle`, `-clear-all`, `-open-selector`, `-inline-create`)
- **AND** every such button carries a testid produced by `buildFieldTestId(...)` with an explicit role

#### Scenario: Runtime and template output share testids

- **GIVEN** the same UI model rendered by (i) this template and (ii) the JUDO frontend runtime at `feature/unify-data-testid-contract` or later
- **WHEN** the generated DOM is compared for any relation widget or table row
- **THEN** the `data-testid` values on the widget outer wrapper (bare `field::<id>`), input, autocomplete, `button::set`, `button::create`, `button::view`, `button::delete`, and every table row / cell are byte-identical across the two outputs
- **AND** the following remain open pending live-DOM comparison: `dropdown` vs `menu` slot mapping on relation-picker poppers, `actions` slot on the icon-button strip

**Templates**:
- `actor/src/utilities/transfer-id.ts.hbs` (unchanged from parent change)
- `actor/src/components/widgets/SingleRelationInput.tsx.hbs`
- `actor/src/components/widgets/Tags.tsx.hbs`
- `actor/src/components/widgets/TextWithTypeAhead.tsx.hbs`
- All templates that previously used `{{ getXMIID X }}` inside a `buildFieldTestId(...)` argument

**Java helpers**:
- `UiGeneralHelper.getElementId(EObject)` — new; resolves `sourceId` first, falls back to `getXMIID`. Used only for `data-testid` emission.
- `UiGeneralHelper.getXMIID(EObject)` — unchanged; retained for Pandino keys, i18n keys, container-name detection.

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

### Requirement: Standalone-button testid grammar

Every standalone button emitted by the generator SHALL carry a `data-testid` constructed through `buildButtonTestId(buttonId, actionType?)` exported from `src/utilities/transfer-id.ts`. The grammar SHALL be:

- Button with an associated action definition: `button::<buttonId>::<actionType>`.
- Button without an associated action definition: `button::<buttonId>` (bare).

The `<buttonId>` SHALL be the button element's `sourceId` when present, otherwise its `xmi:id`, resolved via the existing `UiGeneralHelper.getElementId(EObject)` helper.

The `<actionType>` SHALL be the string returned by `UiActionsHelper.getButtonActionType(EObject)`, defined as:

- Read the button's `actionDefinition` reference.
- Take the referenced element's `@type` (the EMF class name).
- Apply the transformation `.replace("ui:", "").replace("ActionDefinition", "").toLowerCase()`.

This transformation is a byte-exact port of the runtime's `packages/test-ids/src/element.ts::getButtonActionType`. The runtime's `packages/test-ids/src/element.ts::getButtonTestId` is the authoritative source for the grammar itself. Any future change to the transformation or grammar SHALL be made in the runtime first and mirrored here.

**Distinction from field-scoped buttons.** Buttons rendered inside a relation widget (`SingleRelationInput`, `Tags`, `TextWithTypeAhead`) SHALL continue to use the `field::<fieldId>::button::<role>` grammar defined under the "Hierarchical `data-testid` scheme" Requirement. The two grammars are non-overlapping:

- `button::<id>::<actionType>` — standalone buttons (page-level, container-level, button groups, FAB, table toolbar).
- `field::<fieldId>::button::<role>` — buttons that are semantic descendants of a specific field.

The choice between them SHALL be determined by whether the button's DOM ancestry includes a `field::<id>` wrapper.

**Row-action buttons: OPEN.** Per-row action buttons rendered inside a table's `__actions__` cell SHALL follow whichever grammar is settled by the runtime's clarification of `RowActionCell.tsx` (which currently uses `getButtonTestId`) vs `getRowActionTestId` (which is exported by `@judo/test-ids` but unused by any current renderer). This spec does NOT normatively fix a grammar for row-action buttons; the current template output at `components/table/table-row-actions.tsx.hbs` remains in the row-scoped form (`table::<tableId>::row::<transferId>::button::<actionId>`) until the runtime clarification lands.

#### Scenario: Table toolbar Create button emits `button::<id>::opencreateform`

- **GIVEN** a table with a toolbar action whose `actionDefinition` is `OpenCreateFormActionDefinition`
- **WHEN** the toolbar button is rendered
- **THEN** the DOM `data-testid` is `button::<toolbar-button-xmiId>::opencreateform`
- **AND** the value is byte-identical to what the runtime emits for the same model element

#### Scenario: Standalone page button emits `button::<id>::<actionType>`

- **GIVEN** a page-level button whose `actionDefinition` is `OpenPageActionDefinition`
- **WHEN** the button is rendered
- **THEN** the DOM `data-testid` is `button::<button-xmiId>::openpage`

#### Scenario: Button without action-definition emits bare form

- **GIVEN** a button model shape without an `actionDefinition` reference (decorative button, custom-extension button)
- **WHEN** the button is rendered
- **THEN** the DOM `data-testid` is `button::<button-xmiId>` (no `::<actionType>` suffix)
- **AND** the value is byte-identical to the runtime's `joinTestId('button', id, undefined)` output for the same shape

#### Scenario: Field-scoped button retains its existing grammar

- **GIVEN** a `SingleRelationInput` widget with a primary set button whose `actionDefinition` is `OpenSetSelectorActionDefinition`
- **WHEN** the widget is rendered
- **THEN** the set button's DOM `data-testid` is `field::<field-xmiId>::button::set`
- **AND** it is NOT `button::<button-xmiId>::opensetselector`

#### Scenario: Standalone button testid is Playwright-addressable cross-engine

- **GIVEN** the same UI model rendered by (i) this template and (ii) the JUDO frontend runtime
- **WHEN** `page.getByTestId('button::<toolbar-button-xmiId>::opencreateform')` is evaluated on both outputs
- **THEN** the selector resolves exactly one element on each output
- **AND** each resolved element is the same DOM level (the actual `<button>` receiving click events)

**Templates**:
- `actor/src/utilities/transfer-id.ts.hbs` (new export `buildButtonTestId`)
- `actor/src/utilities/table.ts.hbs` (new `actionType` field on `ToolBarActionProps<T>`)
- `actor/src/containers/components/table/index.tsx.hbs` (every `ToolBarActionProps` literal gains `actionType`)
- `actor/src/fragments/container/common-imports.fragment.hbs` (adds `buildButtonTestId` to the shared transfer-id import)
- `actor/src/containers/page.tsx.hbs`
- `actor/src/containers/dialog.tsx.hbs` (dialog action-bar buttons — same emission class as `page.tsx.hbs`, missed in the initial site enumeration)
- `actor/src/containers/widget-fragments/button.hbs`
- `actor/src/containers/widget-fragments/buttongroup.hbs`
- `actor/src/containers/widget-fragments/fab-button-group.fragment.hbs`
- `actor/src/containers/widget-fragments/flex.hbs`
- `actor/src/components/table/EagerTable.tsx.hbs`
- `actor/src/components/table/LazyTable.tsx.hbs`
- `actor/src/components/widgets/AssociationButton.tsx.hbs` (shared TSX component for single-relation `OpenPageActionDefinition` navigation buttons; gains an optional `actionType` prop, threaded from `containers/widget-fragments/button.hbs`)

**Java helpers**:
- `judo-ui-react/src/main/java/hu/blackbelt/judo/ui/generator/react/UiActionsHelper.java` (new public static method `getButtonActionType(EObject)`)

### Requirement: Canonical grid row and cell test IDs

Every rendered data-grid row SHALL carry `data-testid` equal to
`table::<table-id>::row::<resolved-transfer-id>`, and every rendered cell SHALL carry
`table::<table-id>::row::<resolved-transfer-id>::cell::<column-field>`. The `<table-id>` token SHALL
be the value produced by the template's element-identity helper (`sourceId` when present, otherwise
the sanitized `xmi:id`), the transfer segment SHALL be produced by `resolveTransferId`, and the
column segment SHALL be the model field name. Emission SHALL delegate to the helpers in
`utilities/transfer-id.ts.hbs`; no template SHALL re-implement the `::` joining grammar.

#### Scenario: Saved row and cell are uniquely addressable
- **GIVEN** a table whose model element carries a `sourceId`, and a row whose transfer has a `__signedIdentifier`
- **WHEN** the grid renders that row
- **THEN** exactly one element matches `table::<sourceId>::row::<signedIdentifier>`
- **AND** exactly one element matches `table::<sourceId>::row::<signedIdentifier>::cell::<column-field>` for each rendered column
- **AND** the row and cell selectors identify different DOM elements

#### Scenario: Column segment is model-derived, not presentational
- **GIVEN** a column whose model field is `customerName` and whose visible header is translated
- **WHEN** the cell test ID is constructed
- **THEN** the segment is `::cell::customerName`
- **AND** it does not use the translated header text, the visual column index, or the current column order

#### Scenario: Unsaved row uses the temp identity
- **GIVEN** a client-created row that has been seeded with `__tempId` and `__isNew`
- **WHEN** the grid renders that row
- **THEN** the row test ID's transfer segment is the `__tempId` value
- **AND** the value carries the `temp::<ms>::<7-hex>` shape produced by `newTempId`

#### Scenario: Transfer identity precedence matches the runtime
- **GIVEN** a transfer object
- **WHEN** its row test ID is constructed
- **THEN** the transfer segment resolves in the order `__signedIdentifier`, `__identifier`, `__tempId`, `id`, then `idx-<index>`
- **AND** the resolved value equals what `@judo/test-ids`' `resolveTransferId` returns for the same object and index

#### Scenario: Cross-engine equality for the same model and data
- **GIVEN** the same UI model element and the same transfer object rendered by this template and by `judo-frontend-runtime`
- **WHEN** both engines emit the row and cell test IDs
- **THEN** the emitted strings are identical
- **AND** a single Playwright locator built from the model identity matches on both engines

### Requirement: Canonical selection-cell test ID

When a grid renders a selection checkbox column, the checkbox's owning cell SHALL carry
`table::<table-id>::row::<resolved-transfer-id>::cell::__check__`, so a row's selection control is
addressable through the same row hierarchy as its data cells and requires no DOM-shape locator.

#### Scenario: Selection checkbox is addressable through the row hierarchy
- **GIVEN** a table rendered with checkbox selection enabled
- **WHEN** a row is rendered
- **THEN** exactly one element matches `table::<table-id>::row::<transfer-id>::cell::__check__`
- **AND** an interactive checkbox is reachable within that element
- **AND** the selection cell is not addressed by a CSS class, `[data-field]`, or positional index

#### Scenario: No selection column emits no selection cell
- **GIVEN** a table rendered without checkbox selection
- **WHEN** its rows are rendered
- **THEN** no element matches `…::cell::__check__` for any row

### Requirement: Canonical filter-panel test IDs

The data-grid filter panel SHALL carry `table::<table-id>::filter-panel`, and its column, operator
and value inputs SHALL carry that value suffixed with `::column`, `::operator` and `::value`
respectively. The inputs SHALL be stamped through the grid's own filter-form properties so the IDs
survive MUI's internal rendering.

#### Scenario: Filter inputs are addressable without label locators
- **GIVEN** a table that supports filtering
- **WHEN** the filter panel is opened
- **THEN** exactly one element matches `table::<table-id>::filter-panel`
- **AND** exactly one element each matches its `::column`, `::operator` and `::value` suffixes
- **AND** no filter input is addressed by translated label text, placeholder, or DOM position

#### Scenario: Filter-panel IDs are table-scoped
- **GIVEN** two tables rendered on one page
- **WHEN** the filter panel of one table is opened
- **THEN** its panel and input test IDs carry that table's own identity
- **AND** they do not match the other table's panel selectors

### Requirement: Row-action buttons use the canonical row-scoped action grammar

Row-action buttons SHALL carry `table::<table-id>::row::<resolved-transfer-id>::action::<role>`,
where `<role>` is the normalized action role shared with the runtime (for example `set`, `create`,
`view`, `delete`). An overflow/dropdown trigger SHALL use the reserved role `overflow`. When no role
can be resolved for a button, emission SHALL fall back to the standalone button grammar
`button::<id>::<actionType>`. The previous form `…::row::<id>::button::<name>`, keyed on a free-form
`testId ?? id`, SHALL NOT be emitted.

This resolves the row-action grammar left open as design decision D3 of
`align-buttons-with-runtime`: `judo-frontend-runtime`'s `RowActionCell` renderer resolves a role and
calls `getRowActionTestId`, using `getButtonTestId` only as the no-role fallback, so the row-scoped
`::action::<role>` form is the settled canonical shape.

#### Scenario: Row action is addressed by canonical role
- **GIVEN** a table row rendering a delete row-action
- **WHEN** the action button is rendered
- **THEN** its test ID is `table::<table-id>::row::<transfer-id>::action::delete`
- **AND** no element for that action matches `…::row::<transfer-id>::button::<anything>`

#### Scenario: Overflow trigger uses the reserved role
- **GIVEN** a row whose actions exceed the featured-action count
- **WHEN** the overflow dropdown trigger is rendered
- **THEN** its test ID is `table::<table-id>::row::<transfer-id>::action::overflow`

#### Scenario: Role-less button keeps the standalone grammar
- **GIVEN** a row-action button for which no action role can be resolved
- **WHEN** its test ID is constructed
- **THEN** the emitted value is the standalone `button::<id>::<actionType>` form
- **AND** the emission does not produce an empty or `unknown` role segment

#### Scenario: Cross-engine equality for row actions
- **GIVEN** the same row-action button and transfer object rendered by this template and by `judo-frontend-runtime`
- **WHEN** both engines emit the row-action test ID
- **THEN** the emitted strings are identical
