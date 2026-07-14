## MODIFIED Requirements

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
| `button::set` | Primary open-selector-dialog button (runtime `opensetselector` mapping per `@judo/test-ids/src/element.ts::getFieldButtonRole`) |
| `button::clear` | Clear-current-value button |
| `button::create` | Inline create button (runtime `opencreateform` mapping) |
| `button::view` | View / open-form button (runtime `openpage` / `rowopenpage` mapping) |
| `button::delete` | Row-delete button (runtime `rowdelete` mapping) |
| `button::add` | Add-item button (multi-value inputs; template-only role, no runtime counterpart today) |
| `button::remove` | Remove-item button (multi-value inputs, binary widget file-remove) |
| `menu::item::<name>` | A named menu item |
| `filter::operator::<opName>` | Filter operator selector entry |

The button-role labels above are the authoritative vocabulary. Where a role name in this table conflicts with a name emitted by the runtime for the same DOM node, the runtime is the source of truth (per `dataid-template-runtime.pdf` §5). Any future divergence SHALL be resolved by amending this table, not by re-diverging emissions.

Templates SHALL NOT emit `data-testid` values via inline string concatenation such as `${id}-clear-all`, `${id}-open-selector`, `filter-operator-${…}`, or bare `${item.id}`. Every emission site SHALL route through one of the four helpers above.

For any single widget or row, no two DOM elements SHALL carry the same `data-testid` value. This SHALL be structurally guaranteed by giving each DOM level a distinct `role` argument.

Retired role names (previously emitted; SHALL NOT be re-introduced without an explicit runtime-side counterpart):

- `selector` — replaced by `set` on the primary open-selector button.
- `container` — replaced by bare `field::<id>` on composite widget outer wrappers (`SingleRelationInput`, `Tags`, `TextWithTypeAhead`, `BinaryInput`, `AssociationButton`, `ModeledTabs`). The `container` role is still emitted on dialog outer elements, `DropdownButton`, and `flex.hbs` layout wrappers pending separate design decisions (see out-of-scope notes in `openspec/changes/align-testids-with-runtime-package/proposal.md`).

#### Scenario: SingleRelationInput emits distinct testids at each DOM level

- **GIVEN** a `SingleRelationInput` widget with XMI id `esm/_28BIQBn` and no `sourceId`
- **WHEN** the widget is rendered
- **THEN** the outer wrapper carries `data-testid="field::esm/_28BIQBn"` (bare, no role suffix)
- **AND** the autocomplete carries `data-testid="field::esm/_28BIQBn::autocomplete"`
- **AND** the underlying input carries `data-testid="field::esm/_28BIQBn::input"`
- **AND** the primary open-selector button carries `data-testid="field::esm/_28BIQBn::button::set"`
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

#### Scenario: Runtime and template output share testids byte-for-byte on shared DOM nodes

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
